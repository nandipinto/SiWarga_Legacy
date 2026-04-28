package com.siwarga.controller

import com.siwarga.dto.request.CreateRtRequest
import com.siwarga.dto.response.RtResponse
import com.siwarga.service.RtService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rt")
class RtController(
    private val rtService: RtService,
) {
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    fun getAll(): List<RtResponse> = rtService.getAll()

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    fun getById(
        @PathVariable id: Long,
    ): RtResponse = rtService.getById(id)

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(
        @Valid @RequestBody request: CreateRtRequest,
    ): ResponseEntity<RtResponse> = ResponseEntity.status(HttpStatus.CREATED).body(rtService.create(request))

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateRtRequest,
    ): RtResponse = rtService.update(id, request)

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        rtService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
