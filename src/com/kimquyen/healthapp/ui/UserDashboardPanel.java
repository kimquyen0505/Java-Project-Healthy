// package com.kimquyen.healthapp.ui;
package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.service.AssessmentService; // Vẫn cần cho constructor
import com.kimquyen.healthapp.util.SessionManager;
import com.kimquyen.healthapp.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UserDashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JLabel welcomeLabel;
    private MainFrame mainFrame;
    // AssessmentService không được sử dụng trực tiếp trong panel này nữa
    // nhưng vẫn cần truyền vào từ MainFrame, vì constructor của UserDashboardPanel yêu cầu nó.
    // Bạn có thể xem xét loại bỏ nó khỏi constructor nếu nó thực sự không cần thiết.
    private AssessmentService assessmentService;


    public UserDashboardPanel(MainFrame mainFrame, AssessmentService assessmentService) {
        this.mainFrame = mainFrame;
        this.assessmentService = assessmentService; // Gán giá trị

        // Áp dụng style Dark Mode
        setBackground(UIConstants.COLOR_BACKGROUND_DARK);
        setLayout(new BorderLayout(20, 20)); // Khoảng cách giữa các vùng

        // Sửa lỗi setBorder: Tạo EmptyBorder từ Insets
        setBorder(new EmptyBorder(
                UIConstants.INSETS_PANEL_PADDING.top,
                UIConstants.INSETS_PANEL_PADDING.left,
                UIConstants.INSETS_PANEL_PADDING.bottom,
                UIConstants.INSETS_PANEL_PADDING.right
        ));
        // HOẶC ngắn gọn hơn nếu INSETS_PANEL_PADDING là new Insets(20,20,20,20):
        // setBorder(BorderFactory.createEmptyBorder(20,20,20,20));


        initComponents();
    }

    private void initComponents() {
        // Welcome Label
        welcomeLabel = new JLabel("Chào mừng!", JLabel.CENTER);
        welcomeLabel.setFont(UIConstants.FONT_TITLE_LARGE);
        welcomeLabel.setForeground(UIConstants.COLOR_TEXT_LIGHT);
        welcomeLabel.setBorder(new EmptyBorder(15, 0, 30, 0)); // Padding trên/dưới
        add(welcomeLabel, BorderLayout.NORTH);

        // Panel cho các "thẻ" chức năng dashboard
        JPanel dashboardCardsPanel = new JPanel();
        dashboardCardsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 30)); // Khoảng cách ngang 40, dọc 30
        dashboardCardsPanel.setBackground(UIConstants.COLOR_BACKGROUND_DARK);
        dashboardCardsPanel.setBorder(new EmptyBorder(20,0,0,0)); // Padding trên cho nhóm card
        add(dashboardCardsPanel, BorderLayout.CENTER);

        // Thêm các thẻ chức năng
        dashboardCardsPanel.add(createDashboardCard(
                "Làm Bài Đánh Giá Mới",
                MainFrame.TAKE_ASSESSMENT_CARD,
                "/icons/assessment_light.png" // Ví dụ: resources/icons/assessment_light.png
        ));

        dashboardCardsPanel.add(createDashboardCard(
                "Xem Lịch Sử Đánh Giá",
                MainFrame.VIEW_HISTORY_CARD,
                "/icons/history_light.png" // Ví dụ: resources/icons/history_light.png
        ));
    }

    // Helper method để tạo "thẻ" dashboard
    private JPanel createDashboardCard(String title, String actionCommand, String iconResourcePath) {
        JPanel card = new JPanel(new BorderLayout(10, 15)); // Khoảng cách ngang, dọc giữa icon và text
        card.setBackground(UIConstants.COLOR_COMPONENT_BACKGROUND_DARK);
        card.setPreferredSize(new Dimension(280, 200)); // Kích thước tùy chỉnh cho card (có thể cao hơn chút)
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_BORDER_DARK, 1),
                new EmptyBorder(25, 25, 25, 25) // Tăng padding bên trong card
        ));
        card.putClientProperty("FlatLaf.style", "arc: 10");

        // Icon (nếu có)
        if (iconResourcePath != null && !iconResourcePath.isEmpty()) {
            try {
                java.net.URL iconURL = getClass().getResource(iconResourcePath);
                if (iconURL != null) {
                    ImageIcon icon = new ImageIcon(iconURL);
                    // Resize icon nếu muốn kích thước đồng nhất, ví dụ 64x64
                    Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                    JLabel iconLabel = new JLabel(new ImageIcon(img));
                    iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    iconLabel.setBorder(new EmptyBorder(0,0,10,0)); // Khoảng cách dưới icon
                    card.add(iconLabel, BorderLayout.NORTH); // Đặt icon ở trên
                } else {
                    System.err.println("UserDashboardPanel: Không tìm thấy resource icon: " + iconResourcePath);
                }
            } catch (Exception e) {
                System.err.println("UserDashboardPanel: Lỗi khi tải icon: " + iconResourcePath + " - " + e.getMessage());
            }
        }

        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'>" + title.replace("<br>", "<br/>") + "</div></html>", SwingConstants.CENTER);
        titleLabel.setFont(UIConstants.FONT_TITLE_MEDIUM.deriveFont(18f)); // Font to hơn chút cho user card
        titleLabel.setForeground(UIConstants.COLOR_TEXT_LIGHT);
        card.add(titleLabel, BorderLayout.CENTER); // Text ở giữa (dưới icon)

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            final Color originalBg = UIConstants.COLOR_COMPONENT_BACKGROUND_DARK;
            final Color hoverBg = UIConstants.COLOR_ACCENT_BLUE.darker();

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

    private void loadUserData() {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn() && session.getCurrentUserData() != null) {
            UserData currentUserData = session.getCurrentUserData();
            welcomeLabel.setText("Chào mừng, " + currentUserData.getName() + "!");
        } else {
            welcomeLabel.setText("Chào mừng!");
            if (mainFrame != null && (!session.isLoggedIn() || session.getCurrentAccount() == null)) {
                 mainFrame.performLogout();
            }
        }
    }

    private void loadChart() {
        // Hiện tại chưa có biểu đồ ở đây
        // System.out.println("Chức năng biểu đồ cho UserDashboardPanel đang được phát triển.");
    }

    public void panelVisible() {
        loadUserData();
        loadChart();
        System.out.println("UserDashboardPanel is now visible.");
    }
}