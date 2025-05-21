// com/kimquyen/healthapp/ui/GlobalReportsPanel.java
package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.service.AssessmentService;
import com.kimquyen.healthapp.service.UserService;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator; // Cho label trên bar chart
import org.jfree.chart.labels.StandardPieSectionLabelGenerator; // Cho label trên pie chart
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
// import org.jfree.chart.renderer.category.LineAndShapeRenderer; // Không cần nữa
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
// import org.jfree.chart.labels.CategoryItemLabelGenerator; // Có thể không cần nếu dùng Standard...

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat; // Để định dạng số và phần trăm
import java.util.ArrayList; // Cần import này
import java.util.Map;
import java.util.List;
import java.util.Collections;
// import java.util.Comparator; // Không cần cho 2 biểu đồ này nếu DAO/Service đã sắp xếp
// import java.util.LinkedHashMap; // Không cần nữa

public class GlobalReportsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;
    private AssessmentService assessmentService;
    private UserService userService;

    private JPanel chartsContainerPanel;
    private final Dimension PREFERRED_CELL_SIZE = new Dimension(450, 350);

    public GlobalReportsPanel(MainFrame mainFrame, AssessmentService assessmentService, UserService userService) {
        this.mainFrame = mainFrame;
        this.assessmentService = assessmentService;
        this.userService = userService;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        initComponents();
    }

    private void initComponents() {
        JLabel titleLabel = new JLabel("Báo Cáo Tổng Thể", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        add(titleLabel, BorderLayout.NORTH);

        chartsContainerPanel = new JPanel(new GridLayout(0, 2, 15, 15)); // Giữ 2 cột
        JScrollPane scrollPane = new JScrollPane(chartsContainerPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Quay Lại Dashboard");
        backButton.setFont(new Font("Arial", Font.PLAIN, 14));
        backButton.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel(MainFrame.ADMIN_DASHBOARD_CARD);
            }
        });
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void loadReports() {
        chartsContainerPanel.removeAll();

        if (assessmentService == null || userService == null) {
            chartsContainerPanel.add(createNoDataPanel("Lỗi: Service chưa sẵn sàng.", "Lỗi Hệ Thống"));
            chartsContainerPanel.revalidate();
            chartsContainerPanel.repaint();
            return;
        }

        // 1. Biểu đồ tròn: Phân Phối Mức Độ Rủi Ro
        DefaultPieDataset riskLevelDataset = createRiskLevelDataset();
        if (riskLevelDataset != null && riskLevelDataset.getItemCount() > 0) {
            JFreeChart riskLevelChart = ChartFactory.createPieChart(
                    "Phân Phối Mức Độ Rủi Ro", riskLevelDataset, true, true, false); // true cho legend

            PiePlot plot = (PiePlot) riskLevelChart.getPlot();
            plot.setSimpleLabels(false);
            plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 10));
            plot.setNoDataMessage("Không có dữ liệu");
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);

            StandardPieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator(
                    "{0}: {1} ({2})",
                    NumberFormat.getNumberInstance(),
                    NumberFormat.getPercentInstance()
            );
            plot.setLabelGenerator(labelGenerator);
            plot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({1})"));

            ChartPanel riskChartPanel = new ChartPanel(riskLevelChart);
            riskChartPanel.setPreferredSize(PREFERRED_CELL_SIZE);
            riskChartPanel.setBorder(BorderFactory.createTitledBorder("Mức Độ Rủi Ro"));
            chartsContainerPanel.add(riskChartPanel);
        } else {
            chartsContainerPanel.add(createNoDataPanel("Không có dữ liệu đánh giá mức độ rủi ro.", "Mức Độ Rủi Ro"));
        }

        // 2. Biểu đồ cột: Số Lượng Người Dùng Theo Nhà Tài Trợ
        DefaultCategoryDataset userBySponsorDataset = createUserBySponsorDataset();
        if (userBySponsorDataset != null && userBySponsorDataset.getColumnCount() > 0) {
            JFreeChart userBySponsorChart = ChartFactory.createBarChart(
                    "Người Dùng Theo Nhà Tài Trợ", "Nhà Tài Trợ", "Số Lượng Người Dùng",
                    userBySponsorDataset, PlotOrientation.VERTICAL,
                    true, true, false); // true cho legend

            CategoryPlot plot = userBySponsorChart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
            plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 9));

            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, new Color(79, 129, 189));
            renderer.setDrawBarOutline(true);
            // Sử dụng StandardBarPainter để tránh hiệu ứng gradient (tùy chọn)
            renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());


            // Hiển thị giá trị trên cột (Sử dụng setBase... nếu setItem... không có)
            // Thử cả hai cách nếu một trong hai báo lỗi
            try {
                 // API mới hơn (JFreeChart 1.0.14 trở lên thường có cái này)
                renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
                renderer.setBaseItemLabelsVisible(true);
            } catch (NoSuchMethodError | AbstractMethodError e) {
                // API cũ hơn có thể dùng setItemLabelGenerator cho từng series
                // Hoặc bạn có thể cần một cách khác để hiển thị label nếu phiên bản quá cũ
                System.err.println("Warning: setBaseItemLabelGenerator/Visible not found, trying older API or skipping item labels for BarChart.");
                // renderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator());
                // renderer.setItemLabelsVisible(true);
                 try { // Thử setItemLabelGenerator nếu setBaseItemLabelGenerator không có
                    renderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator());
                    renderer.setItemLabelsVisible(true);
                } catch (NoSuchMethodError | AbstractMethodError e2) {
                    System.err.println("Also failed to use setItemLabelGenerator/Visible. Item labels for BarChart might not be shown.");
                }
            }


            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

            ChartPanel userBySponsorChartPanel = new ChartPanel(userBySponsorChart);
            userBySponsorChartPanel.setPreferredSize(PREFERRED_CELL_SIZE);
            userBySponsorChartPanel.setBorder(BorderFactory.createTitledBorder("Phân Bố Người Dùng Theo Nhà Tài Trợ"));
            chartsContainerPanel.add(userBySponsorChartPanel);
        } else {
            chartsContainerPanel.add(createNoDataPanel("Không có dữ liệu người dùng theo nhà tài trợ.", "Người Dùng Theo Nhà Tài Trợ"));
        }

        // BỎ BIỂU ĐỒ ĐƯỜNG

        // Thống kê tổng số người dùng (Có thể giữ lại hoặc bỏ nếu muốn 2 biểu đồ + 2 ô trống)
        JPanel userStatsPanel = new JPanel(new GridBagLayout());
        userStatsPanel.setPreferredSize(PREFERRED_CELL_SIZE);
        userStatsPanel.setBorder(BorderFactory.createTitledBorder("Thống Kê Người Dùng Chung"));
        try {
            int totalUsers = userService.getAllUserData().size();
            JLabel totalUsersLabel = new JLabel("Tổng số người dùng: " + totalUsers, SwingConstants.CENTER);
            totalUsersLabel.setFont(new Font("Arial", Font.BOLD, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            userStatsPanel.add(totalUsersLabel, gbc);
            chartsContainerPanel.add(userStatsPanel); // Thêm vào container
        } catch (Exception e) {
            // Nếu lỗi, thêm panel thông báo lỗi thay vì panel thống kê
            chartsContainerPanel.add(createNoDataPanel("Lỗi tải thống kê người dùng.", "Thống Kê Người Dùng"));
            System.err.println("Lỗi khi lấy tổng số người dùng cho báo cáo: " + e.getMessage());
        }


        // Căn chỉnh layout (Thêm placeholder nếu cần để đủ 2x2)
        int numComponents = chartsContainerPanel.getComponentCount();
        GridLayout layout = (GridLayout) chartsContainerPanel.getLayout();
        int columns = layout.getColumns();
        int totalCells = layout.getRows() * columns; // Nếu rows = 0, thì totalCells dựa trên numComponents

        // Nếu bạn muốn luôn có 4 ô (2x2), và hiện tại có 3 components (2 chart + 1 stats)
        // thì cần 1 placeholder.
        // Nếu chỉ có 2 chart, thì cần 2 placeholder.
        // Hoặc đơn giản là để GridLayout tự điều chỉnh.
        // Ví dụ: nếu bạn chỉ muốn hiển thị 2 biểu đồ và không có gì khác,
        // và GridLayout là (0,2), nó sẽ tự thành 1 hàng 2 cột.

        if (columns > 0 ) { // Chỉ thêm placeholder nếu có cột xác định
             // Ví dụ, nếu mục tiêu là 4 ô (2 biểu đồ + 1 thống kê + 1 trống)
            int targetComponents = 4; // Hoặc 2 nếu bạn chỉ muốn 2 biểu đồ
            if (numComponents < targetComponents && numComponents % columns != 0) {
                 int placeholdersNeeded = targetComponents - numComponents;
                 for (int i = 0; i < placeholdersNeeded; i++) {
                    JPanel placeholder = new JPanel();
                    placeholder.setPreferredSize(PREFERRED_CELL_SIZE);
                    chartsContainerPanel.add(placeholder);
                }
            } else if (numComponents < targetComponents && numComponents % columns == 0 && numComponents > 0) {
                // Trường hợp có 2 components, muốn thành 4 (thêm 2 placeholder)
                int placeholdersNeeded = targetComponents - numComponents;
                 for (int i = 0; i < placeholdersNeeded; i++) {
                    JPanel placeholder = new JPanel();
                    placeholder.setPreferredSize(PREFERRED_CELL_SIZE);
                    chartsContainerPanel.add(placeholder);
                }
            }
        }


        chartsContainerPanel.revalidate();
        chartsContainerPanel.repaint();
    }

    private JPanel createNoDataPanel(String message, String title) {
        JPanel noDataPanel = new JPanel(new BorderLayout());
        noDataPanel.setPreferredSize(PREFERRED_CELL_SIZE);
        noDataPanel.setBorder(BorderFactory.createTitledBorder(title));
        JLabel noDataLabel = new JLabel(message, SwingConstants.CENTER);
        noDataLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        noDataPanel.add(noDataLabel, BorderLayout.CENTER);
        return noDataPanel;
    }

    private DefaultPieDataset createRiskLevelDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset(); // Đảm bảo import java.util.ArrayList nếu nó báo lỗi
        if (assessmentService == null) return dataset;

        Map<String, Long> riskCounts = assessmentService.getRiskLevelDistribution();
        if (riskCounts != null && !riskCounts.isEmpty()) {
            for (Map.Entry<String, Long> entry : riskCounts.entrySet()) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }
        }
        return dataset;
    }

    private DefaultCategoryDataset createUserBySponsorDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); // Đảm bảo import java.util.ArrayList
        if (userService == null) return dataset;

        Map<String, Long> userDistribution = userService.getUserDistributionBySponsor();
        if (userDistribution != null && !userDistribution.isEmpty()) {
            // Sắp xếp theo tên nhà tài trợ để thứ tự trên biểu đồ ổn định
            List<Map.Entry<String, Long>> sortedEntries = new ArrayList<>(userDistribution.entrySet());
            sortedEntries.sort(Map.Entry.comparingByKey());

            for (Map.Entry<String, Long> entry : sortedEntries) {
                dataset.addValue(entry.getValue(), "Số người dùng", entry.getKey());
            }
        }
        return dataset;
    }

    // Bỏ phương thức createAssessmentTrendDataset()

    public void panelVisible() {
        System.out.println("GlobalReportsPanel is now visible. Loading reports (2 charts version)...");
        SwingUtilities.invokeLater(this::loadReports);
    }
}