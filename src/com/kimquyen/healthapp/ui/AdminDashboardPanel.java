package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.Role;
import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.service.AssessmentService;
import com.kimquyen.healthapp.service.QuestionService;
import com.kimquyen.healthapp.service.UserService;
import com.kimquyen.healthapp.util.SessionManager;
import com.kimquyen.healthapp.util.UIConstants;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
// import javax.swing.border.TitledBorder; // Nếu muốn dùng lại TitledBorder cho statsLabel
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AdminDashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JLabel welcomeAdminLabel;
    private JLabel statsLabel;

    private MainFrame mainFrame;
    private UserService userService;
    // QuestionService và AssessmentService không còn được dùng trực tiếp trong panel này
    // nếu các chức năng của chúng được gọi qua các panel quản lý tương ứng.

    public AdminDashboardPanel(MainFrame mainFrame, UserService userService,
                               QuestionService questionService, AssessmentService assessmentService) {
        this.mainFrame = mainFrame;
        this.userService = userService;

        // Áp dụng style Dark Mode
        setBackground(UIConstants.COLOR_BACKGROUND_DARK);
        setLayout(new BorderLayout(20, 20)); // Khoảng cách giữa các vùng NORTH, CENTER, SOUTH

        // Sửa lỗi setBorder: Tạo EmptyBorder từ Insets
        setBorder(new EmptyBorder(
                UIConstants.INSETS_PANEL_PADDING.top,
                UIConstants.INSETS_PANEL_PADDING.left,
                UIConstants.INSETS_PANEL_PADDING.bottom,
                UIConstants.INSETS_PANEL_PADDING.right
        ));

        initComponents();
    }

    private void initComponents() {
        // Welcome Label
        welcomeAdminLabel = new JLabel("Admin Dashbroad", JLabel.CENTER);
        welcomeAdminLabel.setFont(UIConstants.FONT_TITLE_LARGE);
        welcomeAdminLabel.setForeground(UIConstants.COLOR_TEXT_LIGHT);
        welcomeAdminLabel.setBorder(new EmptyBorder(10, 0, 25, 0)); // Padding trên/dưới
        add(welcomeAdminLabel, BorderLayout.NORTH);

        // Panel cho các "thẻ" chức năng
        JPanel dashboardCardsPanel = new JPanel(new GridLayout(0, 2, 25, 25)); // 2 cột, khoảng cách ngang/dọc 25
        dashboardCardsPanel.setBackground(UIConstants.COLOR_BACKGROUND_DARK);
        dashboardCardsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Padding xung quanh group card
        add(dashboardCardsPanel, BorderLayout.CENTER);

        // Thêm các thẻ chức năng
        dashboardCardsPanel.add(createDashboardCard(
                "User Management",
                MainFrame.MANAGE_USERS_CARD,
                "/icons/users_light.png" // Ví dụ: resources/icons/users_light.png
        ));
        dashboardCardsPanel.add(createDashboardCard(
                "Question Management",
                MainFrame.MANAGE_QUESTIONS_CARD,
                "/icons/questions_light.png"
        ));
        dashboardCardsPanel.add(createDashboardCard(
                "Sponsor Management",
                MainFrame.MANAGE_SPONSORS_CARD,
                "/icons/sponsors_light.png"
        ));
        dashboardCardsPanel.add(createDashboardCard(
                "View Overall Report",
                MainFrame.GLOBAL_REPORTS_CARD,
                "/icons/reports_light.png"
        ));


        // Panel cho thống kê
        statsLabel = new JLabel("Loading statistics....", JLabel.CENTER);
        statsLabel.setFont(UIConstants.FONT_PRIMARY_REGULAR.deriveFont(16f));
        statsLabel.setForeground(UIConstants.COLOR_TEXT_SECONDARY_LIGHT);
        statsLabel.setBorder(new EmptyBorder(20, 0, 10, 0)); // Padding trên/dưới
        add(statsLabel, BorderLayout.SOUTH);
    }

    // Helper method để tạo "thẻ" dashboard
    private JPanel createDashboardCard(String title, String actionCommand, String iconResourcePath) {
        JPanel card = new JPanel(new BorderLayout(10, 15)); // Khoảng cách giữa icon và text (ngang, dọc)
        card.setBackground(UIConstants.COLOR_COMPONENT_BACKGROUND_DARK);
        card.setPreferredSize(new Dimension(280, 160)); // Kích thước tùy chỉnh cho card
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_BORDER_DARK, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        // Bo tròn góc (nếu FlatLaf được sử dụng và hỗ trợ)
        card.putClientProperty("FlatLaf.style", "arc: 10"); // Giá trị arc điều chỉnh độ bo tròn

       
        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'>" + title.replace("<br>", "<br/>") + "</div></html>", SwingConstants.CENTER);
        titleLabel.setFont(UIConstants.FONT_TITLE_MEDIUM.deriveFont(17f)); // Điều chỉnh font
        titleLabel.setForeground(UIConstants.COLOR_TEXT_LIGHT);
        // Nếu icon ở NORTH, text có thể ở CENTER hoặc SOUTH
        card.add(titleLabel, BorderLayout.CENTER);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            final Color originalBg = UIConstants.COLOR_COMPONENT_BACKGROUND_DARK; // Lưu màu nền gốc
            final Color hoverBg = UIConstants.COLOR_ACCENT_BLUE.darker();      // Màu khi hover

            @Override
            public void mouseClicked(MouseEvent e) {
                if (mainFrame != null) {
                    mainFrame.showPanel(actionCommand);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(originalBg);
            }
        });
        return card;
    }

    private void loadAdminStats() {
        if (userService != null) {
            try {
                // Giả sử userService.getAllUserData() trả về List<UserData>
                // và bạn muốn lấy kích thước của nó.
                // Nếu phương thức này có thể trả về null, cần kiểm tra null.
                List<UserData> allUsers = userService.getAllUserData();
                int totalUsers = (allUsers != null) ? allUsers.size() : 0;
                statsLabel.setText("Total number of users:  " + totalUsers);
            } catch (Exception e) {
                statsLabel.setText("Error loading user statistics.");
                System.err.println("AdminDashboardPanel: Error loading admin statistics: " + e.getMessage());
                e.printStackTrace(); // In stack trace để debug
            }
        } else {
            statsLabel.setText("Unable to load statistics (UserService is not ready).");
            System.err.println("AdminDashboardPanel: userService là null.");
        }
    }

    public void panelVisible() {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn() && session.getCurrentAccount() != null && session.getCurrentAccount().getRole() == Role.ADMIN) {
            UserData adminUserData = session.getCurrentUserData();
            String adminName = "Admin"; // Tên mặc định
            if (adminUserData != null && adminUserData.getName() != null && !adminUserData.getName().trim().isEmpty()) {
                adminName = adminUserData.getName();
            } else if (session.getCurrentAccount().getUsername() != null) {
                adminName = session.getCurrentAccount().getUsername();
            }
            welcomeAdminLabel.setText("Welcome " + adminName + "!");
        } else {
            welcomeAdminLabel.setText("Admin Dashboard");
            // Cân nhắc việc kiểm tra và có thể gọi mainFrame.performLogout() nếu session không hợp lệ
        }
        loadAdminStats();
        System.out.println("AdminDashboardPanel is now visible.");
    }
}