package com.siwarga.dto.response

data class CsvImportResult(
    val totalRows: Int,
    val successCount: Int,
    val skippedCount: Int,
    val errorCount: Int,
    val rows: List<CsvRowResult>
)

data class CsvRowResult(
    val rowNumber: Int,
    val status: String,     // SUCCESS | SKIPPED | ERROR
    val message: String? = null,
    val detail: Any? = null
)
