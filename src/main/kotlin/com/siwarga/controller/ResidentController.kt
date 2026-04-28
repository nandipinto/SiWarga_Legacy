package com.siwarga.controller

import com.siwarga.dto.request.CreateHouseRequest
import com.siwarga.dto.request.CreateResidentRequest
import com.siwarga.dto.response.HouseResponse
import com.siwarga.dto.response.ResidentResponse
import com.siwarga.service.ResidentService
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
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasRole('ADMIN')")
class ResidentController(
    private val residentService: ResidentService,
) {
    // --- Houses ---

    @GetMapping("/houses")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    fun getAllHouses(): List<HouseResponse> = residentService.getAllHouses()

    @GetMapping("/houses/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    fun getHouseById(
        @PathVariable id: Long,
    ): HouseResponse = residentService.getHouseById(id)

    @PostMapping("/houses")
    fun createHouse(
        @Valid @RequestBody request: CreateHouseRequest,
    ): ResponseEntity<HouseResponse> = ResponseEntity.status(HttpStatus.CREATED).body(residentService.createHouse(request))

    @PutMapping("/houses/{id}")
    fun updateHouse(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateHouseRequest,
    ): HouseResponse = residentService.updateHouse(id, request)

    @DeleteMapping("/houses/{id}")
    fun deleteHouse(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        residentService.deleteHouse(id)
        return ResponseEntity.noContent().build()
    }

    // --- Residents ---

    @GetMapping("/residents")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    fun getAllResidents(): List<ResidentResponse> = residentService.getAllResidents()

    @GetMapping("/residents/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    fun getResidentById(
        @PathVariable id: Long,
    ): ResidentResponse = residentService.getResidentById(id)

    @PostMapping("/residents")
    fun createResident(
        @Valid @RequestBody request: CreateResidentRequest,
    ): ResponseEntity<ResidentResponse> = ResponseEntity.status(HttpStatus.CREATED).body(residentService.createResident(request))

    @PutMapping("/residents/{id}")
    fun updateResident(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateResidentRequest,
    ): ResidentResponse = residentService.updateResident(id, request)

    @DeleteMapping("/residents/{id}")
    fun deleteResident(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        residentService.deleteResident(id)
        return ResponseEntity.noContent().build()
    }
}
