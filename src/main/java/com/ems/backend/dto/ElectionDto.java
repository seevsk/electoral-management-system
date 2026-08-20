package com.ems.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class ElectionDto {

    @NotBlank(message = "El nombre de la elección es obligatorio")
    private String name;

    private String electionType = "PRESIDENCIAL";

    @NotNull(message = "El año es obligatorio")
    @Min(value = 2026, message = "El año debe ser el actual o un año futuro")
    private Integer year;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @NotNull(message = "La fecha de cierre es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "[PA]", message = "El estado debe ser Pendiente (P) o Activo (A)")
    private String status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getElectionType() { return electionType; }
    public void setElectionType(String electionType) { this.electionType = electionType; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
