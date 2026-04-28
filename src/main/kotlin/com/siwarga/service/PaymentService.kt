package com.siwarga.service

import com.siwarga.domain.entity.Fine
import com.siwarga.domain.entity.Payment
import com.siwarga.domain.entity.PaymentAllocation
import com.siwarga.domain.enums.LedgerStatus
import com.siwarga.dto.request.CreatePaymentRequest
import com.siwarga.dto.response.AllocationDetail
import com.siwarga.dto.response.PaymentAllocationResult
import com.siwarga.dto.response.PaymentResponse
import com.siwarga.exception.BusinessRuleException
import com.siwarga.exception.ResourceNotFoundException
import com.siwarga.repository.DuesLedgerRepository
import com.siwarga.repository.FineRepository
import com.siwarga.repository.PaymentAllocationRepository
import com.siwarga.repository.PaymentRepository
import com.siwarga.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth

@Service
@Transactional
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentAllocationRepository: PaymentAllocationRepository,
    private val ledgerRepository: DuesLedgerRepository,
    private val fineRepository: FineRepository,
    private val userRepository: UserRepository,
    private val residentService: ResidentService,
    private val ledgerService: LedgerService,
    private val fineService: FineService,
) {
    companion object {
        private const val MONTHLY_DUES = 120_000L
        private const val SIX_MONTH_BLOCK = 6 * MONTHLY_DUES // 720_000
        private const val SIX_MONTH_DISCOUNT = 60_000L
    }

    @Transactional(readOnly = true)
    fun getAll(): List<PaymentResponse> = paymentRepository.findAll().map { PaymentResponse.from(it) }

    @Transactional(readOnly = true)
    fun getByHouse(houseId: Long): List<PaymentResponse> =
        paymentRepository.findByHouseIdOrderByPaymentDateDesc(houseId).map { PaymentResponse.from(it) }

    @Transactional(readOnly = true)
    fun getById(id: Long): PaymentResponse = PaymentResponse.from(findById(id))

    fun create(request: CreatePaymentRequest): PaymentAllocationResult {
        val house = residentService.findHouseById(request.houseId)

        if (request.receiptNumber != null && paymentRepository.existsByReceiptNumber(request.receiptNumber)) {
            throw BusinessRuleException("Receipt number '${request.receiptNumber}' already exists")
        }

        val currentUser =
            SecurityContextHolder
                .getContext()
                .authentication
                ?.name
                ?.let { userRepository.findByUsername(it).orElse(null) }

        val currentMonth = YearMonth.now()

        // Step 1: Ensure ledger rows exist up to current month
        ledgerService.ensureLedgerExists(house, currentMonth)

        // Step 2: Compute and persist any new fines
        fineService.computeAndPersistFines(house)

        // Step 3: Build debt queue (unpaid ledgers + fines, chronologically ordered)
        val unpaidLedgers =
            ledgerRepository
                .findUnpaidUpToMonth(
                    house.id,
                    currentMonth.year,
                    currentMonth.monthValue,
                    listOf(LedgerStatus.UNPAID, LedgerStatus.PARTIAL),
                ).toMutableList()

        val unpaidFines =
            fineRepository
                .findByHouseIdAndStatusInOrderByTriggerMonthAsc(
                    house.id,
                    listOf(LedgerStatus.UNPAID, LedgerStatus.PARTIAL),
                ).toMutableList()

        val hasArrears = unpaidLedgers.isNotEmpty() || unpaidFines.isNotEmpty()

        // Step 4: Calculate discount
        val discount =
            if (!hasArrears && request.amount >= SIX_MONTH_BLOCK) {
                val blocks = request.amount / SIX_MONTH_BLOCK
                blocks * SIX_MONTH_DISCOUNT
            } else {
                0L
            }
        var remaining = request.amount - discount

        // Step 5: Save the payment record
        val payment =
            paymentRepository.save(
                Payment(
                    house = house,
                    amount = request.amount,
                    paymentDate = request.paymentDate,
                    receiptNumber = request.receiptNumber,
                    notes = request.notes,
                    recordedBy = currentUser,
                ),
            )

        val allocations = mutableListOf<PaymentAllocation>()
        val allocationDetails = mutableListOf<AllocationDetail>()

        // Step 6: Interleave ledgers and fines by timeline position and allocate
        // Fines: triggerMonth is the 1-based position in the consecutive-unpaid run.
        // We place each fine just before the ledger at that same sequential index.
        val ledgerIterator = unpaidLedgers.iterator()
        val fineIterator = unpaidFines.iterator()
        var nextFine: Fine? = if (fineIterator.hasNext()) fineIterator.next() else null
        var consecutivePos = 0 // track position in unpaid run for fine interleaving

        for (ledger in unpaidLedgers) {
            if (remaining <= 0) break
            consecutivePos++

            // Apply any fines that trigger at or before this consecutive position
            while (nextFine != null && nextFine!!.triggerMonth <= consecutivePos) {
                if (remaining <= 0) break
                val fine = nextFine!!
                val outstanding = fine.amount - fine.amountPaid
                val apply = minOf(remaining, outstanding)
                fine.amountPaid += apply
                fine.status = if (fine.amountPaid >= fine.amount) LedgerStatus.PAID else LedgerStatus.PARTIAL
                fineRepository.save(fine)
                remaining -= apply
                val pa = paymentAllocationRepository.save(PaymentAllocation(payment = payment, fine = fine, amountApplied = apply))
                allocations.add(pa)
                allocationDetails.add(
                    AllocationDetail(
                        type = "FINE",
                        period = null,
                        fineType = fine.fineType.name,
                        amountApplied = apply,
                        fullyPaid = fine.status == LedgerStatus.PAID,
                    ),
                )
                nextFine = if (fineIterator.hasNext()) fineIterator.next() else null
            }

            if (remaining <= 0) break
            val outstanding = ledger.amountDue - ledger.amountPaid
            val apply = minOf(remaining, outstanding)
            ledger.amountPaid += apply
            ledger.status = if (ledger.amountPaid >= ledger.amountDue) LedgerStatus.PAID else LedgerStatus.PARTIAL
            if (ledger.status == LedgerStatus.PAID) ledger.paidDate = request.paymentDate
            ledgerRepository.save(ledger)
            remaining -= apply
            val pa = paymentAllocationRepository.save(PaymentAllocation(payment = payment, ledger = ledger, amountApplied = apply))
            allocations.add(pa)
            allocationDetails.add(
                AllocationDetail(
                    type = "DUES",
                    period = "%d-%02d".format(ledger.periodYear, ledger.periodMonth),
                    fineType = null,
                    amountApplied = apply,
                    fullyPaid = ledger.status == LedgerStatus.PAID,
                ),
            )
        }

        // Apply any remaining fines that didn't interleave with a ledger
        while (nextFine != null && remaining > 0) {
            val fine = nextFine!!
            val outstanding = fine.amount - fine.amountPaid
            val apply = minOf(remaining, outstanding)
            fine.amountPaid += apply
            fine.status = if (fine.amountPaid >= fine.amount) LedgerStatus.PAID else LedgerStatus.PARTIAL
            fineRepository.save(fine)
            remaining -= apply
            paymentAllocationRepository.save(PaymentAllocation(payment = payment, fine = fine, amountApplied = apply))
            allocationDetails.add(
                AllocationDetail(
                    type = "FINE",
                    period = null,
                    fineType = fine.fineType.name,
                    amountApplied = apply,
                    fullyPaid = fine.status == LedgerStatus.PAID,
                ),
            )
            nextFine = if (fineIterator.hasNext()) fineIterator.next() else null
        }

        // Step 7: Apply excess to future months
        if (remaining > 0) {
            var futureMonth = currentMonth.plusMonths(1)
            while (remaining > 0) {
                val futureLedger = ledgerService.ensureAndGetLedger(house, futureMonth)
                val outstanding = futureLedger.amountDue - futureLedger.amountPaid
                val apply = minOf(remaining, outstanding)
                futureLedger.amountPaid += apply
                futureLedger.status = if (futureLedger.amountPaid >= futureLedger.amountDue) LedgerStatus.PAID else LedgerStatus.PARTIAL
                if (futureLedger.status == LedgerStatus.PAID) futureLedger.paidDate = request.paymentDate
                ledgerRepository.save(futureLedger)
                remaining -= apply
                paymentAllocationRepository.save(PaymentAllocation(payment = payment, ledger = futureLedger, amountApplied = apply))
                allocationDetails.add(
                    AllocationDetail(
                        type = "DUES",
                        period = "%d-%02d".format(futureLedger.periodYear, futureLedger.periodMonth),
                        fineType = null,
                        amountApplied = apply,
                        fullyPaid = futureLedger.status == LedgerStatus.PAID,
                    ),
                )
                futureMonth = futureMonth.plusMonths(1)
            }
        }

        return PaymentAllocationResult(
            paymentId = payment.id,
            houseId = house.id,
            houseDisplay = house.displayName,
            totalAmount = request.amount,
            discountApplied = discount,
            effectiveAmount = request.amount - discount,
            allocations = allocationDetails,
            remainingUnallocated = remaining,
        )
    }

    fun delete(id: Long) {
        val payment = findById(id)
        // Remove allocations first
        val allocations = paymentAllocationRepository.findByPaymentId(payment.id)
        // Reverse the allocation effects
        for (alloc in allocations) {
            alloc.ledger?.let { ledger ->
                ledger.amountPaid -= alloc.amountApplied
                ledger.status =
                    when {
                        ledger.amountPaid <= 0 -> LedgerStatus.UNPAID
                        ledger.amountPaid < ledger.amountDue -> LedgerStatus.PARTIAL
                        else -> LedgerStatus.PAID
                    }
                if (ledger.status != LedgerStatus.PAID) ledger.paidDate = null
                ledgerRepository.save(ledger)
            }
            alloc.fine?.let { fine ->
                fine.amountPaid -= alloc.amountApplied
                fine.status =
                    when {
                        fine.amountPaid <= 0 -> LedgerStatus.UNPAID
                        fine.amountPaid < fine.amount -> LedgerStatus.PARTIAL
                        else -> LedgerStatus.PAID
                    }
                fineRepository.save(fine)
            }
        }
        paymentAllocationRepository.deleteAll(allocations)
        paymentRepository.delete(payment)
    }

    private fun findById(id: Long): Payment =
        paymentRepository.findById(id).orElseThrow { ResourceNotFoundException("Payment with id $id not found") }
}
