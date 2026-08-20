package com.ems.backend.repository;

import com.ems.backend.entity.ReportJob;
import com.ems.backend.entity.ReportJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportJobRepository extends JpaRepository<ReportJob, Long> {

    List<ReportJob> findAllByOrderByCreatedAtDesc();

    List<ReportJob> findByStatusOrderByCreatedAtDesc(ReportJobStatus status);
}
