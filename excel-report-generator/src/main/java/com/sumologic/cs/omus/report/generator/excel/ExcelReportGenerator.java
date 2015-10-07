package com.sumologic.cs.omus.report.generator.excel;

import com.sumologic.cs.omus.report.generator.api.OmusReportGenerationException;
import com.sumologic.cs.omus.report.generator.api.ReportConfig;
import com.sumologic.cs.omus.report.generator.api.ReportGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class ExcelReportGenerator implements ReportGenerator {

    private static final Log LOGGER = LogFactory.getLog(ExcelReportGenerator.class);

    @Autowired
    private WorkbookGenerator workbookGenerator;

    @Autowired
    private WorkbookPopulator workbookPopulator;

    @Override
    public void generateReport(ReportConfig reportConfig) throws OmusReportGenerationException {
        try {
            LOGGER.info("starting report generation");
            LOGGER.debug("using config: " + reportConfig);
            Workbook workbook = getWorkbook(reportConfig);
            workbookPopulator.populateWorkbookWithData(reportConfig, workbook);
            LOGGER.info("report successfully generated");
        } catch (IOException e) {
            throw new OmusReportGenerationException(e);
        }
    }

    private Workbook getWorkbook(ReportConfig reportConfig) throws IOException, OmusReportGenerationException {
        Workbook workbook;
        if (reportConfig.getTemplateFile() == null) {
            workbook = workbookGenerator.generateWorkbook(reportConfig);
        } else {
            workbook = copyTemplate(reportConfig);
        }
        return workbook;
    }

    private Workbook copyTemplate(ReportConfig reportConfig) throws OmusReportGenerationException {
        try {
            File srcFile = new File(reportConfig.getTemplateFile());
            File destFile = new File(reportConfig.getDestinationFile());
            FileUtils.copyFile(srcFile, destFile);
            FileInputStream fileInputStream = new FileInputStream(destFile);
            OPCPackage opc = OPCPackage.open(fileInputStream);
            return WorkbookFactory.create(opc);
        } catch (IOException | InvalidFormatException e) {
            throw new OmusReportGenerationException(e);
        }
    }

}