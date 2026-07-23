package com.ems.backend.service;

import com.ems.backend.entity.ReportJob;

import java.util.List;
import java.util.Optional;

public interface ReportService {

    ReportJob createElectoralGeneralReport(String requestedBy);

    List<ReportJob> findAllReports();

    Optional<ReportJob> findById(Long id);

    ReportJob getById(Long id);
}
