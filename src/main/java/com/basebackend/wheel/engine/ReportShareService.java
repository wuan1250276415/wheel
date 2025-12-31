package com.basebackend.wheel.engine;

import com.basebackend.wheel.dto.ReportData;
import com.basebackend.wheel.dto.ShareImageVO;
import com.basebackend.wheel.entity.CoupleReport;
import com.basebackend.wheel.enums.ReportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * 报告分享服务
 * 负责将报告生成为可分享的图片
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportShareService {

    private static final int IMAGE_WIDTH = 750;
    private static final int IMAGE_HEIGHT = 1334;
    private static final String DEFAULT_THEME = "default";
    
    // 主题颜色配置
    private static final Color THEME_DEFAULT_BG = new Color(255, 182, 193); // 浅粉色
    private static final Color THEME_DEFAULT_TEXT = new Color(139, 69, 79); // 深粉色
    private static final Color THEME_BLUE_BG = new Color(173, 216, 230); // 浅蓝色
    private static final Color THEME_BLUE_TEXT = new Color(25, 25, 112); // 深蓝色
    private static final Color THEME_PURPLE_BG = new Color(230, 190, 255); // 浅紫色
    private static final Color THEME_PURPLE_TEXT = new Color(75, 0, 130); // 深紫色

    /**
     * 生成分享图片
     *
     * @param report 情侣报告
     * @param theme  主题名称（default, blue, purple）
     * @return 分享图片VO，包含Base64编码的图片
     */
    public ShareImageVO generateShareImage(CoupleReport report, String theme) {
        if (report == null) {
            log.warn("报告为空，无法生成分享图片");
            return null;
        }

        String actualTheme = (theme == null || theme.isEmpty()) ? DEFAULT_THEME : theme;
        log.info("开始生成分享图片: reportId={}, theme={}", report.getId(), actualTheme);

        try {
            // 渲染报告模板
            BufferedImage image = renderTemplate(report, actualTheme);
            
            // 转换为Base64
            String base64 = imageToBase64(image);
            
            ShareImageVO vo = new ShareImageVO();
            vo.setImageBase64(base64);
            vo.setTheme(actualTheme);
            
            log.info("分享图片生成成功: reportId={}", report.getId());
            return vo;
        } catch (Exception e) {
            log.error("生成分享图片失败: reportId={}", report.getId(), e);
            return null;
        }
    }

    /**
     * 渲染报告模板
     *
     * @param report 情侣报告
     * @param theme  主题名称
     * @return 渲染后的图片
     */
    public BufferedImage renderTemplate(CoupleReport report, String theme) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 获取主题颜色
        Color bgColor = getBackgroundColor(theme);
        Color textColor = getTextColor(theme);

        // 绘制背景
        drawBackground(g2d, bgColor);

        // 绘制装饰元素
        drawDecorations(g2d, textColor);

        // 绘制报告内容
        drawReportContent(g2d, report, textColor);

        g2d.dispose();
        return image;
    }

    /**
     * 图片转Base64
     *
     * @param image 图片
     * @return Base64编码字符串
     */
    public String imageToBase64(BufferedImage image) {
        if (image == null) {
            return null;
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            log.error("图片转Base64失败", e);
            return null;
        }
    }

    /**
     * 获取背景颜色
     */
    private Color getBackgroundColor(String theme) {
        return switch (theme.toLowerCase()) {
            case "blue" -> THEME_BLUE_BG;
            case "purple" -> THEME_PURPLE_BG;
            default -> THEME_DEFAULT_BG;
        };
    }

    /**
     * 获取文字颜色
     */
    private Color getTextColor(String theme) {
        return switch (theme.toLowerCase()) {
            case "blue" -> THEME_BLUE_TEXT;
            case "purple" -> THEME_PURPLE_TEXT;
            default -> THEME_DEFAULT_TEXT;
        };
    }

    /**
     * 绘制背景
     */
    private void drawBackground(Graphics2D g2d, Color bgColor) {
        // 绘制渐变背景
        GradientPaint gradient = new GradientPaint(
                0, 0, bgColor,
                0, IMAGE_HEIGHT, bgColor.brighter()
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    /**
     * 绘制装饰元素
     */
    private void drawDecorations(Graphics2D g2d, Color color) {
        // 绘制顶部装饰线
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(50, 80, IMAGE_WIDTH - 50, 80);
        
        // 绘制底部装饰线
        g2d.drawLine(50, IMAGE_HEIGHT - 80, IMAGE_WIDTH - 50, IMAGE_HEIGHT - 80);

        // 绘制爱心装饰
        drawHeart(g2d, IMAGE_WIDTH / 2, 50, 30, color);
    }

    /**
     * 绘制爱心
     */
    private void drawHeart(Graphics2D g2d, int x, int y, int size, Color color) {
        g2d.setColor(color);
        int[] xPoints = new int[20];
        int[] yPoints = new int[20];
        
        for (int i = 0; i < 20; i++) {
            double t = Math.PI * 2 * i / 20;
            xPoints[i] = (int) (x + size * 16 * Math.pow(Math.sin(t), 3) / 16);
            yPoints[i] = (int) (y - size * (13 * Math.cos(t) - 5 * Math.cos(2 * t) 
                    - 2 * Math.cos(3 * t) - Math.cos(4 * t)) / 16);
        }
        g2d.fillPolygon(xPoints, yPoints, 20);
    }

    /**
     * 绘制报告内容
     */
    private void drawReportContent(Graphics2D g2d, CoupleReport report, Color textColor) {
        g2d.setColor(textColor);
        
        int yOffset = 150;
        int lineHeight = 60;
        int centerX = IMAGE_WIDTH / 2;

        // 绘制标题
        Font titleFont = new Font("微软雅黑", Font.BOLD, 48);
        g2d.setFont(titleFont);
        String title = getReportTitle(report.getReportType());
        drawCenteredString(g2d, title, centerX, yOffset);
        yOffset += lineHeight + 20;

        // 绘制日期范围
        Font dateFont = new Font("微软雅黑", Font.PLAIN, 24);
        g2d.setFont(dateFont);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        String dateRange = report.getStartDate().format(formatter) + " - " + report.getEndDate().format(formatter);
        drawCenteredString(g2d, dateRange, centerX, yOffset);
        yOffset += lineHeight + 40;

        // 绘制报告数据
        ReportData data = report.getReportData();
        if (data != null) {
            Font dataFont = new Font("微软雅黑", Font.PLAIN, 32);
            g2d.setFont(dataFont);

            // 认识天数
            yOffset = drawDataItem(g2d, "💕 在一起", data.getDaysTogether() + " 天", centerX, yOffset, lineHeight);

            // 转盘次数
            yOffset = drawDataItem(g2d, "🎡 转盘次数", data.getTotalSpins() + " 次", centerX, yOffset, lineHeight);

            // 最爱分类
            if (data.getFavoriteCategory() != null) {
                yOffset = drawDataItem(g2d, "❤️ 最爱分类", data.getFavoriteCategory(), centerX, yOffset, lineHeight);
            }

            // 聊天消息
            yOffset = drawDataItem(g2d, "💬 聊天消息", data.getChatMessageCount() + " 条", centerX, yOffset, lineHeight);

            // 动态数量
            yOffset = drawDataItem(g2d, "📸 甜蜜动态", data.getMomentCount() + " 条", centerX, yOffset, lineHeight);
        }

        // 绘制默契度评分
        yOffset += 40;
        drawCompatibilityScore(g2d, report.getCompatibilityScore(), 
                data != null ? data.getCompatibilityDesc() : null, centerX, yOffset, textColor);
    }

    /**
     * 绘制数据项
     */
    private int drawDataItem(Graphics2D g2d, String label, String value, int centerX, int yOffset, int lineHeight) {
        drawCenteredString(g2d, label + ": " + value, centerX, yOffset);
        return yOffset + lineHeight;
    }

    /**
     * 绘制默契度评分
     */
    private void drawCompatibilityScore(Graphics2D g2d, Integer score, String desc, 
                                         int centerX, int yOffset, Color textColor) {
        if (score == null) {
            return;
        }

        // 绘制评分圆圈
        int circleRadius = 80;
        g2d.setColor(textColor);
        g2d.setStroke(new BasicStroke(5));
        g2d.drawOval(centerX - circleRadius, yOffset, circleRadius * 2, circleRadius * 2);

        // 绘制分数
        Font scoreFont = new Font("微软雅黑", Font.BOLD, 56);
        g2d.setFont(scoreFont);
        drawCenteredString(g2d, score.toString(), centerX, yOffset + circleRadius + 20);

        // 绘制描述
        if (desc != null) {
            Font descFont = new Font("微软雅黑", Font.PLAIN, 28);
            g2d.setFont(descFont);
            drawCenteredString(g2d, "默契度: " + desc, centerX, yOffset + circleRadius * 2 + 50);
        }
    }

    /**
     * 绘制居中文字
     */
    private void drawCenteredString(Graphics2D g2d, String text, int centerX, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = centerX - fm.stringWidth(text) / 2;
        g2d.drawString(text, x, y);
    }

    /**
     * 获取报告标题
     */
    private String getReportTitle(Integer reportType) {
        if (reportType == null) {
            return "情侣报告";
        }
        return switch (reportType) {
            case 1 -> "💑 周报";
            case 2 -> "💑 月报";
            case 3 -> "💑 年报";
            default -> "💑 情侣报告";
        };
    }
}
