// package com.kimquyen.healthapp.ui;
package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.service.AssessmentService; // Import AssessmentService
import com.kimquyen.healthapp.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent; // Import nếu bạn dùng anonymous class cho ActionListener
import java.awt.event.ActionListener; // Import nếu bạn dùng anonymous class cho ActionListener

public class UserDashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L; // Thêm để tránh cảnh báo

    private JLabel welcomeLabel;
    private JButton takeAssessmentButton;
    private JButton viewHistoryButton;
    // private JPanel chartPanelContainer; // Để chứa biểu đồ (nếu có)

    // Tham chiếu đến MainFrame để gọi các phương thức chuyển panel
    private MainFrame mainFrame;
    // Service cần thiết cho panel này
    private AssessmentService assessmentService;

    // Constructor nhận MainFrame và AssessmentService
    public UserDashboardPanel(MainFrame mainFrame, AssessmentService assessmentService) {
        this.mainFrame = mainFrame;
        this.assessmentService = assessmentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Thêm padding
        initComponents();
        // loadUserData(); // Gọi trong panelVisible() để đảm bảo dữ liệu mới nhất khi panel được hiển thị
    }

    private void initComponents() {
        // Welcome Label
        welcomeLabel = new JLabel("Chào mừng!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28)); // Tăng kích thước font
        add(welcomeLabel, BorderLayout.NORTH);

        // Panel cho các nút chính
        JPanel buttonPanel = new JPanel();
        // Sử dụng GridBagLayout để căn giữa các nút và cho chúng kích thước bằng nhau nếu muốn
        buttonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Khoảng cách giữa các nút
        gbc.fill = GridBagConstraints.HORIZONTAL; // Cho nút chiếm hết chiều rộng của ô grid

        takeAssessmentButton = new JButton("Làm bài đánh giá mới");
        takeAssessmentButton.setFont(new Font("Arial", Font.PLAIN, 16));
        takeAssessmentButton.setIcon(UIManager.getIcon("FileView.fileIcon")); // Ví dụ icon
        takeAssessmentButton.setPreferredSize(new Dimension(250, 50)); // Kích thước nút
        takeAssessmentButton.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel(MainFrame.TAKE_ASSESSMENT_CARD); // Gọi MainFrame để chuyển panel
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(takeAssessmentButton, gbc);

        viewHistoryButton = new JButton("Xem lịch sử đánh giá");
        viewHistoryButton.setFont(new Font("Arial", Font.PLAIN, 16));
        viewHistoryButton.setIcon(UIManager.getIcon("FileView.historyIcon")); // Ví dụ icon (cần kiểm tra icon tồn tại)
        viewHistoryButton.setPreferredSize(new Dimension(250, 50));
        viewHistoryButton.addActionListener(e -> {
            // Logic để mở panel/dialog xem lịch sử
            // Ví dụ: if (mainFrame != null) mainFrame.showPanel("ViewHistoryPanel");
            JOptionPane.showMessageDialog(this, "Chức năng 'Xem lịch sử đánh giá' đang được phát triển.");
        });
        gbc.gridx = 0; // Cùng cột
        gbc.gridy = 1; // Hàng tiếp theo
        buttonPanel.add(viewHistoryButton, gbc);

        add(buttonPanel, BorderLayout.CENTER);

        // Panel cho biểu đồ (nếu có)
        // chartPanelContainer = new JPanel(new BorderLayout());
        // chartPanelContainer.setBorder(BorderFactory.createTitledBorder("Thống kê nhanh"));
        // chartPanelContainer.setPreferredSize(new Dimension(getWidth(), 200)); // Chiều cao cố định cho chart
        // add(chartPanelContainer, BorderLayout.SOUTH);
    }

    private void loadUserData() {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn() && session.getCurrentUserData() != null) {
            UserData currentUserData = session.getCurrentUserData();
            welcomeLabel.setText("Chào mừng, " + currentUserData.getName() + "!");
        } else {
            // Trường hợp này không nên xảy ra nếu panel chỉ được hiển thị khi đã đăng nhập
            welcomeLabel.setText("Chào mừng! (Lỗi: Không có thông tin người dùng)");
            // Có thể gọi mainFrame.performLogout() nếu session không hợp lệ
        }
    }

    private void loadChart() {
        // Logic để tạo và hiển thị JFreeChart (nếu bạn dùng)
        // Ví dụ:
        // if (assessmentService != null && SessionManager.getInstance().isLoggedIn()) {
        //     UserData currentUser = SessionManager.getInstance().getCurrentUserData();
        //     if (currentUser != null) {
        //         // DefaultPieDataset dataset = assessmentService.getRecentAssessmentSummaryForUser(currentUser.getId());
        //         // JFreeChart chart = ChartFactory.createPieChart("Kết quả đánh giá gần nhất", dataset, true, true, false);
        //         // ChartPanel chartDisplayPanel = new ChartPanel(chart);
        //         // chartPanelContainer.removeAll();
        //         // chartPanelContainer.add(chartDisplayPanel, BorderLayout.CENTER);
        //         // chartPanelContainer.revalidate();
        //         // chartPanelContainer.repaint();
        //     }
        // }
        System.out.println("Chức năng biểu đồ đang được phát triển.");
    }

    /**
     * Phương thức này sẽ được gọi bởi MainFrame khi panel này được hiển thị.
     * Dùng để tải/làm mới dữ liệu cho panel.
     */
    public void panelVisible() {
        loadUserData(); // Cập nhật lại thông tin chào mừng
        loadChart();    // Tải lại biểu đồ (nếu có)
        System.out.println("UserDashboardPanel is now visible.");
    }
}