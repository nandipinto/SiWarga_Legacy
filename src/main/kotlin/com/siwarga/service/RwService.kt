package com.siwarga.service

import com.siwarga.domain.entity.Rw
import com.siwarga.dto.request.CreateRwRequest
import com.siwarga.dto.response.RwResponse
import com.siwarga.exception.BusinessRuleException
import com.siwarga.exception.ResourceNotFoundException
import com.siwarga.repository.RwRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RwService(private val rwRepository: RwRepository) {

    @Transactional(readOnly = true)
    fun getAll(): List<RwResponse> = rwRepository.findAll().map { RwResponse.from(it) }

    @Transactional(readOnly = true)
    fun getById(id: Long): RwResponse = RwResponse.from(findById(id))

    fun create(request: CreateRwRequest): RwResponse {
        if (rwRepository.existsByName(request.name)) {
            throw BusinessRuleException("RW with name '${request.name}' already exists")
        }
        val rw = Rw(name = request.name, description = request.description)
        return RwResponse.from(rwRepository.save(rw))
    }

    fun update(id: Long, request: CreateRwRequest): RwResponse {
        val rw = findById(id)
        if (rw.name != request.name && rwRepository.existsByName(request.name)) {
            throw BusinessRuleException("RW with name '${request.name}' already exists")
        }
        rw.name = request.name
        rw.description = request.description
        return RwResponse.from(rwRepository.save(rw))
    }

    fun delete(id: Long) {
        val rw = findById(id)
        rwRepository.delete(rw)
    }

    fun findById(id: Long): Rw = rwRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("RW with id $id not found") }
}
