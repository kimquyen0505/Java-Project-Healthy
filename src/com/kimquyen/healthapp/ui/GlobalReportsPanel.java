// package com.kimquyen.healthapp.ui;
package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.service.AssessmentService;
import com.kimquyen.healthapp.service.QuestionService;
import com.kimquyen.healthapp.service.UserService;
// import com.kimquyen.healthapp.util.UIConstants; // Giả sử UIConstants nằm ở package util

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import com.kimquyen.healthapp.model.HraQuestion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

// LỚP UIConstants GIẢ ĐỊNH
class UIConstants {
    public static final Color COLOR_BACKGROUND_DARK = new Color(235, 235, 235);
    public static final Color COLOR_BORDER_DARK = new Color(200, 200, 200);
    public static final Color COLOR_TEXT_DARK = new Color(30, 30, 30);
    public static final Color COLOR_ACCENT_BLUE = new Color(0, 120, 215);
    public static final Font FONT_PRIMARY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_TITLE_LARGE = new Font("Segoe UI Semibold", Font.BOLD, 28);
    public static final Font FONT_TITLE_MEDIUM = new Font("Segoe UI", Font.BOLD, 18);
}


public class GlobalReportsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;
    private AssessmentService assessmentService;
    private UserService userService;
    private QuestionService questionService;

    private JPanel chartsContainerPanel;
    private final Dimension PREFERRED_CELL_SIZE = new Dimension(800, 700);

    public GlobalReportsPanel(MainFrame mainFrame, AssessmentService assessmentService, UserService userService, QuestionService questionService) {
        this.mainFrame = mainFrame;
        this.assessmentService = assessmentService;
        this.userService = userService;
        this.questionService = questionService;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(UIConstants.COLOR_BACKGROUND_DARK);
        initComponents();
    }

    private void initComponents() {
        JLabel titleLabel = new JLabel("Báo Cáo Tổng Thể", SwingConstants.CENTER);
        titleLabel.setFont(UIConstants.FONT_TITLE_LARGE.deriveFont(22f));
        titleLabel.setForeground(UIConstants.COLOR_TEXT_DARK);
        add(titleLabel, BorderLayout.NORTH);

        chartsContainerPanel = new JPanel(new GridLayout(0, 1, 15, 15));
        chartsContainerPanel.setBackground(UIConstants.COLOR_BACKGROUND_DARK);
        JScrollPane scrollPane = new JScrollPane(chartsContainerPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UIConstants.COLOR_BACKGROUND_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Quay Lại Dashboard");
        backButton.setFont(UIConstants.FONT_PRIMARY_BOLD);
        backButton.setBackground(UIConstants.COLOR_ACCENT_BLUE);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(8,15,8,15));
        backButton.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel(MainFrame.ADMIN_DASHBOARD_CARD);
            }
        });
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBackground(UIConstants.COLOR_BACKGROUND_DARK);
        southPanel.setBorder(new EmptyBorder(5,0,5,5));
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void loadReports() {
        chartsContainerPanel.removeAll();

        if (assessmentService == null || userService == null || questionService == null) {
            chartsContainerPanel.add(createNoDataPanel("Lỗi: Một hoặc nhiều Service chưa sẵn sàng.", "Lỗi Hệ Thống"));
            revalidateAndRepaintContainer();
            return;
        }

        // 1. Biểu đồ tròn: Phân Phối Mức Độ Rủi Ro
        DefaultPieDataset riskLevelDataset = createRiskLevelDataset();
        if (riskLevelDataset != null && riskLevelDataset.getItemCount() > 0) {
            JFreeChart riskLevelChart = ChartFactory.createPieChart(
                    "Phân Phối Mức Độ Rủi Ro", riskLevelDataset, true, true, false);
            configurePiePlot((PiePlot) riskLevelChart.getPlot());
            ChartPanel riskChartPanel = createStyledChartPanel(riskLevelChart, "Mức Độ Rủi Ro");
            chartsContainerPanel.add(riskChartPanel);
        } else {
            chartsContainerPanel.add(createNoDataPanel("Không có dữ liệu đánh giá mức độ rủi ro.", "Mức Độ Rủi Ro"));
        }

        // 2. Biểu đồ tròn: Phân Phối Câu Trả Lời cho Câu Hỏi ID = 1
        int questionIdForDist = 1;
        DefaultPieDataset responseDistDataset = createResponseDistributionForQuestionIdDataset(questionIdForDist);
        if (responseDistDataset != null && responseDistDataset.getItemCount() > 0) {
            String chartTitle = getChartTitleForQuestionDistribution(questionIdForDist);
            JFreeChart responseDistChart = ChartFactory.createPieChart(chartTitle, responseDistDataset, true, true, false);
            configurePiePlot((PiePlot) responseDistChart.getPlot());
            ChartPanel responseDistChartPanel = createStyledChartPanel(responseDistChart, "Phân Tích Câu Trả Lời (Câu ID " + questionIdForDist + ")");
            chartsContainerPanel.add(responseDistChartPanel);
        } else {
            chartsContainerPanel.add(createNoDataPanel("Không có dữ liệu trả lời cho câu hỏi ID: " + questionIdForDist + ".", "Phân Tích Câu Trả Lời (Câu ID " + questionIdForDist + ")"));
        }

        // 3. Biểu đồ cột: Số Lượng Người Dùng Theo Nhà Tài Trợ
        DefaultCategoryDataset userBySponsorDataset = createUserBySponsorDataset();
        if (userBySponsorDataset != null && userBySponsorDataset.getColumnCount() > 0) {
            JFreeChart userBySponsorChart = ChartFactory.createBarChart(
                    "Người Dùng Theo Nhà Tài Trợ", "Nhà Tài Trợ", "Số Lượng Người Dùng",
                    userBySponsorDataset, PlotOrientation.VERTICAL, true, true, false);
            configureBarPlot((CategoryPlot) userBySponsorChart.getPlot());
            ChartPanel userBySponsorChartPanel = createStyledChartPanel(userBySponsorChart, "Phân Bố Người Dùng Theo Nhà Tài Trợ");
            chartsContainerPanel.add(userBySponsorChartPanel);
        } else {
            chartsContainerPanel.add(createNoDataPanel("Không có dữ liệu người dùng theo nhà tài trợ.", "Người Dùng Theo Nhà Tài Trợ"));
        }

        // 4. Panel Thống Kê Chung
        JPanel statsSummaryPanel = createStatsSummaryPanel();
        chartsContainerPanel.add(statsSummaryPanel);

        fillEmptyCellsInGrid();
        revalidateAndRepaintContainer();
    }

    private void revalidateAndRepaintContainer() {
        chartsContainerPanel.revalidate();
        chartsContainerPanel.repaint();
    }

    private void fillEmptyCellsInGrid() {
        int numComponents = chartsContainerPanel.getComponentCount();
        GridLayout layout = (GridLayout) chartsContainerPanel.getLayout();
        int columns = layout.getColumns();
        if (columns > 0 && numComponents > 0 && numComponents % columns != 0) {
            int placeholdersNeeded = columns - (numComponents % columns);
            for (int i = 0; i < placeholdersNeeded; i++) {
                JPanel placeholder = new JPanel();
                placeholder.setPreferredSize(PREFERRED_CELL_SIZE);
                placeholder.setBackground(UIConstants.COLOR_BACKGROUND_DARK);
                chartsContainerPanel.add(placeholder);
            }
        }
    }

    private JPanel createStatsSummaryPanel() {
        JPanel statsSummaryPanel = new JPanel(new GridBagLayout());
        statsSummaryPanel.setPreferredSize(PREFERRED_CELL_SIZE);
        stylePanel(statsSummaryPanel, "Thống Kê Chung");
        try {
            int totalUsers = 0;
            if (userService != null && userService.getAllUserData() != null) {
                totalUsers = userService.getAllUserData().size();
            }

            long totalAssessments = 0;
            if (assessmentService != null) {
                Map<String, Long> assessmentCounts = assessmentService.getAssessmentCountByMonth();
                if (assessmentCounts != null) {
                     for(long count : assessmentCounts.values()){ totalAssessments += count; }
                }
            }
            addStatLabel(statsSummaryPanel, "Tổng số người dùng: " + totalUsers, 0);
            addStatLabel(statsSummaryPanel, "Tổng số bài đánh giá sau ngày 1/1/2025: " + totalAssessments, 1);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 2; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.VERTICAL;
            statsSummaryPanel.add(Box.createVerticalGlue(), gbc);

        } catch (Exception e) {
            statsSummaryPanel.removeAll();
            statsSummaryPanel.setLayout(new BorderLayout());
            JLabel errorLabel = new JLabel("Lỗi tải thống kê.", SwingConstants.CENTER);
            errorLabel.setFont(UIConstants.FONT_PRIMARY_BOLD.deriveFont(Font.ITALIC, 14f));
            errorLabel.setForeground(UIConstants.COLOR_TEXT_DARK);
            statsSummaryPanel.add(errorLabel, BorderLayout.CENTER);
            System.err.println("Lỗi khi lấy thống kê cho báo cáo: " + e.getMessage());
        }
        return statsSummaryPanel;
    }

    private void addStatLabel(JPanel panel, String text, int gridy) {
        JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.setFont(UIConstants.FONT_TITLE_MEDIUM.deriveFont(16f));
        label.setForeground(UIConstants.COLOR_TEXT_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = gridy;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10,10,(gridy == 0 ? 5 : 10),10);
        gbc.weightx = 1.0;
        panel.add(label, gbc);
    }

    private ChartPanel createStyledChartPanel(JFreeChart chart, String borderTitle) {
        chart.setBackgroundPaint(Color.WHITE);
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(Color.WHITE);
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 10));
        }
        if (chart.getTitle() != null) {
            chart.getTitle().setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
            chart.getTitle().setPaint(UIConstants.COLOR_TEXT_DARK);
        }
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(PREFERRED_CELL_SIZE);
        stylePanel(chartPanel, borderTitle);
        return chartPanel;
    }

     private void stylePanel(JPanel panel, String title) {
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UIConstants.COLOR_BORDER_DARK), title,
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            UIConstants.FONT_PRIMARY_BOLD.deriveFont(13f),
            UIConstants.COLOR_TEXT_DARK)
        );
        panel.setBackground(Color.WHITE);
    }

    private void configurePiePlot(PiePlot plot) {
        plot.setSimpleLabels(false);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.setNoDataMessage("Không có dữ liệu");
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setSectionOutlinesVisible(false);
        plot.setLabelGap(0.02);
        StandardPieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})", NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance());
        plot.setLabelGenerator(labelGenerator);
        plot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({1})"));
        plot.setShadowPaint(null);
    }

    private void configureBarPlot(CategoryPlot plot) {
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 9));
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        plot.setOutlineVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, UIConstants.COLOR_ACCENT_BLUE);
        renderer.setDrawBarOutline(false);
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);

        try {
            renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
            renderer.setBaseItemLabelsVisible(true);
            renderer.setBaseItemLabelFont(new Font("Segoe UI", Font.PLAIN, 9));
        } catch (Exception e) {
            System.err.println("Không thể set item label cho BarChart: " + e.getMessage());
        }
        CategoryAxis domainAxis = plot.getDomainAxis();
        if (plot.getDataset() != null && plot.getDataset().getColumnCount() > 5) {
             domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
    }

    private String getChartTitleForQuestionDistribution(int questionId) {
        String chartTitle = "Phân Phối Trả Lời (Câu ID: " + questionId + ")";
        if (questionService != null) {
            HraQuestion specificQuestion = questionService.getQuestionById(questionId);
            if (specificQuestion != null) {
                String title = specificQuestion.getTitle();
                String text = specificQuestion.getText();
                if (title != null && !title.isEmpty()) {
                    chartTitle = "Phân Phối: " + title;
                } else if (text != null) {
                    String shortText = text.length() > 40 ? text.substring(0, 37) + "..." : text;
                    chartTitle = "Phân Phối: " + shortText;
                }
            }
        }
        return chartTitle;
    }

    private DefaultPieDataset createRiskLevelDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        if (assessmentService == null) {
            System.err.println("Dataset Creation: AssessmentService is null for risk level distribution.");
            return dataset;
        }
        Map<String, Long> riskCounts = assessmentService.getRiskLevelDistribution();
        if (riskCounts != null && !riskCounts.isEmpty()) {
            for (Map.Entry<String, Long> entry : riskCounts.entrySet()) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }
        } else {
            System.out.println("Dataset Creation: No risk level distribution data found.");
        }
        return dataset;
    }

    private JPanel createNoDataPanel(String message, String title) {
        JPanel noDataPanel = new JPanel(new BorderLayout());
        stylePanel(noDataPanel, title);
        noDataPanel.setPreferredSize(PREFERRED_CELL_SIZE);
        JLabel noDataLabel = new JLabel(message, SwingConstants.CENTER);
        noDataLabel.setFont(UIConstants.FONT_PRIMARY_BOLD.deriveFont(Font.ITALIC, 14f));
        noDataLabel.setForeground(UIConstants.COLOR_TEXT_DARK);
        noDataPanel.add(noDataLabel, BorderLayout.CENTER);
        return noDataPanel;
    }

    private DefaultPieDataset createResponseDistributionForQuestionIdDataset(int questionId) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        if (assessmentService == null) return dataset;

        Map<String, Long> distribution = assessmentService.getResponseDistributionForQuestion(questionId);
        if (distribution != null && !distribution.isEmpty()) {
            for (Map.Entry<String, Long> entry : distribution.entrySet()) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }
        }
        return dataset;
    }

    private DefaultCategoryDataset createUserBySponsorDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (userService == null) return dataset;

        Map<String, Long> userDistribution = userService.getUserDistributionBySponsor();
        if (userDistribution != null && !userDistribution.isEmpty()) {
            List<Map.Entry<String, Long>> sortedEntries = new ArrayList<>(userDistribution.entrySet());
            sortedEntries.sort(Map.Entry.comparingByKey());

            for (Map.Entry<String, Long> entry : sortedEntries) {
                dataset.addValue(entry.getValue(), "Số người dùng", entry.getKey());
            }
        }
        return dataset;
    }

    public void panelVisible() {
        System.out.println("GlobalReportsPanel is now visible. Loading 2 pie charts, 1 bar chart + stats...");
        SwingUtilities.invokeLater(this::loadReports);
    }
}