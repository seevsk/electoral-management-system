package com.ems.backend.service.impl;

import com.ems.backend.service.ReportExcelGenerator;
import com.ems.backend.service.report.DistrictParticipationRow;
import com.ems.backend.service.report.ElectoralGeneralReportData;
import com.ems.backend.service.report.PresidentialResultRow;
import com.ems.backend.service.report.VoterAssignmentRow;
import com.ems.backend.service.report.VotingLocationRow;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

@Service
public class ReportExcelGeneratorImpl implements ReportExcelGenerator {

    private static final String MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Path generateElectoralGeneralReport(Long reportJobId,
                                               ElectoralGeneralReportData data,
                                               Consumer<Integer> progressUpdater) throws IOException {
        Path outputDir = Paths.get("generated-reports");
        Files.createDirectories(outputDir);

        String fileName = "reporte-electoral-general-%d-%d.xlsx".formatted(reportJobId, System.currentTimeMillis());
        Path outputFile = outputDir.resolve(fileName);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            workbook.setCompressTempFiles(true);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle bodyStyle = createBodyStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);

            writeSummarySheet(workbook, data, headerStyle, bodyStyle, numberStyle, percentStyle);
            progressUpdater.accept(20);

            writeDistrictParticipationSheet(workbook, data.districtParticipationRows(), headerStyle, bodyStyle, numberStyle, percentStyle);
            progressUpdater.accept(40);

            writePresidentialResultsSheet(workbook, data, headerStyle, bodyStyle, numberStyle, percentStyle);
            progressUpdater.accept(60);

            writeVotingLocationsSheet(workbook, data.votingLocationRows(), headerStyle, bodyStyle, numberStyle);
            progressUpdater.accept(80);

            writeVoterAssignmentsSheet(workbook, data.voterAssignmentRows(), headerStyle, bodyStyle);

