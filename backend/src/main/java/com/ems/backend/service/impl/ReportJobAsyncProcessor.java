package com.ems.backend.service.impl;

import com.ems.backend.entity.ReportJob;
import com.ems.backend.entity.ReportJobStatus;
import com.ems.backend.repository.ReportJobRepository;
import com.ems.backend.service.ReportDataQueryService;
import com.ems.backend.service.ReportExcelGenerator;
import com.ems.backend.service.report.ElectoralGeneralReportData;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Component
public class ReportJobAsyncProcessor {

    private final ReportJobRepository reportJobRepository;
    private final ReportDataQueryService reportDataQueryService;
    private final ReportExcelGenerator reportExcelGenerator;

    public ReportJobAsyncProcessor(ReportJobRepository reportJobRepository,
                                   ReportDataQueryService reportDataQueryService,
                                   ReportExcelGenerator reportExcelGenerator) {
        this.reportJobRepository = reportJobRepository;
        this.reportDataQueryService = reportDataQueryService;
        this.reportExcelGenerator = reportExcelGenerator;
    }

    @Async("reportTaskExecutor")
    public void processElectoralGeneralReportAsync(Long reportJobId) {
        try {
            ReportJob reportJob = reportJobRepository.findById(reportJobId)
                    .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + reportJobId));

            reportJob.setStatus(ReportJobStatus.RUNNING);
            reportJob.setStartedAt(LocalDateTime.now());
            reportJob.setProcessedRows(0);
            reportJob.setProgressPercentage(BigDecimal.ZERO);
            reportJobRepository.save(reportJob);

            ElectoralGeneralReportData data = reportDataQueryService.loadElectoralGeneralReportData();
            updateProgress(reportJob, 10);

            Path filePath = reportExcelGenerator.generateElectoralGeneralReport(
                    reportJobId,
                    data,
                    percentage -> {
                        int processedRows = Math.max(0, Math.min(reportJob.getTotalRows(), percentage));
                        reportJob.setProcessedRows(processedRows);
                        reportJob.setProgressPercentage(BigDecimal.valueOf(percentage));
                        reportJobRepository.save(reportJob);
                    }
            );

            long fileSizeBytes = java.nio.file.Files.size(filePath);

            reportJob.setProcessedRows(reportJob.getTotalRows());
            reportJob.setProgressPercentage(BigDecimal.valueOf(100));
            reportJob.setStatus(ReportJobStatus.COMPLETED);
            reportJob.setFinishedAt(LocalDateTime.now());
            reportJob.setFileName(filePath.getFileName().toString());
            reportJob.setFilePath(filePath.toAbsolutePath().toString());
            reportJob.setFileSizeBytes(fileSizeBytes);
            reportJob.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            reportJobRepository.save(reportJob);
        } catch (Exception ex) {
            markAsFailed(reportJobId, ex);
        }
    }

    private void updateProgress(ReportJob reportJob, int percentage) {
        int processedRows = Math.max(0, Math.min(reportJob.getTotalRows(), percentage));
        reportJob.setProcessedRows(processedRows);
        reportJob.setProgressPercentage(BigDecimal.valueOf(percentage));
        reportJobRepository.save(reportJob);
    }

    private void markAsFailed(Long reportJobId, Exception ex) {
        reportJobRepository.findById(reportJobId).ifPresent(reportJob -> {
            reportJob.setStatus(ReportJobStatus.FAILED);
            reportJob.setErrorMessage(ex.getMessage());
            reportJob.setFinishedAt(LocalDateTime.now());
            reportJobRepository.save(reportJob);
        });
    }
}
