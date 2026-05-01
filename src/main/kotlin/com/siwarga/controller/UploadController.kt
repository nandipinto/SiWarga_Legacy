package com.siwarga.controller

import com.siwarga.dto.response.CsvImportResult
import com.siwarga.service.CsvImportService
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/upload")
class UploadController(
    private val csvImportService: CsvImportService,
) {
    @PostMapping("/residents", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('ADMIN')")
    fun uploadResidents(
        @RequestParam("file") file: MultipartFile,
    ): CsvImportResult = csvImportService.importResidents(file.inputStream)

    @PostMapping("/payments", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    fun uploadPayments(
        @RequestParam("file") file: MultipartFile,
    ): CsvImportResult = csvImportService.importPayments(file.inputStream)
}
