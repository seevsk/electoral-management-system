package com.ems.backend.service.impl;

import com.ems.backend.entity.ReportJob;
import com.ems.backend.entity.ReportJobStatus;
import com.ems.backend.entity.ReportType;
import com.ems.backend.repository.ReportJobRepository;
import com.ems.backend.service.ReportService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportJobRepository reportJobRepository;
    private final ReportJobAsyncProcessor reportJobAsyncProcessor;

    public ReportServiceImpl(ReportJobRepository reportJobRepository,
                             ReportJobAsyncProcessor reportJobAsyncProcessor) {
        this.reportJobRepository = reportJobRepository;
        this.reportJobAsyncProcessor = reportJobAsyncProcessor;
    }

    @Override
    @Transactional
    public ReportJob createElectoralGeneralReport(String requestedBy) {
        ReportJob reportJob = new ReportJob();
        reportJob.setReportType(ReportType.ELECTORAL_GENERAL);
        reportJob.setStatus(ReportJobStatus.PENDING);
        reportJob.setRequestedBy(requestedBy);
        reportJob.setTotalRows(100);
        reportJob.setProcessedRows(0);
        reportJob.setProgressPercentage(BigDecimal.ZERO);
        reportJob.setCreatedAt(LocalDateTime.now());

        ReportJob saved = reportJobRepository.save(reportJob);
        reportJobAsyncProcessor.processElectoralGeneralReportAsync(saved.getId());
        return saved;
    }

    @Override
    public List<ReportJob> findAllReports() {
        return reportJobRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Optional<ReportJob> findById(Long id) {
        return reportJobRepository.findById(id);
    }

    @Override
    public ReportJob getById(Long id) {
        return reportJobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado con id: " + id));
    }
}
