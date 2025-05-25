// package com.kimquyen.healthapp.ui;
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
import com.kimquyen.healthapp.model.UserData; // Cần UserData để hiển thị tên
import com.kimquyen.healthapp.util.SessionManager;
import com.kimquyen.healthapp.util.UIConstants;

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

    // Services (Không cần DAOs ở đây nữa nếu các service đã đủ)
    private AuthService authService; // Cần cho logout để tạo lại LoginFrame
    private UserService userService;
    private QuestionService questionService;
    private AssessmentService assessmentService;
    private SponsorService sponsorService;
    private UserDataDAO userDataDAO; // Cần cho logout

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
    public static final String ERROR_PANEL_DISPLAY_CARD = "ErrorPanelDisplay"; // Card cho panel lỗi


    public MainFrame(AuthService authService, UserService userService,
                     QuestionService questionService, AssessmentService assessmentService, SponsorService sponsorService,
                     UserDataDAO userDataDAO, AccountDAO accountDAO /* AccountDAO có thể không cần trực tiếp ở đây */) {
        this.authService = authService;
        this.userService = userService;
        this.questionService = questionService;
        this.assessmentService = assessmentService;
        this.sponsorService = sponsorService;
        this.userDataDAO = userDataDAO; // Giữ lại cho performLogout
        // this.accountDAO = accountDAO; // Có thể không cần nếu service xử lý hết

        setTitle("Health Response Application");
        setSize(1280, 760); // Kích thước tùy chỉnh
        setMinimumSize(new Dimension(1000, 600)); // Kích thước tối thiểu
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        determineInitialPanel();
    }

    private void initComponents() {
        getContentPane().setBackground(UIConstants.COLOR_BACKGROUND_DARK);
        setLayout(new BorderLayout(0, 0));

        // --- Sidebar Panel ---
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(UIConstants.COLOR_SIDEBAR_DARK);
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(280, getHeight())); // Tăng chiều rộng sidebar
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIConstants.COLOR_BORDER_DARK));

        appTitleLabel = new JLabel("Health App", SwingConstants.CENTER);
        appTitleLabel.setFont(UIConstants.FONT_TITLE_LARGE.deriveFont(24f)); // Font to hơn
        appTitleLabel.setForeground(UIConstants.COLOR_ACCENT_BLUE);
        appTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        appTitleLabel.setBorder(new EmptyBorder(25, 15, 15, 15));
        sidebarPanel.add(appTitleLabel);

        userNameLabel = new JLabel("User Name", SwingConstants.CENTER); // Placeholder
        userNameLabel.setFont(UIConstants.FONT_PRIMARY_BOLD.deriveFont(16f));
        userNameLabel.setForeground(UIConstants.COLOR_TEXT_SECONDARY_LIGHT);
        userNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        userNameLabel.setBorder(new EmptyBorder(5, 15, 25, 15));
        sidebarPanel.add(userNameLabel);

        // Ngăn cách giữa tên user và các nút menu
        sidebarPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        sidebarPanel.add(Box.createRigidArea(new Dimension(0,15)));


        logoutButton = createSidebarButton("Đăng Xuất", "LOGOUT_ACTION" /*, "/icons/logout_light.png" */);

        add(sidebarPanel, BorderLayout.WEST);

        // --- Main Content Panel Container ---
        cardLayout = new CardLayout();
        mainContentPanelContainer = new JPanel(cardLayout);
        mainContentPanelContainer.setBackground(UIConstants.COLOR_BACKGROUND_DARK);
        add(mainContentPanelContainer, BorderLayout.CENTER);

        // --- Khởi tạo các panel con ---
        userDashboardPanel = new UserDashboardPanel(this, this.assessmentService);
        adminDashboardPanel = new AdminDashboardPanel(this, this.userService, this.questionService, this.assessmentService);
        takeAssessmentPanel = new TakeAssessmentPanel(this, this.assessmentService);
        manageUsersPanel = new ManageUsersPanel(this, this.userService);
        manageQuestionsPanel = new ManageQuestionsPanel(this, this.questionService);
        manageSponsorsPanel = new ManageSponsorsPanel(this, this.sponsorService);
        viewHistoryPanel = new ViewHistoryPanel(this, this.assessmentService);
        globalReportsPanel = new GlobalReportsPanel(this, this.assessmentService, this.userService);

        // --- Thêm các panel (card) vào mainContentPanelContainer ---
        mainContentPanelContainer.add(userDashboardPanel, USER_DASHBOARD_CARD);
        mainContentPanelContainer.add(adminDashboardPanel, ADMIN_DASHBOARD_CARD);
        mainContentPanelContainer.add(takeAssessmentPanel, TAKE_ASSESSMENT_CARD);
        mainContentPanelContainer.add(manageUsersPanel, MANAGE_USERS_CARD);
        mainContentPanelContainer.add(manageQuestionsPanel, MANAGE_QUESTIONS_CARD);
        mainContentPanelContainer.add(manageSponsorsPanel, MANAGE_SPONSORS_CARD);
        mainContentPanelContainer.add(viewHistoryPanel, VIEW_HISTORY_CARD);
        mainContentPanelContainer.add(globalReportsPanel, GLOBAL_REPORTS_CARD);

        // Panel lỗi (để không bị lỗi nếu card không tồn tại lúc đầu)
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(UIConstants.COLOR_BACKGROUND_DARK);
        JLabel errorLabel = new JLabel("Panel không tồn tại hoặc có lỗi hiển thị.", SwingConstants.CENTER);
        errorLabel.setForeground(UIConstants.COLOR_TEXT_LIGHT);
        errorLabel.setFont(UIConstants.FONT_TITLE_MEDIUM);
        errorPanel.add(errorLabel, BorderLayout.CENTER);
        mainContentPanelContainer.add(errorPanel, ERROR_PANEL_DISPLAY_CARD);
    }

    private JButton createSidebarButton(String text, String actionCommand /*, String iconResourcePath */) {
        JButton button = new JButton(text);
        button.setActionCommand(actionCommand);

        // if (iconResourcePath != null && !iconResourcePath.isEmpty()) {
        //     try {
        //         java.net.URL iconURL = getClass().getResource(iconResourcePath);
        //         if (iconURL != null) {
        //             ImageIcon icon = new ImageIcon(iconURL);
        //             // Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH); // Resize nếu cần
        //             // button.setIcon(new ImageIcon(img));
        //             button.setIcon(icon);
        //             button.setIconTextGap(12); // Khoảng cách giữa icon và text
        //         }
        //     } catch (Exception e) { System.err.println("Lỗi tải icon sidebar: " + iconResourcePath);}
        // }

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(UIConstants.COLOR_TEXT_LIGHT);
        button.setBackground(UIConstants.COLOR_SIDEBAR_DARK);
        button.setFont(UIConstants.FONT_SIDEBAR_BUTTON);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(UIConstants.INSETS_SIDEBAR_BUTTON)); // Sử dụng EmptyBorder đúng cách
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT); // Căn trái trong BoxLayout

        Dimension preferredSize = new Dimension(Integer.MAX_VALUE, 50); // Chiều cao cố định, chiều rộng tối đa
        button.setPreferredSize(preferredSize);
        button.setMinimumSize(new Dimension(200, 50)); // Chiều rộng tối thiểu
        button.setMaximumSize(preferredSize);


        button.addActionListener(e -> {
            String cmd = e.getActionCommand();
            if ("LOGOUT_ACTION".equals(cmd)) {
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
                    button.setBorder(new EmptyBorder(UIConstants.INSETS_SIDEBAR_BUTTON)); // Reset border
                }
            }
        });
        button.putClientProperty("activeButton", Boolean.FALSE);
        sidebarButtons.add(button);
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
                btn.setBorder(new EmptyBorder(UIConstants.INSETS_SIDEBAR_BUTTON)); // Reset border
                btn.putClientProperty("activeButton", Boolean.FALSE);
            }
        }
    }

    private void updateSidebarBasedOnRole() {
        // Xóa các nút cũ, nhưng không xóa appTitleLabel, userNameLabel, JSeparator
        for (int i = sidebarPanel.getComponentCount() - 1; i >= 0; i--) {
            Component comp = sidebarPanel.getComponent(i);
            if (comp instanceof JButton || comp instanceof Box.Filler || comp instanceof JSeparator) {
                 // Nếu là JSeparator được thêm sau userNameLabel, ta cần giữ lại nó.
                 // Cách đơn giản là chỉ xóa JButton và Box.Filler
                 if (comp instanceof JButton || comp instanceof Box.Filler){
                    sidebarPanel.remove(comp);
                 }
            }
        }
        // Nếu JSeparator cũng bị xóa, thêm lại sau userNameLabel:
        // Component lastStaticComp = userNameLabel; // Hoặc JSeparator nếu bạn đã thêm nó cố định
        // int insertAtIndex = -1;
        // for(int i=0; i < sidebarPanel.getComponentCount(); i++){
        //     if(sidebarPanel.getComponent(i) == lastStaticComp){
        //         insertAtIndex = i + 1;
        //         break;
        //     }
        // }
        // if(insertAtIndex != -1 && !(sidebarPanel.getComponent(insertAtIndex) instanceof JSeparator)){
        //      sidebarPanel.add(new JSeparator(SwingConstants.HORIZONTAL), insertAtIndex);
        //      sidebarPanel.add(Box.createRigidArea(new Dimension(0,15)), insertAtIndex + 1);
        // }


        sidebarButtons.clear();

        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn() && session.getCurrentAccount() != null) {
            Account currentUser = session.getCurrentAccount();
            UserData currentUserData = session.getCurrentUserData();

            if (currentUserData != null && currentUserData.getName() != null) {
                userNameLabel.setText(currentUserData.getName());
            } else {
                userNameLabel.setText(currentUser.getUsername());
            }

            if (currentUser.getRole() == Role.ADMIN) {
                sidebarPanel.add(createSidebarButton("Dashboard Admin", ADMIN_DASHBOARD_CARD /*, "/icons/dashboard_admin.png"*/));
                sidebarPanel.add(createSidebarButton("Quản Lý Người Dùng", MANAGE_USERS_CARD /*, "/icons/users.png"*/));
                sidebarPanel.add(createSidebarButton("Quản Lý Câu Hỏi", MANAGE_QUESTIONS_CARD /*, "/icons/questions.png"*/));
                sidebarPanel.add(createSidebarButton("Quản Lý Nhà Tài Trợ", MANAGE_SPONSORS_CARD /*, "/icons/sponsors.png"*/));
                sidebarPanel.add(createSidebarButton("Báo Cáo Tổng Thể", GLOBAL_REPORTS_CARD /*, "/icons/reports.png"*/));
            } else { // USER
                sidebarPanel.add(createSidebarButton("Dashboard", USER_DASHBOARD_CARD /*, "/icons/dashboard_user.png"*/));
                sidebarPanel.add(createSidebarButton("Làm Đánh Giá", TAKE_ASSESSMENT_CARD /*, "/icons/assessment.png"*/));
                sidebarPanel.add(createSidebarButton("Lịch Sử Đánh Giá", VIEW_HISTORY_CARD /*, "/icons/history.png"*/));
            }
            sidebarPanel.add(Box.createVerticalGlue());
            sidebarPanel.add(logoutButton);
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
            return;
        }

        String panelTitle = "Health App"; // Tiêu đề mặc định
        boolean panelFound = true;

        switch (panelNameCard) {
            case USER_DASHBOARD_CARD:
                if (userDashboardPanel != null) userDashboardPanel.panelVisible(); else panelFound = false;
                panelTitle = "Bảng Điều Khiển";
                break;
            case ADMIN_DASHBOARD_CARD:
                if (adminDashboardPanel != null) adminDashboardPanel.panelVisible(); else panelFound = false;
                panelTitle = "Bảng Điều Khiển Admin";
                break;
            case TAKE_ASSESSMENT_CARD:
                if (takeAssessmentPanel != null) takeAssessmentPanel.panelVisible(); else panelFound = false;
                panelTitle = "Làm Bài Đánh Giá";
                break;
            case MANAGE_USERS_CARD:
                if (manageUsersPanel != null) manageUsersPanel.panelVisible(); else panelFound = false;
                panelTitle = "Quản Lý Người Dùng";
                break;
            case MANAGE_QUESTIONS_CARD:
                if (manageQuestionsPanel != null) manageQuestionsPanel.panelVisible(); else panelFound = false;
                panelTitle = "Quản Lý Câu Hỏi";
                break;
            case MANAGE_SPONSORS_CARD:
                if (manageSponsorsPanel != null) manageSponsorsPanel.panelVisible(); else panelFound = false;
                panelTitle = "Quản Lý Nhà Tài Trợ";
                break;
            case VIEW_HISTORY_CARD:
                if (viewHistoryPanel != null) viewHistoryPanel.panelVisible(); else panelFound = false;
                panelTitle = "Lịch Sử Đánh Giá";
                break;
            case GLOBAL_REPORTS_CARD:
                if (globalReportsPanel != null) globalReportsPanel.panelVisible(); else panelFound = false;
                panelTitle = "Báo Cáo Tổng Thể";
                break;
            default:
                panelFound = false;
                System.err.println("Cảnh báo MainFrame: Không tìm thấy panel với tên card '" + panelNameCard + "'.");
                break;
        }

        if (panelFound) {
            cardLayout.show(mainContentPanelContainer, panelNameCard);
            updateActiveSidebarButton(panelNameCard);
        } else {
            cardLayout.show(mainContentPanelContainer, ERROR_PANEL_DISPLAY_CARD);
            // Không cập nhật active button nếu panel không tìm thấy, hoặc chọn một nút mặc định
            // updateActiveSidebarButton(""); // Hoặc một card name mặc định
            panelTitle = "Lỗi Hiển Thị";
        }
        setTitle(panelTitle + " - Health App");
    }

    public void performLogout() {
        SessionManager.getInstance().logout();
        this.dispose();
        if (com.kimquyen.healthapp.MainApp.authService != null && com.kimquyen.healthapp.MainApp.userDataDAO != null) {
            SwingUtilities.invokeLater(() -> {
                new LoginFrame(com.kimquyen.healthapp.MainApp.authService, com.kimquyen.healthapp.MainApp.userDataDAO).setVisible(true);
            });
        } else {
            System.err.println("LỖI NGHIÊM TRỌNG: Không thể khởi tạo lại LoginFrame (service/DAO null từ MainApp).");
            JOptionPane.showMessageDialog(null, "Lỗi nghiêm trọng khi đăng xuất. Vui lòng khởi động lại ứng dụng.", "Lỗi Đăng Xuất", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}