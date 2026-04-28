package com.siwarga.controller

import com.siwarga.dto.request.CreatePaymentRequest
import com.siwarga.dto.response.PaymentAllocationResult
import com.siwarga.dto.response.PaymentResponse
import com.siwarga.service.PaymentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payments")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
class PaymentController(
    private val paymentService: PaymentService,
) {
    @GetMapping
    fun getAll(): List<PaymentResponse> = paymentService.getAll()

    @GetMapping("/house/{houseId}")
    fun getByHouse(
        @PathVariable houseId: Long,
    ): List<PaymentResponse> = paymentService.getByHouse(houseId)

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long,
    ): PaymentResponse = paymentService.getById(id)

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreatePaymentRequest,
    ): ResponseEntity<PaymentAllocationResult> = ResponseEntity.status(HttpStatus.CREATED).body(paymentService.create(request))

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        paymentService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