            try (OutputStream os = Files.newOutputStream(outputFile)) {
                workbook.write(os);
            }
        }

        return outputFile;
    }

    private void writeSummarySheet(SXSSFWorkbook workbook,
                                   ElectoralGeneralReportData data,
                                   CellStyle headerStyle,
                                   CellStyle bodyStyle,
                                   CellStyle numberStyle,
                                   CellStyle percentStyle) {
        Sheet sheet = workbook.createSheet("Resumen General");
        if (sheet instanceof SXSSFSheet sxSheet) {
            sxSheet.trackAllColumnsForAutoSizing();
        }
        createTitleRow(sheet, "Resumen General del Reporte Electoral");

        int rowIndex = 2;
        rowIndex = addSummaryRow(sheet, rowIndex, "Elección", data.electionName(), bodyStyle);
        rowIndex = addSummaryRow(sheet, rowIndex, "Total de electores", data.summary().totalElectors(), numberStyle);
        rowIndex = addSummaryRow(sheet, rowIndex, "Total de votos emitidos", data.summary().totalVotesEmitted(), numberStyle);
        rowIndex = addSummaryRow(sheet, rowIndex, "Total de votos pendientes", data.summary().totalVotesPending(), numberStyle);
        rowIndex = addSummaryRow(sheet, rowIndex, "Porcentaje de participación", data.summary().participationPercentage(), percentStyle);
        rowIndex = addSummaryRow(sheet, rowIndex, "Total de votos registrados", data.summary().totalVotesRegistered(), numberStyle);
        rowIndex = addSummaryRow(sheet, rowIndex, "Total de votos en blanco", data.summary().totalBlankVotes(), numberStyle);
        addSummaryRow(sheet, rowIndex, "Fecha de generación", data.summary().generatedAt().format(DATE_TIME_FORMATTER), bodyStyle);

        autosize(sheet, 2);
    }

    private void writeDistrictParticipationSheet(SXSSFWorkbook workbook,
                                                 List<DistrictParticipationRow> rows,
                                                 CellStyle headerStyle,
                                                 CellStyle bodyStyle,
                                                 CellStyle numberStyle,
                                                 CellStyle percentStyle) {
        Sheet sheet = workbook.createSheet("Participación por Distrito");
        if (sheet instanceof SXSSFSheet sxSheet) {
            sxSheet.trackAllColumnsForAutoSizing();
        }
        createTableHeader(sheet, 0, headerStyle,
                "Ubigeo", "Distrito", "Total de electores", "Votos emitidos", "Votos pendientes", "Porcentaje de participación");

        int rowIndex = 1;
        for (DistrictParticipationRow row : rows) {
            Row excelRow = sheet.createRow(rowIndex++);
            createCell(excelRow, 0, row.ubigeo(), bodyStyle);
            createCell(excelRow, 1, row.district(), bodyStyle);
            createCell(excelRow, 2, row.totalElectors(), numberStyle);
            createCell(excelRow, 3, row.votesEmitted(), numberStyle);
            createCell(excelRow, 4, row.votesPending(), numberStyle);
            createCell(excelRow, 5, row.participationPercentage(), percentStyle);
        }

        autosize(sheet, 6);
    }

    private void writePresidentialResultsSheet(SXSSFWorkbook workbook,
                                               ElectoralGeneralReportData data,
                                               CellStyle headerStyle,
                                               CellStyle bodyStyle,
                                               CellStyle numberStyle,
                                               CellStyle percentStyle) {
        Sheet sheet = workbook.createSheet("Resultados Presidenciales");
        if (sheet instanceof SXSSFSheet sxSheet) {
            sxSheet.trackAllColumnsForAutoSizing();
        }
        createTableHeader(sheet, 0, headerStyle,
                "Elección", "Candidato", "Partido", "Votos", "Porcentaje");

        int rowIndex = 1;
        for (PresidentialResultRow row : data.presidentialResultRows()) {
            Row excelRow = sheet.createRow(rowIndex++);
            createCell(excelRow, 0, row.electionName(), bodyStyle);
            createCell(excelRow, 1, row.candidateName(), bodyStyle);
            createCell(excelRow, 2, row.partyName(), bodyStyle);
            createCell(excelRow, 3, row.votes(), numberStyle);
            createCell(excelRow, 4, row.percentage(), percentStyle);
        }

        autosize(sheet, 5);
    }

    private void writeVotingLocationsSheet(SXSSFWorkbook workbook,
                                           List<VotingLocationRow> rows,
                                           CellStyle headerStyle,
                                           CellStyle bodyStyle,
                                           CellStyle numberStyle) {
        Sheet sheet = workbook.createSheet("Locales y Mesas");
        if (sheet instanceof SXSSFSheet sxSheet) {
            sxSheet.trackAllColumnsForAutoSizing();
        }
        createTableHeader(sheet, 0, headerStyle,
                "Local de votación", "Dirección", "Ubigeo", "Distrito", "Cantidad de mesas", "Cantidad de votantes asignados");

        int rowIndex = 1;
        for (VotingLocationRow row : rows) {
            Row excelRow = sheet.createRow(rowIndex++);
            createCell(excelRow, 0, row.locationName(), bodyStyle);
            createCell(excelRow, 1, row.address(), bodyStyle);
            createCell(excelRow, 2, row.ubigeo(), bodyStyle);
            createCell(excelRow, 3, row.district(), bodyStyle);
            createCell(excelRow, 4, row.tableCount(), numberStyle);
            createCell(excelRow, 5, row.assignedVotersCount(), numberStyle);
        }

        autosize(sheet, 6);
    }

    private void writeVoterAssignmentsSheet(SXSSFWorkbook workbook,
                                            List<VoterAssignmentRow> rows,
                                            CellStyle headerStyle,
                                            CellStyle bodyStyle) {
        Sheet sheet = workbook.createSheet("Asignación de Votantes");
        if (sheet instanceof SXSSFSheet sxSheet) {
            sxSheet.trackAllColumnsForAutoSizing();
        }
        createTableHeader(sheet, 0, headerStyle,
                "DNI del votante", "Nombre del votante", "Local de votación", "Mesa", "Ubigeo", "Distrito");

        int rowIndex = 1;
        for (VoterAssignmentRow row : rows) {
            Row excelRow = sheet.createRow(rowIndex++);
            createCell(excelRow, 0, row.dni(), bodyStyle);
            createCell(excelRow, 1, row.voterName(), bodyStyle);
            createCell(excelRow, 2, row.locationName(), bodyStyle);
            createCell(excelRow, 3, row.tableNumber(), bodyStyle);
            createCell(excelRow, 4, row.ubigeo(), bodyStyle);
            createCell(excelRow, 5, row.district(), bodyStyle);
        }

        autosize(sheet, 6);
    }

    private void createTitleRow(Sheet sheet, String title) {
        Row titleRow = sheet.createRow(0);
        Cell cell = titleRow.createCell(0);
        cell.setCellValue(title);
    }

    private int addSummaryRow(Sheet sheet, int rowIndex, String label, Object value, CellStyle valueStyle) {
        Row row = sheet.createRow(rowIndex);
        createCell(row, 0, label, null);
        createCell(row, 1, value, valueStyle);
        return rowIndex + 1;
    }

    private void createTableHeader(Sheet sheet, int rowIndex, CellStyle headerStyle, String... headers) {
        Row headerRow = sheet.createRow(rowIndex);
        for (int i = 0; i < headers.length; i++) {
            createCell(headerRow, i, headers[i], headerStyle);
        }
    }

    private void createCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (style != null) {
            cell.setCellStyle(style);
        }

        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            return;
        }

        if (value instanceof BigDecimal decimal) {
            cell.setCellValue(decimal.doubleValue());
            return;
        }

        cell.setCellValue(value == null ? "" : String.valueOf(value));
    }

    private CellStyle createHeaderStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle createBodyStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createNumberStyle(SXSSFWorkbook workbook) {
        CellStyle style = createBodyStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat("0"));
        return style;
    }

    private CellStyle createPercentStyle(SXSSFWorkbook workbook) {
        CellStyle style = createBodyStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat("0.00"));
        return style;
    }

    private void autosize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
