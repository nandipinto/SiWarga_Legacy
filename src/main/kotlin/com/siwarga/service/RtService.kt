package com.siwarga.service

import com.siwarga.domain.entity.Rt
import com.siwarga.dto.request.CreateRtRequest
import com.siwarga.dto.response.RtResponse
import com.siwarga.exception.BusinessRuleException
import com.siwarga.exception.ResourceNotFoundException
import com.siwarga.repository.RtRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RtService(
    private val rtRepository: RtRepository,
    private val rwService: RwService,
) {

    @Transactional(readOnly = true)
    fun getAll(): List<RtResponse> = rtRepository.findAll().map { RtResponse.from(it) }

    @Transactional(readOnly = true)
    fun getById(id: Long): RtResponse = RtResponse.from(findById(id))

    fun create(request: CreateRtRequest): RtResponse {
        if (rtRepository.existsByName(request.name)) {
            throw BusinessRuleException("RT with name '${request.name}' already exists")
        }
        val rw = rwService.findById(request.rwId)
        val rt = Rt(rw = rw, name = request.name, description = request.description)
        return RtResponse.from(rtRepository.save(rt))
    }

    fun update(id: Long, request: CreateRtRequest): RtResponse {
        val rt = findById(id)
        if (rt.name != request.name && rtRepository.existsByName(request.name)) {
            throw BusinessRuleException("RT with name '${request.name}' already exists")
        }
        rt.rw = rwService.findById(request.rwId)
        rt.name = request.name
        rt.description = request.description
        return RtResponse.from(rtRepository.save(rt))
    }

    fun delete(id: Long) {
        val rt = findById(id)
        rtRepository.delete(rt)
    }

    fun findById(id: Long): Rt = rtRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("RT with id $id not found") }
}
