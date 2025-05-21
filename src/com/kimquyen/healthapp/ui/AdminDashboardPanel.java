// package com.kimquyen.healthapp.ui;
package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.Role;
import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.service.AssessmentService;
import com.kimquyen.healthapp.service.QuestionService;
import com.kimquyen.healthapp.service.UserService;
import com.kimquyen.healthapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminDashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JButton manageUsersButton;
    private JButton manageQuestionsButton;
    private JButton manageSponsorsButton;
    private JButton viewGlobalReportsButton; // Nút này sẽ được cập nhật

    private JLabel welcomeAdminLabel;
    private JLabel statsLabel;

    private MainFrame mainFrame;
    private UserService userService;
    private QuestionService questionService;
    private AssessmentService assessmentService;

    public AdminDashboardPanel(MainFrame mainFrame, UserService userService,
                               QuestionService questionService, AssessmentService assessmentService) {
        this.mainFrame = mainFrame;
        this.userService = userService;
        this.questionService = questionService;
        this.assessmentService = assessmentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
    }

    private void initComponents() {
        // Welcome Label
        welcomeAdminLabel = new JLabel("Bảng Điều Khiển Admin", JLabel.CENTER);
        welcomeAdminLabel.setFont(new Font("Arial", Font.BOLD, 28));
        add(welcomeAdminLabel, BorderLayout.NORTH);

        // Panel cho các nút chức năng quản lý
        JPanel buttonGridPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        Font buttonFont = new Font("Arial", Font.PLAIN, 16);
        Dimension buttonSize = new Dimension(220, 80); // Tăng nhẹ chiều rộng để chữ hiển thị tốt hơn

        // Quản Lý Người Dùng
        manageUsersButton = new JButton("<html><center>Quản Lý<br>Người Dùng</center></html>");
        manageUsersButton.setFont(buttonFont);
        manageUsersButton.setPreferredSize(buttonSize);
        manageUsersButton.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel(MainFrame.MANAGE_USERS_CARD);
            }
        });
        gbc.gridx = 0; gbc.gridy = 0;
        buttonGridPanel.add(manageUsersButton, gbc);

        // Quản Lý Câu Hỏi
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

        // Quản Lý Nhà Tài Trợ
        manageSponsorsButton = new JButton("<html><center>Quản Lý<br>Nhà Tài Trợ</center></html>");
        manageSponsorsButton.setFont(buttonFont);
        manageSponsorsButton.setPreferredSize(buttonSize);
        manageSponsorsButton.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel(MainFrame.MANAGE_SPONSORS_CARD);
            }
        });
        gbc.gridx = 0; gbc.gridy = 1;
        buttonGridPanel.add(manageSponsorsButton, gbc);

        // Xem Báo Cáo Tổng Thể
        viewGlobalReportsButton = new JButton("<html><center>Xem Báo Cáo<br>Tổng Thể</center></html>");
        viewGlobalReportsButton.setFont(buttonFont);
        viewGlobalReportsButton.setPreferredSize(buttonSize);
        viewGlobalReportsButton.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel(MainFrame.GLOBAL_REPORTS_CARD); // <<==== SỬA Ở ĐÂY
            }
            // XÓA HOẶC COMMENT DÒNG NÀY:
            // JOptionPane.showMessageDialog(this, "Chức năng Xem Báo cáo tổng thể đang được phát triển.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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
        if (userService != null && assessmentService != null) {
            try {
                int totalUsers = userService.getAllUserData().size();
                statsLabel.setText("Tổng số người dùng: " + totalUsers);
            } catch (Exception e) {
                statsLabel.setText("Lỗi khi tải thống kê.");
                System.err.println("Lỗi khi tải thống kê admin: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            statsLabel.setText("Không thể tải thống kê do service chưa sẵn sàng.");
            System.err.println("AdminDashboardPanel: userService hoặc assessmentService là null.");
        }
    }

    public void panelVisible() {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn() && session.getCurrentAccount() != null && session.getCurrentAccount().getRole() == Role.ADMIN) {
            UserData adminUserData = session.getCurrentUserData();
            if (adminUserData != null && adminUserData.getName() != null && !adminUserData.getName().trim().isEmpty()) {
                welcomeAdminLabel.setText("Chào mừng Admin: " + adminUserData.getName() + "!");
            } else if (session.getCurrentAccount().getUsername() != null) {
                welcomeAdminLabel.setText("Chào mừng Admin: " + session.getCurrentAccount().getUsername() + "!");
            } else {
                 welcomeAdminLabel.setText("Chào mừng Admin!");
            }
        } else {
            welcomeAdminLabel.setText("Bảng Điều Khiển Admin (Lỗi Session)");
        }
        loadAdminStats();
        System.out.println("AdminDashboardPanel is now visible.");
    }
}