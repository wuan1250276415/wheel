package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.service.StatisticsExportService;
import com.basebackend.wheel.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 统计数据导出服务实现
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Slf4j
@Service
public class StatisticsExportServiceImpl implements StatisticsExportService {

    @Autowired
    private StatisticsService statisticsService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public byte[] exportUserStatisticsToExcel(Long userId) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("用户统计");

            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 获取数据
            StatisticsService.UserStatistics stats = statisticsService.getUserStatistics(userId);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"统计项", "数值"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            rowNum = createDataRow(sheet, rowNum, "总转盘次数", String.valueOf(stats.getTotalSpins()), dataStyle);
            rowNum = createDataRow(sheet, rowNum, "今日转盘次数", String.valueOf(stats.getTodaySpins()), dataStyle);
            rowNum = createDataRow(sheet, rowNum, "本周转盘次数", String.valueOf(stats.getWeekSpins()), dataStyle);
            rowNum = createDataRow(sheet, rowNum, "本月转盘次数", String.valueOf(stats.getMonthSpins()), dataStyle);
            rowNum = createDataRow(sheet, rowNum, "连续天数", String.valueOf(stats.getConsecutiveDays()), dataStyle);
            rowNum = createDataRow(sheet, rowNum, "偏好分类", stats.getFavoriteCategory(), dataStyle);
            rowNum = createDataRow(sheet, rowNum, "平均转盘时长(ms)", String.valueOf(stats.getAvgSpinDuration()), dataStyle);

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }

            return workbookToBytes(workbook);
        } catch (Exception e) {
            log.error("导出用户统计数据失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportSpinTrendToExcel(LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("转盘趋势");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 获取数据
            List<StatisticsService.SpinTrend> trendList = statisticsService.getSpinTrend(startDate, endDate);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"日期", "转盘次数", "用户数", "情侣数"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (StatisticsService.SpinTrend trend : trendList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                Cell dateCell = row.createCell(colNum++);
                dateCell.setCellValue(trend.getDate());
                dateCell.setCellStyle(dataStyle);

                Cell spinCell = row.createCell(colNum++);
                spinCell.setCellValue(trend.getSpinCount());
                spinCell.setCellStyle(dataStyle);

                Cell userCell = row.createCell(colNum++);
                userCell.setCellValue(trend.getUserCount());
                userCell.setCellStyle(dataStyle);

                Cell coupleCell = row.createCell(colNum++);
                coupleCell.setCellValue(trend.getCoupleCount());
                coupleCell.setCellStyle(dataStyle);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }

            return workbookToBytes(workbook);
        } catch (Exception e) {
            log.error("导出转盘趋势数据失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportPopularContentToExcel(Integer limit) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("热门内容");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 获取数据
            List<StatisticsService.PopularContent> contentList = statisticsService.getPopularContent(limit);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"排名", "内容文本", "分类", "转盘次数", "中奖率(%)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            int rank = 1;
            for (StatisticsService.PopularContent content : contentList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                Cell rankCell = row.createCell(colNum++);
                rankCell.setCellValue(rank++);
                rankCell.setCellStyle(dataStyle);

                Cell textCell = row.createCell(colNum++);
                textCell.setCellValue(content.getContentText());
                textCell.setCellStyle(dataStyle);

                Cell categoryCell = row.createCell(colNum++);
                categoryCell.setCellValue(content.getCategoryName());
                categoryCell.setCellStyle(dataStyle);

                Cell spinCell = row.createCell(colNum++);
                spinCell.setCellValue(content.getSpinCount());
                spinCell.setCellStyle(dataStyle);

                Cell winRateCell = row.createCell(colNum++);
                winRateCell.setCellValue(String.format("%.2f", content.getWinRate()));
                winRateCell.setCellStyle(dataStyle);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }

            return workbookToBytes(workbook);
        } catch (Exception e) {
            log.error("导出热门内容数据失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportCategoryStatisticsToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("分类统计");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 获取数据
            List<StatisticsService.CategoryStatistics> statsList = statisticsService.getCategoryStatistics();

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"分类名称", "内容数量", "总转盘次数", "平均权重", "独立用户数"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (StatisticsService.CategoryStatistics stats : statsList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                Cell nameCell = row.createCell(colNum++);
                nameCell.setCellValue(stats.getCategoryName());
                nameCell.setCellStyle(dataStyle);

                Cell countCell = row.createCell(colNum++);
                countCell.setCellValue(stats.getContentCount());
                countCell.setCellStyle(dataStyle);

                Cell spinCell = row.createCell(colNum++);
                spinCell.setCellValue(stats.getTotalSpins());
                spinCell.setCellStyle(dataStyle);

                Cell avgWeightCell = row.createCell(colNum++);
                avgWeightCell.setCellValue(String.format("%.2f", stats.getAvgWeight()));
                avgWeightCell.setCellStyle(dataStyle);

                Cell userCell = row.createCell(colNum++);
                userCell.setCellValue(stats.getUniqueUsers());
                userCell.setCellStyle(dataStyle);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }

            return workbookToBytes(workbook);
        } catch (Exception e) {
            log.error("导出分类统计数据失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportAllStatisticsToExcel(Long userId, LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 1. 用户统计Sheet
            createUserStatisticsSheet(workbook, userId, headerStyle, dataStyle);

            // 2. 转盘趋势Sheet
            createSpinTrendSheet(workbook, startDate, endDate, headerStyle, dataStyle);

            // 3. 热门内容Sheet
            createPopularContentSheet(workbook, 10, headerStyle, dataStyle);

            // 4. 分类统计Sheet
            createCategoryStatisticsSheet(workbook, headerStyle, dataStyle);

            return workbookToBytes(workbook);
        } catch (Exception e) {
            log.error("导出全部统计数据失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        return style;
    }

    /**
     * 创建数据行
     */
    private int createDataRow(Sheet sheet, int rowNum, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowNum);

        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(style);

        return rowNum + 1;
    }

    /**
     * 创建用户统计Sheet
     */
    private void createUserStatisticsSheet(Workbook workbook, Long userId, CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("用户统计");
        StatisticsService.UserStatistics stats = statisticsService.getUserStatistics(userId);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"统计项", "数值"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        rowNum = createDataRow(sheet, rowNum, "总转盘次数", String.valueOf(stats.getTotalSpins()), dataStyle);
        rowNum = createDataRow(sheet, rowNum, "今日转盘次数", String.valueOf(stats.getTodaySpins()), dataStyle);
        rowNum = createDataRow(sheet, rowNum, "本周转盘次数", String.valueOf(stats.getWeekSpins()), dataStyle);
        rowNum = createDataRow(sheet, rowNum, "本月转盘次数", String.valueOf(stats.getMonthSpins()), dataStyle);
        rowNum = createDataRow(sheet, rowNum, "连续天数", String.valueOf(stats.getConsecutiveDays()), dataStyle);
        rowNum = createDataRow(sheet, rowNum, "偏好分类", stats.getFavoriteCategory(), dataStyle);

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
    }

    /**
     * 创建转盘趋势Sheet
     */
    private void createSpinTrendSheet(Workbook workbook, LocalDate startDate, LocalDate endDate, CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("转盘趋势");
        List<StatisticsService.SpinTrend> trendList = statisticsService.getSpinTrend(startDate, endDate);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"日期", "转盘次数", "用户数", "情侣数"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (StatisticsService.SpinTrend trend : trendList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(trend.getDate());
            row.createCell(1).setCellValue(trend.getSpinCount());
            row.createCell(2).setCellValue(trend.getUserCount());
            row.createCell(3).setCellValue(trend.getCoupleCount());

            for (int i = 0; i < 4; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建热门内容Sheet
     */
    private void createPopularContentSheet(Workbook workbook, Integer limit, CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("热门内容");
        List<StatisticsService.PopularContent> contentList = statisticsService.getPopularContent(limit);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"排名", "内容文本", "分类", "转盘次数", "中奖率(%)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        int rank = 1;
        for (StatisticsService.PopularContent content : contentList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rank++);
            row.createCell(1).setCellValue(content.getContentText());
            row.createCell(2).setCellValue(content.getCategoryName());
            row.createCell(3).setCellValue(content.getSpinCount());
            row.createCell(4).setCellValue(String.format("%.2f", content.getWinRate()));

            for (int i = 0; i < 5; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
    }

    /**
     * 创建分类统计Sheet
     */
    private void createCategoryStatisticsSheet(Workbook workbook, CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("分类统计");
        List<StatisticsService.CategoryStatistics> statsList = statisticsService.getCategoryStatistics();

        Row headerRow = sheet.createRow(0);
        String[] headers = {"分类名称", "内容数量", "总转盘次数", "平均权重", "独立用户数"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (StatisticsService.CategoryStatistics stats : statsList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stats.getCategoryName());
            row.createCell(1).setCellValue(stats.getContentCount());
            row.createCell(2).setCellValue(stats.getTotalSpins());
            row.createCell(3).setCellValue(String.format("%.2f", stats.getAvgWeight()));
            row.createCell(4).setCellValue(stats.getUniqueUsers());

            for (int i = 0; i < 5; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
    }

    /**
     * 将Workbook转换为字节数组
     */
    private byte[] workbookToBytes(Workbook workbook) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
