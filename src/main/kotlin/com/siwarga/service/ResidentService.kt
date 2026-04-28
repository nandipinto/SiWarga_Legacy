package com.siwarga.service

import com.siwarga.domain.entity.House
import com.siwarga.domain.entity.Resident
import com.siwarga.dto.request.CreateHouseRequest
import com.siwarga.dto.request.CreateResidentRequest
import com.siwarga.dto.response.HouseResponse
import com.siwarga.dto.response.ResidentResponse
import com.siwarga.exception.BusinessRuleException
import com.siwarga.exception.ResourceNotFoundException
import com.siwarga.repository.HouseRepository
import com.siwarga.repository.ResidentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ResidentService(
    private val residentRepository: ResidentRepository,
    private val houseRepository: HouseRepository,
    private val rtService: RtService
) {

    // --- Houses ---

    @Transactional(readOnly = true)
    fun getAllHouses(): List<HouseResponse> =
        houseRepository.findAll().map { HouseResponse.from(it) }

    @Transactional(readOnly = true)
    fun getHouseById(id: Long): HouseResponse = HouseResponse.from(findHouseById(id))

    fun createHouse(request: CreateHouseRequest): HouseResponse {
        val rt = rtService.findById(request.rtId)
        if (houseRepository.existsByRtIdAndBlockAndHouseNumber(rt.id, request.block, request.houseNumber)) {
            throw BusinessRuleException("House Blok ${request.block} No. ${request.houseNumber} already exists in ${rt.name}")
        }
        val house = House(rt = rt, block = request.block, houseNumber = request.houseNumber)
        return HouseResponse.from(houseRepository.save(house))
    }

    fun updateHouse(id: Long, request: CreateHouseRequest): HouseResponse {
        val house = findHouseById(id)
        val rt = rtService.findById(request.rtId)
        if ((house.rt.id != rt.id || house.block != request.block || house.houseNumber != request.houseNumber) &&
            houseRepository.existsByRtIdAndBlockAndHouseNumber(rt.id, request.block, request.houseNumber)) {
            throw BusinessRuleException("House Blok ${request.block} No. ${request.houseNumber} already exists in ${rt.name}")
        }
        house.rt = rt
        house.block = request.block
        house.houseNumber = request.houseNumber
        return HouseResponse.from(houseRepository.save(house))
    }

    fun deleteHouse(id: Long) {
        val house = findHouseById(id)
        houseRepository.delete(house)
    }

    // --- Residents ---

    @Transactional(readOnly = true)
    fun getAllResidents(): List<ResidentResponse> =
        residentRepository.findAll().map { ResidentResponse.from(it) }

    @Transactional(readOnly = true)
    fun getResidentById(id: Long): ResidentResponse = ResidentResponse.from(findResidentById(id))

    fun createResident(request: CreateResidentRequest): ResidentResponse {
        val house = findHouseById(request.houseId)
        if (residentRepository.existsByHouseId(house.id)) {
            throw BusinessRuleException("House ${house.displayName} already has a registered resident")
        }
        val resident = Resident(
            house = house,
            name = request.name,
            email = request.email,
            phone = request.phone
        )
        return ResidentResponse.from(residentRepository.save(resident))
    }

    fun updateResident(id: Long, request: CreateResidentRequest): ResidentResponse {
        val resident = findResidentById(id)
        val house = findHouseById(request.houseId)
        if (resident.house.id != house.id && residentRepository.existsByHouseId(house.id)) {
            throw BusinessRuleException("House ${house.displayName} already has a registered resident")
        }
        resident.house = house
        resident.name = request.name
        resident.email = request.email
        resident.phone = request.phone
        return ResidentResponse.from(residentRepository.save(resident))
    }

    fun deleteResident(id: Long) {
        val resident = findResidentById(id)
        residentRepository.delete(resident)
    }

    // --- Internal helpers ---

    fun findHouseById(id: Long): House = houseRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("House with id $id not found") }

    fun findResidentById(id: Long): Resident = residentRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("Resident with id $id not found") }
}
