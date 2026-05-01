package com.siwarga.service

import com.opencsv.CSVReaderBuilder
import com.siwarga.domain.entity.House
import com.siwarga.domain.entity.Resident
import com.siwarga.dto.request.CreatePaymentRequest
import com.siwarga.dto.response.CsvImportResult
import com.siwarga.dto.response.CsvRowResult
import com.siwarga.repository.HouseRepository
import com.siwarga.repository.PaymentRepository
import com.siwarga.repository.ResidentRepository
import com.siwarga.repository.RtRepository
import com.siwarga.repository.RwRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Service
class CsvImportService(
    private val rwRepository: RwRepository,
    private val rtRepository: RtRepository,
    private val houseRepository: HouseRepository,
    private val residentRepository: ResidentRepository,
    private val paymentRepository: PaymentRepository,
    private val paymentService: PaymentService,
    private val rtService: RtService,
) {
    private val log = LoggerFactory.getLogger(CsvImportService::class.java)

    /**
     * CSV format: rw_name,rt_name,block,house_number,resident_name,email,phone
     */
    @Transactional
    fun importResidents(inputStream: InputStream): CsvImportResult {
        val reader =
            CSVReaderBuilder(InputStreamReader(inputStream, Charsets.UTF_8))
                .withSkipLines(1) // skip header
                .build()

        val rows = mutableListOf<CsvRowResult>()
        var rowNumber = 1

        reader.use { csv ->
            for (line in csv) {
                rowNumber++
                if (line.size < 5) {
                    rows.add(CsvRowResult(rowNumber, "ERROR", "Insufficient columns (expected at least 5)"))
                    continue
                }

                val rwName = line[0].trim()
                val rtName = line[1].trim()
                val block = line[2].trim()
                val houseNumber = line[3].trim()
                val residentName = line[4].trim()
                val email = line.getOrNull(5)?.trim()?.takeIf { it.isNotBlank() }
                val phone = line.getOrNull(6)?.trim()?.takeIf { it.isNotBlank() }

                if (rwName.isBlank() || rtName.isBlank() || block.isBlank() || houseNumber.isBlank() || residentName.isBlank()) {
                    rows.add(CsvRowResult(rowNumber, "ERROR", "rw_name, rt_name, block, house_number, and resident_name are required"))
                    continue
                }

                try {
                    val rw =
                        rwRepository.findByName(rwName).orElseGet {
                            rwRepository.save(
                                com.siwarga.domain.entity
                                    .Rw(name = rwName),
                            )
                        }
                    val rt =
                        rtRepository.findByName(rtName).orElseGet {
                            rtRepository.save(
                                com.siwarga.domain.entity
                                    .Rt(rw = rw, name = rtName),
                            )
                        }

                    val house =
                        houseRepository
                            .findByRtIdAndBlockAndHouseNumber(rt.id, block, houseNumber)
                            .orElseGet {
                                houseRepository.save(House(rt = rt, block = block, houseNumber = houseNumber))
                            }

                    if (residentRepository.existsByHouseId(house.id)) {
                        rows.add(CsvRowResult(rowNumber, "SKIPPED", "House ${house.displayName} already has a resident"))
                        continue
                    }

                    residentRepository.save(Resident(house = house, name = residentName, email = email, phone = phone))
                    rows.add(CsvRowResult(rowNumber, "SUCCESS", "Resident '$residentName' created for ${house.displayName}"))
                } catch (e: Exception) {
                    log.error("Error importing resident row $rowNumber", e)
                    rows.add(CsvRowResult(rowNumber, "ERROR", e.message ?: "Unexpected error"))
                }
            }
        }

        return CsvImportResult(
            totalRows = rowNumber - 1,
            successCount = rows.count { it.status == "SUCCESS" },
            skippedCount = rows.count { it.status == "SKIPPED" },
            errorCount = rows.count { it.status == "ERROR" },
            rows = rows,
        )
    }

    /**
     * CSV format: rt_name,block,house_number,amount,payment_date,receipt_number,notes
     */
    fun importPayments(inputStream: InputStream): CsvImportResult {
        val reader =
            CSVReaderBuilder(InputStreamReader(inputStream, Charsets.UTF_8))
                .withSkipLines(1)
                .build()

        val rows = mutableListOf<CsvRowResult>()
        var rowNumber = 1

        reader.use { csv ->
            for (line in csv) {
                rowNumber++
                if (line.size < 5) {
                    rows.add(CsvRowResult(rowNumber, "ERROR", "Insufficient columns (expected at least 5)"))
                    continue
                }

                val rtName = line[0].trim()
                val block = line[1].trim()
                val houseNumber = line[2].trim()
                val amountStr = line[3].trim()
                val dateStr = line[4].trim()
                val receiptNumber = line.getOrNull(5)?.trim()?.takeIf { it.isNotBlank() }
                val notes = line.getOrNull(6)?.trim()?.takeIf { it.isNotBlank() }

                if (rtName.isBlank() || block.isBlank() || houseNumber.isBlank()) {
                    rows.add(CsvRowResult(rowNumber, "ERROR", "rt_name, block, and house_number are required"))
                    continue
                }

                val amount = amountStr.toLongOrNull()
                if (amount == null || amount <= 0) {
                    rows.add(CsvRowResult(rowNumber, "ERROR", "Invalid amount: '$amountStr'"))
                    continue
                }

                val paymentDate =
                    try {
                        LocalDate.parse(dateStr)
                    } catch (e: DateTimeParseException) {
                        rows.add(CsvRowResult(rowNumber, "ERROR", "Invalid date format '$dateStr' (expected yyyy-MM-dd)"))
                        continue
                    }

                if (receiptNumber != null && paymentRepository.existsByReceiptNumber(receiptNumber)) {
                    rows.add(CsvRowResult(rowNumber, "SKIPPED", "Receipt number '$receiptNumber' already exists"))
                    continue
                }

                try {
                    val house =
                        houseRepository
                            .findByRtNameAndBlockAndHouseNumber(rtName, block, houseNumber)
                            .orElse(null)
                    if (house == null) {
                        rows.add(CsvRowResult(rowNumber, "ERROR", "House not found: $rtName / Blok $block No. $houseNumber"))
                        continue
                    }

                    val request =
                        CreatePaymentRequest(
                            houseId = house.id,
                            amount = amount,
                            paymentDate = paymentDate,
                            receiptNumber = receiptNumber,
                            notes = notes,
                        )
                    val result = paymentService.create(request)
                    rows.add(CsvRowResult(rowNumber, "SUCCESS", "Payment allocated for ${house.displayName}", result))
                } catch (e: Exception) {
                    log.error("Error importing payment row $rowNumber", e)
                    rows.add(CsvRowResult(rowNumber, "ERROR", e.message ?: "Unexpected error"))
                }
            }
        }

        return CsvImportResult(
            totalRows = rowNumber - 1,
            successCount = rows.count { it.status == "SUCCESS" },
            skippedCount = rows.count { it.status == "SKIPPED" },
            errorCount = rows.count { it.status == "ERROR" },
            rows = rows,
        )
    }
}
