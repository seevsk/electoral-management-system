package com.ems.backend.dto;

import com.ems.backend.entity.ReportJobStatus;

import java.math.BigDecimal;

public record ReportJobStatusDto(
        Long id,
        ReportJobStatus status,
        BigDecimal progressPercentage,
        Integer processedRows,
        Integer totalRows,
        String fileName,
        String errorMessage
) {
}
