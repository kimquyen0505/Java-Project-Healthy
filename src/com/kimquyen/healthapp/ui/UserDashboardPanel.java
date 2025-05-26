package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.service.AssessmentService; // Vẫn giữ nếu MainFrame yêu cầu khi tạo UserDashboardPanel
import com.kimquyen.healthapp.util.SessionManager;
import com.kimquyen.healthapp.util.UIConstants; // Đảm bảo bạn có lớp này

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UserDashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JLabel welcomeLabel;
    private MainFrame mainFrame;
    // AssessmentService có thể không cần thiết nếu panel này không dùng nó trực tiếp
    // Tuy nhiên, constructor hiện tại yêu cầu nó.
    @SuppressWarnings("unused") // Để tránh cảnh báo "unused" nếu không dùng đến
    private AssessmentService assessmentService;


    public UserDashboardPanel(MainFrame mainFrame, AssessmentService assessmentService) {
        this.mainFrame = mainFrame;
        this.assessmentService = assessmentService;

        setBackground(UIConstants.COLOR_BACKGROUND_DARK); // Sử dụng màu từ UIConstants
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(UIConstants.INSETS_PANEL_PADDING)); // Sử dụng Insets trực tiếp

        initComponents();
    }

    private void initComponents() {
        // Welcome Label
        welcomeLabel = new JLabel("Chào mừng!", JLabel.CENTER);
        welcomeLabel.setFont(UIConstants.FONT_TITLE_LARGE);
        welcomeLabel.setForeground(UIConstants.COLOR_TEXT_DARK); // Chữ tối trên nền sáng
        welcomeLabel.setBorder(new EmptyBorder(15, 0, 30, 0));
        add(welcomeLabel, BorderLayout.NORTH);

        // Panel cho các "thẻ" chức năng dashboard
        JPanel dashboardCardsPanel = new JPanel();
        // FlowLayout để các card tự sắp xếp, có thể dùng GridLayout nếu muốn cố định số cột
        dashboardCardsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 30));
        dashboardCardsPanel.setBackground(UIConstants.COLOR_BACKGROUND_DARK); // Cùng màu nền
        dashboardCardsPanel.setBorder(new EmptyBorder(20,0,0,0));
        add(dashboardCardsPanel, BorderLayout.CENTER);

        // Thêm các thẻ chức năng
        dashboardCardsPanel.add(createDashboardCard(
                "Làm Bài Đánh Giá Mới",
                MainFrame.TAKE_ASSESSMENT_CARD
                // Bỏ đường dẫn icon nếu không dùng
        ));

        dashboardCardsPanel.add(createDashboardCard(
                "Xem Lịch Sử Đánh Giá",
                MainFrame.VIEW_HISTORY_CARD
                // Bỏ đường dẫn icon nếu không dùng
        ));
    }

    // Helper method để tạo "thẻ" dashboard (đã bỏ phần icon)
    private JPanel createDashboardCard(String title, String actionCommand) {
        JPanel card = new JPanel(new BorderLayout(10, 10)); // Giảm khoảng cách nếu không có icon
        card.setBackground(Color.WHITE); // Nền trắng cho card để nổi bật trên nền tối
        card.setPreferredSize(new Dimension(280, 120)); // Kích thước card (có thể điều chỉnh)
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_BORDER_DARK, 1), // Viền mỏng
                new EmptyBorder(20, 20, 20, 20)
        ));
        // Thử nghiệm với bo góc của FlatLaf nếu bạn đang dùng
        // card.putClientProperty("FlatLaf.style", "arc: 10");

        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'>" + title + "</div></html>", SwingConstants.CENTER);
        titleLabel.setFont(UIConstants.FONT_TITLE_MEDIUM.deriveFont(18f));
        titleLabel.setForeground(UIConstants.COLOR_TEXT_DARK); // Chữ tối trên nền card trắng
        card.add(titleLabel, BorderLayout.CENTER);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            final Color originalBg = Color.WHITE; // Nền gốc của card
            final Color hoverBg = new Color(230, 245, 255); // Màu hover nhạt (ví dụ)

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
            // Cân nhắc xử lý trường hợp session không hợp lệ, ví dụ logout
            if (mainFrame != null && (!session.isLoggedIn() || session.getCurrentAccount() == null)) {
                 mainFrame.performLogout();
            }
        }
    }

    // loadChart() không còn cần thiết nếu không có biểu đồ
    // private void loadChart() { }

    public void panelVisible() {
        loadUserData();
        // loadChart(); // Bỏ gọi loadChart
        System.out.println("UserDashboardPanel is now visible.");
    }
}