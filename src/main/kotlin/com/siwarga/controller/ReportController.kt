package com.siwarga.controller

import com.siwarga.dto.response.ArrearsStatusResponse
import com.siwarga.dto.response.HouseFinancialStandingResponse
import com.siwarga.dto.response.MonthlyReportResponse
import com.siwarga.service.ReportService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
class ReportController(
    private val reportService: ReportService,
) {
    @GetMapping("/reports/monthly")
    fun monthlyReport(
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam(required = false) rtId: Long?,
    ): MonthlyReportResponse = reportService.getMonthlyReport(year, month, rtId)

    @GetMapping("/status/arrears/{houseId}")
    fun arrearsStatus(
        @PathVariable houseId: Long,
    ): ArrearsStatusResponse = reportService.getArrearsStatus(houseId)

    @GetMapping("/houses/{houseId}/financial-standing")
    fun financialStanding(
        @PathVariable houseId: Long,
    ): HouseFinancialStandingResponse = reportService.getFinancialStanding(houseId)
}
