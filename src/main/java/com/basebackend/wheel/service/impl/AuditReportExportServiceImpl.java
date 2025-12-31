package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.dto.AuditStatisticsVO;
import com.basebackend.wheel.dto.AuditorWorkloadVO;
import com.basebackend.wheel.dto.ViolationDistributionVO;
import com.basebackend.wheel.service.AuditReportExportService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 审核报表导出服务实现
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Service
public class AuditReportExportServiceImpl implements AuditReportExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void exportToExcel(AuditStatisticsVO statistics, String startTime, String endTime, OutputStream outputStream) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);

            // 1. 概览Sheet
            createOverviewSheet(workbook, statistics, startTime, endTime, headerStyle, dataStyle, titleStyle);

            // 2. 审核员工作量Sheet
            createAuditorWorkloadSheet(workbook, statistics, headerStyle, dataStyle, titleStyle);

            // 3. 违规类型分布Sheet
            createViolationDistributionSheet(workbook, statistics, headerStyle, dataStyle, titleStyle);

            // 4. 每日审核趋势Sheet
            createDailyTrendSheet(workbook, statistics, headerStyle, dataStyle, titleStyle);

            workbook.write(outputStream);
            log.info("审核报表Excel导出成功");
        } catch (IOException e) {
            log.error("审核报表Excel导出失败: {}", e.getMessage(), e);
            throw new RuntimeException("Excel导出失败: " + e.getMessage(), e);
        }
    }


    @Override
    public void exportToPdf(AuditStatisticsVO statistics, String startTime, String endTime, OutputStream outputStream) {
        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 使用内置字体（支持基本ASCII字符）
            PdfFont font = PdfFontFactory.createFont();

            // 标题
            Paragraph title = new Paragraph("Audit Report / 审核报表")
                    .setFont(font)
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // 时间范围
            String timeRange = String.format("Time Range: %s - %s",
                    startTime != null ? startTime : "All",
                    endTime != null ? endTime : "Now");
            document.add(new Paragraph(timeRange)
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // 1. 概览统计
            addOverviewSection(document, statistics, font);

            // 2. 今日统计
            addTodaySection(document, statistics, font);

            // 3. 审核员工作量
            addAuditorWorkloadSection(document, statistics, font);

            // 4. 违规类型分布
            addViolationDistributionSection(document, statistics, font);

            document.close();
            log.info("审核报表PDF导出成功");
        } catch (IOException e) {
            log.error("审核报表PDF导出失败: {}", e.getMessage(), e);
            throw new RuntimeException("PDF导出失败: " + e.getMessage(), e);
        }
    }

    // ==================== Excel Helper Methods ====================

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void createOverviewSheet(Workbook workbook, AuditStatisticsVO statistics,
                                     String startTime, String endTime,
                                     CellStyle headerStyle, CellStyle dataStyle, CellStyle titleStyle) {
        Sheet sheet = workbook.createSheet("Overview");

        int rowNum = 0;

        // 标题
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("审核报表概览");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        // 时间范围
        Row timeRow = sheet.createRow(rowNum++);
        timeRow.createCell(0).setCellValue("统计时间范围:");
        timeRow.createCell(1).setCellValue((startTime != null ? startTime : "全部") + " - " + (endTime != null ? endTime : "至今"));

        // 生成时间
        Row genRow = sheet.createRow(rowNum++);
        genRow.createCell(0).setCellValue("报表生成时间:");
        genRow.createCell(1).setCellValue(LocalDateTime.now().format(DATE_FORMATTER));

        rowNum++; // 空行

        // 统计数据表头
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"指标", "数值", "说明"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 统计数据
        addDataRow(sheet, rowNum++, dataStyle, "总审核数", String.valueOf(statistics.getTotalAudited()), "统计期间内审核的内容总数");
        addDataRow(sheet, rowNum++, dataStyle, "通过数", String.valueOf(statistics.getApprovedCount()), "审核通过的内容数量");
        addDataRow(sheet, rowNum++, dataStyle, "拒绝数", String.valueOf(statistics.getRejectedCount()), "审核拒绝的内容数量");
        addDataRow(sheet, rowNum++, dataStyle, "待审核数", String.valueOf(statistics.getPendingCount()), "当前待审核的内容数量");
        addDataRow(sheet, rowNum++, dataStyle, "通过率", String.format("%.2f%%", statistics.getApprovalRate()), "审核通过率");
        addDataRow(sheet, rowNum++, dataStyle, "平均处理时长", formatProcessTime(statistics.getAvgProcessTime()), "平均每条内容的审核处理时长");

        // 自动调整列宽
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }


    private void createAuditorWorkloadSheet(Workbook workbook, AuditStatisticsVO statistics,
                                            CellStyle headerStyle, CellStyle dataStyle, CellStyle titleStyle) {
        Sheet sheet = workbook.createSheet("Auditor Workload");

        int rowNum = 0;

        // 标题
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("审核员工作量统计");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        rowNum++; // 空行

        // 表头
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"审核员ID", "审核总数", "通过数", "拒绝数", "通过率", "平均处理时长"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 数据
        List<AuditorWorkloadVO> workloadList = statistics.getAuditorWorkloadList();
        if (workloadList != null) {
            for (AuditorWorkloadVO workload : workloadList) {
                Row dataRow = sheet.createRow(rowNum++);
                createDataCell(dataRow, 0, String.valueOf(workload.getAuditorId()), dataStyle);
                createDataCell(dataRow, 1, String.valueOf(workload.getTotalCount()), dataStyle);
                createDataCell(dataRow, 2, String.valueOf(workload.getApprovedCount()), dataStyle);
                createDataCell(dataRow, 3, String.valueOf(workload.getRejectedCount()), dataStyle);
                createDataCell(dataRow, 4, String.format("%.2f%%", workload.getApprovalRate()), dataStyle);
                createDataCell(dataRow, 5, formatProcessTime(workload.getAvgProcessTime()), dataStyle);
            }
        }

        // 自动调整列宽
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createViolationDistributionSheet(Workbook workbook, AuditStatisticsVO statistics,
                                                   CellStyle headerStyle, CellStyle dataStyle, CellStyle titleStyle) {
        Sheet sheet = workbook.createSheet("Violation Distribution");

        int rowNum = 0;

        // 标题
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("违规类型分布");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        rowNum++; // 空行

        // 表头
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"违规类型", "数量", "占比"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 数据
        List<ViolationDistributionVO> distribution = statistics.getViolationDistribution();
        if (distribution != null) {
            for (ViolationDistributionVO item : distribution) {
                Row dataRow = sheet.createRow(rowNum++);
                createDataCell(dataRow, 0, item.getViolationTypeName(), dataStyle);
                createDataCell(dataRow, 1, String.valueOf(item.getCount()), dataStyle);
                createDataCell(dataRow, 2, String.format("%.2f%%", item.getPercentage()), dataStyle);
            }
        }

        // 自动调整列宽
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createDailyTrendSheet(Workbook workbook, AuditStatisticsVO statistics,
                                       CellStyle headerStyle, CellStyle dataStyle, CellStyle titleStyle) {
        Sheet sheet = workbook.createSheet("Daily Trend");

        int rowNum = 0;

        // 标题
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("每日审核趋势");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        rowNum++; // 空行

        // 表头
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"日期", "审核数量"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 数据
        Map<String, Long> dailyCount = statistics.getDailyCount();
        if (dailyCount != null) {
            for (Map.Entry<String, Long> entry : dailyCount.entrySet()) {
                Row dataRow = sheet.createRow(rowNum++);
                createDataCell(dataRow, 0, entry.getKey(), dataStyle);
                createDataCell(dataRow, 1, String.valueOf(entry.getValue()), dataStyle);
            }
        }

        // 自动调整列宽
        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void addDataRow(Sheet sheet, int rowNum, CellStyle style, String... values) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < values.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
            cell.setCellValue(values[i]);
            cell.setCellStyle(style);
        }
    }

    private void createDataCell(Row row, int column, String value, CellStyle style) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }


    // ==================== PDF Helper Methods ====================

    private void addOverviewSection(Document document, AuditStatisticsVO statistics, PdfFont font) {
        document.add(new Paragraph("1. Overview Statistics")
                .setFont(font)
                .setFontSize(14)
                .setBold());

        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}))
                .setWidth(UnitValue.createPercentValue(100));

        // 表头
        table.addHeaderCell(createPdfHeaderCell("Metric", font));
        table.addHeaderCell(createPdfHeaderCell("Value", font));
        table.addHeaderCell(createPdfHeaderCell("Description", font));

        // 数据行
        addPdfDataRow(table, font, "Total Audited", String.valueOf(statistics.getTotalAudited()), "Total content audited");
        addPdfDataRow(table, font, "Approved", String.valueOf(statistics.getApprovedCount()), "Content approved");
        addPdfDataRow(table, font, "Rejected", String.valueOf(statistics.getRejectedCount()), "Content rejected");
        addPdfDataRow(table, font, "Pending", String.valueOf(statistics.getPendingCount()), "Content pending review");
        addPdfDataRow(table, font, "Approval Rate", String.format("%.2f%%", statistics.getApprovalRate()), "Approval percentage");
        addPdfDataRow(table, font, "Avg Process Time", formatProcessTime(statistics.getAvgProcessTime()), "Average processing time");

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addTodaySection(Document document, AuditStatisticsVO statistics, PdfFont font) {
        document.add(new Paragraph("2. Today's Statistics")
                .setFont(font)
                .setFontSize(14)
                .setBold());

        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(createPdfHeaderCell("Metric", font));
        table.addHeaderCell(createPdfHeaderCell("Value", font));

        addPdfDataRow(table, font, "Today Audit Count", String.valueOf(statistics.getTodayAuditCount()));
        addPdfDataRow(table, font, "Today Approval Rate", String.format("%.2f%%", statistics.getTodayApprovalRate()));
        addPdfDataRow(table, font, "Today Avg Process Time", formatProcessTime(statistics.getTodayAvgProcessTime()));

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addAuditorWorkloadSection(Document document, AuditStatisticsVO statistics, PdfFont font) {
        document.add(new Paragraph("3. Auditor Workload")
                .setFont(font)
                .setFontSize(14)
                .setBold());

        List<AuditorWorkloadVO> workloadList = statistics.getAuditorWorkloadList();
        if (workloadList == null || workloadList.isEmpty()) {
            document.add(new Paragraph("No auditor workload data available.")
                    .setFont(font)
                    .setFontSize(10));
            document.add(new Paragraph("\n"));
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[]{15, 15, 15, 15, 20, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(createPdfHeaderCell("Auditor ID", font));
        table.addHeaderCell(createPdfHeaderCell("Total", font));
        table.addHeaderCell(createPdfHeaderCell("Approved", font));
        table.addHeaderCell(createPdfHeaderCell("Rejected", font));
        table.addHeaderCell(createPdfHeaderCell("Approval Rate", font));
        table.addHeaderCell(createPdfHeaderCell("Avg Time", font));

        for (AuditorWorkloadVO workload : workloadList) {
            table.addCell(createPdfDataCell(String.valueOf(workload.getAuditorId()), font));
            table.addCell(createPdfDataCell(String.valueOf(workload.getTotalCount()), font));
            table.addCell(createPdfDataCell(String.valueOf(workload.getApprovedCount()), font));
            table.addCell(createPdfDataCell(String.valueOf(workload.getRejectedCount()), font));
            table.addCell(createPdfDataCell(String.format("%.2f%%", workload.getApprovalRate()), font));
            table.addCell(createPdfDataCell(formatProcessTime(workload.getAvgProcessTime()), font));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addViolationDistributionSection(Document document, AuditStatisticsVO statistics, PdfFont font) {
        document.add(new Paragraph("4. Violation Distribution")
                .setFont(font)
                .setFontSize(14)
                .setBold());

        List<ViolationDistributionVO> distribution = statistics.getViolationDistribution();
        if (distribution == null || distribution.isEmpty()) {
            document.add(new Paragraph("No violation data available.")
                    .setFont(font)
                    .setFontSize(10));
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}))
                .setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(createPdfHeaderCell("Violation Type", font));
        table.addHeaderCell(createPdfHeaderCell("Count", font));
        table.addHeaderCell(createPdfHeaderCell("Percentage", font));

        for (ViolationDistributionVO item : distribution) {
            table.addCell(createPdfDataCell(item.getViolationTypeName(), font));
            table.addCell(createPdfDataCell(String.valueOf(item.getCount()), font));
            table.addCell(createPdfDataCell(String.format("%.2f%%", item.getPercentage()), font));
        }

        document.add(table);
    }

    private Cell createPdfHeaderCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(10).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private Cell createPdfDataCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(9))
                .setTextAlignment(TextAlignment.CENTER);
    }

    private void addPdfDataRow(Table table, PdfFont font, String... values) {
        for (String value : values) {
            table.addCell(createPdfDataCell(value, font));
        }
    }

    private String formatProcessTime(Long milliseconds) {
        if (milliseconds == null || milliseconds == 0) {
            return "0ms";
        }
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        } else if (milliseconds < 60000) {
            return String.format("%.1fs", milliseconds / 1000.0);
        } else if (milliseconds < 3600000) {
            long minutes = milliseconds / 60000;
            long seconds = (milliseconds % 60000) / 1000;
            return String.format("%dm%ds", minutes, seconds);
        } else {
            long hours = milliseconds / 3600000;
            long minutes = (milliseconds % 3600000) / 60000;
            return String.format("%dh%dm", hours, minutes);
        }
    }
}
