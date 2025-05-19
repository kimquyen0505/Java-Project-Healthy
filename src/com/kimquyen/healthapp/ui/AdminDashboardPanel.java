// package com.kimquyen.healthapp.ui;
package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.Role;
import com.kimquyen.healthapp.model.UserData; // Nếu cần hiển thị tên Admin
import com.kimquyen.healthapp.service.AssessmentService; // Ví dụ nếu cần thống kê
import com.kimquyen.healthapp.service.QuestionService;
import com.kimquyen.healthapp.service.UserService;
import com.kimquyen.healthapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder; // Cho padding
import java.awt.*;
import java.awt.event.ActionEvent; // Import nếu dùng anonymous class
import java.awt.event.ActionListener; // Import nếu dùng anonymous class

public class AdminDashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L; // Thêm để tránh cảnh báo

    private JButton manageUsersButton;
    private JButton manageQuestionsButton;
    private JButton manageSponsorsButton; // Nếu có
    private JButton viewGlobalReportsButton; // Xem báo cáo tổng thể

    private JLabel welcomeAdminLabel;
    private JLabel statsLabel; // Để hiển thị thống kê nhanh
    // private JPanel chartPanelContainer; // Nếu admin có biểu đồ riêng

    // Tham chiếu đến MainFrame để gọi các phương thức chuyển panel
    private MainFrame mainFrame;
    // Các service cần thiết cho panel này
    private UserService userService;
    private QuestionService questionService;
    private AssessmentService assessmentService; // Ví dụ cho thống kê

    // Constructor nhận MainFrame và các service cần thiết
    public AdminDashboardPanel(MainFrame mainFrame, UserService userService,
                               QuestionService questionService, AssessmentService assessmentService) {
        this.mainFrame = mainFrame;
        this.userService = userService;
        this.questionService = questionService;
        this.assessmentService = assessmentService;

        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20)); // Thêm padding cho panel

        initComponents();
        // loadAdminStats(); // Gọi trong panelVisible()
    }

    private void initComponents() {
        // Welcome Label
        welcomeAdminLabel = new JLabel("Bảng Điều Khiển Admin", JLabel.CENTER);
        welcomeAdminLabel.setFont(new Font("Arial", Font.BOLD, 28));
        add(welcomeAdminLabel, BorderLayout.NORTH);

        // Panel cho các nút chức năng quản lý
        JPanel buttonGridPanel = new JPanel(new GridBagLayout()); // Sử dụng GridBagLayout để linh hoạt hơn
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; // Cho nút chiếm hết không gian ô
        gbc.insets = new Insets(10, 10, 10, 10); // Khoảng cách giữa các nút
        gbc.weightx = 1.0; // Cho phép co giãn theo chiều ngang
        gbc.weighty = 1.0; // Cho phép co giãn theo chiều dọc

        Font buttonFont = new Font("Arial", Font.PLAIN, 16);
        Dimension buttonSize = new Dimension(200, 80); // Kích thước đồng nhất cho các nút

        manageUsersButton = new JButton("<html><center>Quản Lý<br>Người Dùng</center></html>");
        manageUsersButton.setFont(buttonFont);
        manageUsersButton.setPreferredSize(buttonSize);
        manageUsersButton.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel(MainFrame.MANAGE_USERS_CARD); // Gọi MainFrame để chuyển panel
            }
        });
        gbc.gridx = 0; gbc.gridy = 0;
        buttonGridPanel.add(manageUsersButton, gbc);

        manageQuestionsButton = new JButton("<html><center>Quản Lý<br>Câu Hỏi</center></html>");
        manageQuestionsButton.setFont(buttonFont);
        manageQuestionsButton.setPreferredSize(buttonSize);
        manageQuestionsButton.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel(MainFrame.MANAGE_QUESTIONS_CARD);
            }
        });
        gbc.gridx = 1; gbc.gridy = 0;
        buttonGridPanel.add(manageQuestionsButton, gbc);

        manageSponsorsButton = new JButton("<html><center>Quản Lý<br>Nhà Tài Trợ</center></html>");
        manageSponsorsButton.setFont(buttonFont);
        manageSponsorsButton.setPreferredSize(buttonSize);
        manageSponsorsButton.addActionListener(e -> {
            // if (mainFrame != null) {
            //     mainFrame.showPanel(MainFrame.MANAGE_SPONSORS_CARD);
            // }
            JOptionPane.showMessageDialog(this, "Chức năng Quản lý Nhà tài trợ đang được phát triển.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
        gbc.gridx = 0; gbc.gridy = 1;
        buttonGridPanel.add(manageSponsorsButton, gbc);

        viewGlobalReportsButton = new JButton("<html><center>Xem Báo Cáo<br>Tổng Thể</center></html>");
        viewGlobalReportsButton.setFont(buttonFont);
        viewGlobalReportsButton.setPreferredSize(buttonSize);
        viewGlobalReportsButton.addActionListener(e -> {
            // if (mainFrame != null) {
            //     mainFrame.showPanel(MainFrame.GLOBAL_REPORTS_CARD);
            // }
            JOptionPane.showMessageDialog(this, "Chức năng Xem Báo cáo tổng thể đang được phát triển.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
        gbc.gridx = 1; gbc.gridy = 1;
        buttonGridPanel.add(viewGlobalReportsButton, gbc);

        add(buttonGridPanel, BorderLayout.CENTER);

        // Panel cho thống kê
        statsLabel = new JLabel("Đang tải thống kê...", JLabel.CENTER);
        statsLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        statsLabel.setBorder(BorderFactory.createTitledBorder("Thông tin chung"));
        add(statsLabel, BorderLayout.SOUTH);
    }

    private void loadAdminStats() {
        // Gọi các service để lấy thông tin thống kê
        if (userService != null && assessmentService != null) {
            try {
                int totalUsers = userService.getAllUserData().size();
                // Giả sử AssessmentService có phương thức để lấy tổng số bài đánh giá
                // int totalAssessments = assessmentService.getTotalAssessmentsCount();
                // statsLabel.setText("Tổng số người dùng: " + totalUsers + " | Tổng số bài đánh giá đã thực hiện: " + totalAssessments);
                statsLabel.setText("Tổng số người dùng: " + totalUsers);
            } catch (Exception e) {
                statsLabel.setText("Lỗi khi tải thống kê.");
                e.printStackTrace();
            }
        } else {
            statsLabel.setText("Không thể tải thống kê do service chưa sẵn sàng.");
        }
    }

    // private void loadAdminChart() {
    //     // Logic JFreeChart cho dashboard của admin (nếu có)
    // }

    /**
     * Phương thức này sẽ được gọi bởi MainFrame khi panel này được hiển thị.
     * Dùng để tải/làm mới dữ liệu cho panel.
     */
    public void panelVisible() {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn() && session.getCurrentAccount() != null && session.getCurrentAccount().getRole() == Role.ADMIN) {
            UserData adminUserData = session.getCurrentUserData();
            if (adminUserData != null) {
                welcomeAdminLabel.setText("Chào mừng Admin: " + adminUserData.getName() + "!");
            } else {
                welcomeAdminLabel.setText("Chào mừng Admin: " + session.getCurrentAccount().getUsername() + "!");
            }
        } else {
            // Trường hợp này không nên xảy ra nếu panel chỉ được hiển thị cho Admin đã đăng nhập
            welcomeAdminLabel.setText("Bảng Điều Khiển Admin (Lỗi Session)");
        }
        loadAdminStats();
        // loadAdminChart(); // Nếu có biểu đồ
        System.out.println("AdminDashboardPanel is now visible.");
    }
}