package com.siwarga.controller

import com.siwarga.dto.request.CreateRwRequest
import com.siwarga.dto.response.RwResponse
import com.siwarga.service.RwService
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
@RequestMapping("/rw")
class RwController(
    private val rwService: RwService,
) {
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    fun getAll(): List<RwResponse> = rwService.getAll()

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    fun getById(
        @PathVariable id: Long,
    ): RwResponse = rwService.getById(id)

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(
        @Valid @RequestBody request: CreateRwRequest,
    ): ResponseEntity<RwResponse> = ResponseEntity.status(HttpStatus.CREATED).body(rwService.create(request))

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateRwRequest,
    ): RwResponse = rwService.update(id, request)

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        rwService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
