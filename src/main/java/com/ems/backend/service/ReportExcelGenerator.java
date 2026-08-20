package com.ems.backend.service;

import com.ems.backend.service.report.ElectoralGeneralReportData;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public interface ReportExcelGenerator {

    Path generateElectoralGeneralReport(Long reportJobId,
                                        ElectoralGeneralReportData data,
                                        Consumer<Integer> progressUpdater) throws IOException;
}
