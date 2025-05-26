// File: com/kimquyen/healthapp/ui/MainFrame.java
package com.kimquyen.healthapp.ui;

// Import các DAO và Service
import com.kimquyen.healthapp.dao.AccountDAO;
import com.kimquyen.healthapp.dao.UserDataDAO;
import com.kimquyen.healthapp.service.AssessmentService;
import com.kimquyen.healthapp.service.AuthService;
import com.kimquyen.healthapp.service.QuestionService;
import com.kimquyen.healthapp.service.SponsorService;
import com.kimquyen.healthapp.service.UserService;

// Import các Model và Util
import com.kimquyen.healthapp.model.Account;
import com.kimquyen.healthapp.model.Role;
import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.util.SessionManager;
import com.kimquyen.healthapp.util.UIConstants; // Đảm bảo import này đúng

// Import Swing components
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    // Services
    private AuthService authService;
    private UserService userService;
    private QuestionService questionService;
    private AssessmentService assessmentService;
    private SponsorService sponsorService;
    private UserDataDAO userDataDAO;

    // UI Panels
    private UserDashboardPanel userDashboardPanel;
    private AdminDashboardPanel adminDashboardPanel;
    private TakeAssessmentPanel takeAssessmentPanel;
    private ManageUsersPanel manageUsersPanel;
    private ManageQuestionsPanel manageQuestionsPanel;
    private ManageSponsorsPanel manageSponsorsPanel;
    private ViewHistoryPanel viewHistoryPanel;
    private GlobalReportsPanel globalReportsPanel;

    // UI cho giao diện mới
    private JPanel sidebarPanel;
    private JPanel mainContentPanelContainer;
    private CardLayout cardLayout;
    private JLabel appTitleLabel;
    private JLabel userNameLabel;
    private JSeparator userMenuSeparator;
    private Component menuSpacing;
    private List<JButton> sidebarButtons = new ArrayList<>();
    private JButton logoutButton;

    // Card names (Constants)
    public static final String USER_DASHBOARD_CARD = "UserDashboard";
    public static final String ADMIN_DASHBOARD_CARD = "AdminDashboard";
    public static final String TAKE_ASSESSMENT_CARD = "TakeAssessment";
    public static final String MANAGE_USERS_CARD = "ManageUsers";
    public static final String MANAGE_QUESTIONS_CARD = "ManageQuestions";
    public static final String MANAGE_SPONSORS_CARD = "ManageSponsors";
    public static final String VIEW_HISTORY_CARD = "ViewHistoryPanel";
    public static final String GLOBAL_REPORTS_CARD = "GlobalReports";
    public static final String ERROR_PANEL_DISPLAY_CARD = "ErrorPanelDisplay";

    private static final String ACTION_CMD_LOGOUT = "LOGOUT_ACTION";

    public MainFrame(AuthService authService, UserService userService,
                     QuestionService questionService, AssessmentService assessmentService, SponsorService sponsorService,
                     UserDataDAO userDataDAO, AccountDAO accountDAO /* Không dùng accountDAO trực tiếp */) {
        this.authService = authService;
        this.userService = userService;
        this.questionService = questionService;
        this.assessmentService = assessmentService;
        this.sponsorService = sponsorService;
        this.userDataDAO = userDataDAO;

        setTitle("Health Response Application");
        setSize(1280, 760);
        setMinimumSize(new Dimension(1000, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        determineInitialPanel();
    }

    private void initComponents() {
        // Sử dụng màu từ UIConstants - bạn đang dùng COLOR_BACKGROUND_DARK cho nền chính là màu tối
        // Nếu muốn nền sáng, bạn cần định nghĩa một hằng số màu sáng trong UIConstants
        // và sử dụng nó ở đây, hoặc thay đổi giá trị của COLOR_BACKGROUND_DARK.
        // Giả sử UIConstants.COLOR_BACKGROUND_DARK là màu nền bạn muốn.
        getContentPane().setBackground(UIConstants.COLOR_BACKGROUND_DARK);
        setLayout(new BorderLayout(0, 0));

        // --- Sidebar Panel ---
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(UIConstants.COLOR_SIDEBAR_DARK);
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(280, getHeight()));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIConstants.COLOR_BORDER_DARK));

        appTitleLabel = new JLabel("Health App", SwingConstants.CENTER);
        appTitleLabel.setFont(UIConstants.FONT_TITLE_LARGE.deriveFont(24f));
        appTitleLabel.setForeground(UIConstants.COLOR_ACCENT_BLUE);
        appTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        appTitleLabel.setBorder(new EmptyBorder(30, 15, 20, 15));
        sidebarPanel.add(appTitleLabel);

        userNameLabel = new JLabel("User Name", SwingConstants.CENTER);
        userNameLabel.setFont(UIConstants.FONT_PRIMARY_BOLD.deriveFont(16f));
        userNameLabel.setForeground(UIConstants.COLOR_TEXT_SECONDARY_LIGHT);
        userNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        userNameLabel.setBorder(new EmptyBorder(5, 15, 20, 15));
        sidebarPanel.add(userNameLabel);

        userMenuSeparator = new JSeparator(SwingConstants.HORIZONTAL);
        userMenuSeparator.setForeground(UIConstants.COLOR_BORDER_DARK);
        userMenuSeparator.setBackground(UIConstants.COLOR_BORDER_DARK);
        Dimension sepSize = new Dimension(sidebarPanel.getPreferredSize().width - 40, 1);
        userMenuSeparator.setPreferredSize(sepSize);
        userMenuSeparator.setMaximumSize(sepSize);
        userMenuSeparator.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarPanel.add(userMenuSeparator);

        menuSpacing = Box.createRigidArea(new Dimension(0, 20));
        sidebarPanel.add(menuSpacing);

        logoutButton = createSidebarButton("Đăng Xuất", ACTION_CMD_LOGOUT);

        add(sidebarPanel, BorderLayout.WEST);

        // --- Main Content Panel Container ---
        cardLayout = new CardLayout();
        mainContentPanelContainer = new JPanel(cardLayout);
        // Các panel con sẽ tự set màu nền, hoặc bạn có thể set màu nền chung ở đây
        mainContentPanelContainer.setBackground(UIConstants.COLOR_BACKGROUND_DARK); // Hoặc một màu nền khác cho content area
        add(mainContentPanelContainer, BorderLayout.CENTER);

        // --- Khởi tạo các panel con ---
        userDashboardPanel = new UserDashboardPanel(this, this.assessmentService);
        adminDashboardPanel = new AdminDashboardPanel(this, this.userService, this.questionService, this.assessmentService);
        takeAssessmentPanel = new TakeAssessmentPanel(this, this.assessmentService);
        manageUsersPanel = new ManageUsersPanel(this, this.userService);
        manageQuestionsPanel = new ManageQuestionsPanel(this, this.questionService);
        manageSponsorsPanel = new ManageSponsorsPanel(this, this.sponsorService);
        viewHistoryPanel = new ViewHistoryPanel(this, this.assessmentService);
        globalReportsPanel = new GlobalReportsPanel(this, this.assessmentService, this.userService, this.questionService);

        // --- Thêm các panel (card) vào mainContentPanelContainer ---
        mainContentPanelContainer.add(userDashboardPanel, USER_DASHBOARD_CARD);
        mainContentPanelContainer.add(adminDashboardPanel, ADMIN_DASHBOARD_CARD);
        mainContentPanelContainer.add(takeAssessmentPanel, TAKE_ASSESSMENT_CARD);
        mainContentPanelContainer.add(manageUsersPanel, MANAGE_USERS_CARD);
        mainContentPanelContainer.add(manageQuestionsPanel, MANAGE_QUESTIONS_CARD);
        mainContentPanelContainer.add(manageSponsorsPanel, MANAGE_SPONSORS_CARD);
        mainContentPanelContainer.add(viewHistoryPanel, VIEW_HISTORY_CARD);
        mainContentPanelContainer.add(globalReportsPanel, GLOBAL_REPORTS_CARD);

        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(UIConstants.COLOR_BACKGROUND_DARK);
        JLabel errorLabel = new JLabel("Panel không tồn tại hoặc có lỗi hiển thị.", SwingConstants.CENTER);
        errorLabel.setForeground(UIConstants.COLOR_TEXT_LIGHT); // Chữ sáng trên nền tối
        errorLabel.setFont(UIConstants.FONT_TITLE_MEDIUM);
        errorPanel.add(errorLabel, BorderLayout.CENTER);
        mainContentPanelContainer.add(errorPanel, ERROR_PANEL_DISPLAY_CARD);
    }

    private JButton createSidebarButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setActionCommand(actionCommand);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(UIConstants.COLOR_TEXT_LIGHT);
        button.setBackground(UIConstants.COLOR_SIDEBAR_DARK);
        button.setFont(UIConstants.FONT_SIDEBAR_BUTTON);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(UIConstants.INSETS_SIDEBAR_BUTTON));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        Dimension preferredSize = new Dimension(Integer.MAX_VALUE, 55);
        button.setPreferredSize(preferredSize);
        button.setMinimumSize(new Dimension(200, 55));
        button.setMaximumSize(preferredSize);

        button.addActionListener(e -> {
            String cmd = e.getActionCommand();
            if (ACTION_CMD_LOGOUT.equals(cmd)) {
                performLogout();
            } else {
                showPanel(cmd);
            }
        });

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!Boolean.TRUE.equals(button.getClientProperty("activeButton"))) {
                    button.setBackground(UIConstants.COLOR_COMPONENT_BACKGROUND_DARK);
                    Border hoverBorder = BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 4, 0, 0, UIConstants.COLOR_ACCENT_BLUE),
                            new EmptyBorder(
                                    UIConstants.INSETS_SIDEBAR_BUTTON.top,
                                    UIConstants.INSETS_SIDEBAR_BUTTON.left - 4,
                                    UIConstants.INSETS_SIDEBAR_BUTTON.bottom,
                                    UIConstants.INSETS_SIDEBAR_BUTTON.right)
                    );
                    button.setBorder(hoverBorder);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!Boolean.TRUE.equals(button.getClientProperty("activeButton"))) {
                    button.setBackground(UIConstants.COLOR_SIDEBAR_DARK);
                    button.setBorder(new EmptyBorder(UIConstants.INSETS_SIDEBAR_BUTTON));
                }
            }
        });
        button.putClientProperty("activeButton", Boolean.FALSE);
        return button;
    }

    private void updateActiveSidebarButton(String activeActionCommand) {
        for (JButton btn : sidebarButtons) {
            if (btn.getActionCommand().equals(activeActionCommand)) {
                btn.setBackground(UIConstants.COLOR_ACCENT_BLUE);
                btn.setForeground(Color.WHITE);
                Border activeBorder = BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, Color.WHITE),
                        new EmptyBorder(
                                UIConstants.INSETS_SIDEBAR_BUTTON.top,
                                UIConstants.INSETS_SIDEBAR_BUTTON.left - 4,
                                UIConstants.INSETS_SIDEBAR_BUTTON.bottom,
                                UIConstants.INSETS_SIDEBAR_BUTTON.right)
                );
                btn.setBorder(activeBorder);
                btn.putClientProperty("activeButton", Boolean.TRUE);
            } else {
                btn.setBackground(UIConstants.COLOR_SIDEBAR_DARK);
                btn.setForeground(UIConstants.COLOR_TEXT_LIGHT);
                btn.setBorder(new EmptyBorder(UIConstants.INSETS_SIDEBAR_BUTTON));
                btn.putClientProperty("activeButton", Boolean.FALSE);
            }
        }
        logoutButton.setBackground(UIConstants.COLOR_SIDEBAR_DARK);
        logoutButton.setForeground(UIConstants.COLOR_TEXT_LIGHT);
        logoutButton.setBorder(new EmptyBorder(UIConstants.INSETS_SIDEBAR_BUTTON));
        logoutButton.putClientProperty("activeButton", Boolean.FALSE);
    }

    private void updateSidebarBasedOnRole() {
        List<Component> componentsToRemove = new ArrayList<>();
        for (Component comp : sidebarPanel.getComponents()) {
            if (comp instanceof JButton && comp != logoutButton) {
                componentsToRemove.add(comp);
            } else if (comp instanceof Box.Filler) {
                componentsToRemove.add(comp);
            }
        }
        for (Component comp : componentsToRemove) {
            sidebarPanel.remove(comp);
        }
        sidebarButtons.clear();

        int menuInsertIndex = -1;
        for (int i = 0; i < sidebarPanel.getComponentCount(); i++) {
            if (sidebarPanel.getComponent(i) == menuSpacing) {
                menuInsertIndex = i + 1;
                break;
            }
        }
        if (menuInsertIndex == -1) {
             menuInsertIndex = sidebarPanel.getComponentCount();
             if (logoutButton.getParent() == sidebarPanel) menuInsertIndex--; // Chèn trước logout nếu nó đã ở đó
             if (menuInsertIndex < 0) menuInsertIndex = 0;
        }


        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn() && session.getCurrentAccount() != null) {
            Account currentUser = session.getCurrentAccount();
            UserData currentUserData = session.getCurrentUserData();

            String userDisplayName = currentUser.getUsername();
            if (currentUserData != null && currentUserData.getName() != null && !currentUserData.getName().trim().isEmpty()) {
                userDisplayName = currentUserData.getName();
            }
            userNameLabel.setText("<html><div style='text-align: center;'>" + userDisplayName + "<br><i style='font-size:smaller;'>(" + currentUser.getRole().name() + ")</i></div></html>");


            if (currentUser.getRole() == Role.ADMIN) {
                sidebarButtons.add(createSidebarButton("Dashboard Admin", ADMIN_DASHBOARD_CARD));
                sidebarButtons.add(createSidebarButton("Quản Lý Người Dùng", MANAGE_USERS_CARD));
                sidebarButtons.add(createSidebarButton("Quản Lý Câu Hỏi", MANAGE_QUESTIONS_CARD));
                sidebarButtons.add(createSidebarButton("Quản Lý Nhà Tài Trợ", MANAGE_SPONSORS_CARD));
                sidebarButtons.add(createSidebarButton("Báo Cáo Tổng Thể", GLOBAL_REPORTS_CARD));
            } else { // USER
                sidebarButtons.add(createSidebarButton("Dashboard", USER_DASHBOARD_CARD));
                sidebarButtons.add(createSidebarButton("Làm Đánh Giá", TAKE_ASSESSMENT_CARD));
                sidebarButtons.add(createSidebarButton("Lịch Sử Đánh Giá", VIEW_HISTORY_CARD));
            }

            for (JButton btn : sidebarButtons) {
                sidebarPanel.add(btn, menuInsertIndex++);
            }

            if (logoutButton.getParent() != null) {
                sidebarPanel.remove(logoutButton);
            }
            sidebarPanel.add(Box.createVerticalGlue(), menuInsertIndex++);
            sidebarPanel.add(logoutButton, menuInsertIndex);

        } else {
            userNameLabel.setText("Chưa đăng nhập");
             if (logoutButton.getParent() != null) {
                sidebarPanel.remove(logoutButton);
            }
        }

        sidebarPanel.revalidate();
        sidebarPanel.repaint();
    }


    private void determineInitialPanel() {
        updateSidebarBasedOnRole();
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn() && session.getCurrentAccount() != null) {
            Account currentUser = session.getCurrentAccount();
            if (currentUser.getRole() == Role.ADMIN) {
                showPanel(ADMIN_DASHBOARD_CARD);
            } else {
                showPanel(USER_DASHBOARD_CARD);
            }
        } else {
            performLogout();
        }
    }

    public void showPanel(String panelNameCard) {
        if (cardLayout == null || mainContentPanelContainer == null) {
            System.err.println("Lỗi MainFrame: CardLayout hoặc mainContentPanelContainer chưa được khởi tạo.");
            if(mainContentPanelContainer != null && cardLayout != null) {
                 cardLayout.show(mainContentPanelContainer, ERROR_PANEL_DISPLAY_CARD);
            }
            setTitle("Lỗi Hiển Thị - Health App");
            return;
        }

        String panelTitle = "Health App";
        boolean panelFoundAndVisible = false;

        if (USER_DASHBOARD_CARD.equals(panelNameCard) && userDashboardPanel != null) {
            userDashboardPanel.panelVisible(); panelFoundAndVisible = true; panelTitle = "Bảng Điều Khiển";
        } else if (ADMIN_DASHBOARD_CARD.equals(panelNameCard) && adminDashboardPanel != null) {
            adminDashboardPanel.panelVisible(); panelFoundAndVisible = true; panelTitle = "Bảng Điều Khiển Admin";
        } else if (TAKE_ASSESSMENT_CARD.equals(panelNameCard) && takeAssessmentPanel != null) {
            takeAssessmentPanel.panelVisible(); panelFoundAndVisible = true; panelTitle = "Làm Bài Đánh Giá";
        } else if (MANAGE_USERS_CARD.equals(panelNameCard) && manageUsersPanel != null) {
            manageUsersPanel.panelVisible(); panelFoundAndVisible = true; panelTitle = "Quản Lý Người Dùng";
        } else if (MANAGE_QUESTIONS_CARD.equals(panelNameCard) && manageQuestionsPanel != null) {
            manageQuestionsPanel.panelVisible(); panelFoundAndVisible = true; panelTitle = "Quản Lý Câu Hỏi";
        } else if (MANAGE_SPONSORS_CARD.equals(panelNameCard) && manageSponsorsPanel != null) {
            manageSponsorsPanel.panelVisible(); panelFoundAndVisible = true; panelTitle = "Quản Lý Nhà Tài Trợ";
        } else if (VIEW_HISTORY_CARD.equals(panelNameCard) && viewHistoryPanel != null) {
            viewHistoryPanel.panelVisible(); panelFoundAndVisible = true; panelTitle = "Lịch Sử Đánh Giá";
        } else if (GLOBAL_REPORTS_CARD.equals(panelNameCard) && globalReportsPanel != null) {
            globalReportsPanel.panelVisible(); panelFoundAndVisible = true; panelTitle = "Báo Cáo Tổng Thể";
        }

        if (panelFoundAndVisible) {
            cardLayout.show(mainContentPanelContainer, panelNameCard);
            updateActiveSidebarButton(panelNameCard);
        } else {
             System.err.println("Cảnh báo MainFrame: Panel instance không được tìm thấy hoặc là null cho card '" + panelNameCard + "'. Hiển thị panel lỗi.");
            cardLayout.show(mainContentPanelContainer, ERROR_PANEL_DISPLAY_CARD);
            updateActiveSidebarButton("");
            panelTitle = "Lỗi Hiển Thị";
        }
        setTitle(panelTitle + " - Health App");
    }

    public void performLogout() {
        SessionManager.getInstance().logout();
        this.dispose();

        AuthService appAuthService = com.kimquyen.healthapp.MainApp.authService;
        UserDataDAO appUserDataDAO = com.kimquyen.healthapp.MainApp.userDataDAO;

        if (appAuthService != null && appUserDataDAO != null) {
            SwingUtilities.invokeLater(() -> {
                new LoginFrame(appAuthService, appUserDataDAO).setVisible(true);
            });
        } else {
            System.err.println("LỖI NGHIÊM TRỌNG: Không thể khởi tạo lại LoginFrame (authService hoặc userDataDAO là null từ MainApp). Vui lòng khởi động lại ứng dụng.");
            JOptionPane.showMessageDialog(null, "Lỗi nghiêm trọng khi đăng xuất. Vui lòng khởi động lại ứng dụng.", "Lỗi Đăng Xuất", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}