package com.ems.backend.controller;

import com.ems.backend.dto.ReportJobStatusDto;
import com.ems.backend.entity.ReportJob;
import com.ems.backend.entity.ReportJobStatus;
import com.ems.backend.service.ReportService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    private static final String DEFAULT_EXCEL_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String listReports(Model model) {
        model.addAttribute("reports", reportService.findAllReports());
        return "admin/reports";
    }

    @PostMapping("/generate")
    public String generateReport(Principal principal, RedirectAttributes redirectAttributes) {
        String requestedBy = principal != null ? principal.getName() : "admin";
        ReportJob reportJob = reportService.createElectoralGeneralReport(requestedBy);
        redirectAttributes.addFlashAttribute("successMessage",
                "Reporte solicitado correctamente. ID: " + reportJob.getId());
        return "redirect:/admin/reports";
    }

    @GetMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<ReportJobStatusDto> getStatus(@PathVariable Long id) {
        return reportService.findById(id)
                .map(this::toStatusDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws Exception {
        ReportJob reportJob = reportService.getById(id);

        if (reportJob.getStatus() != ReportJobStatus.COMPLETED || reportJob.getFilePath() == null) {
            return ResponseEntity.status(409).build();
        }

        Path filePath = Paths.get(reportJob.getFilePath());
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(filePath);
        String mimeType = reportJob.getMimeType() != null ? reportJob.getMimeType() : DEFAULT_EXCEL_MIME;
        String fileName = reportJob.getFileName() != null ? reportJob.getFileName() : filePath.getFileName().toString();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    private ReportJobStatusDto toStatusDto(ReportJob reportJob) {
        return new ReportJobStatusDto(
                reportJob.getId(),
                reportJob.getStatus(),
                reportJob.getProgressPercentage(),
                reportJob.getProcessedRows(),
                reportJob.getTotalRows(),
                reportJob.getFileName(),
                reportJob.getErrorMessage()
        );
    }
}
