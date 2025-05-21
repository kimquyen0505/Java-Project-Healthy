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
import com.kimquyen.healthapp.util.SessionManager;

// Import Swing components
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    // Services and DAOs
    private AuthService authService;
    private UserService userService;
    private QuestionService questionService;
    private AssessmentService assessmentService;
    private UserDataDAO userDataDAO;
    private AccountDAO accountDAO;
    private SponsorService sponsorService;

    // UI Panels
    private UserDashboardPanel userDashboardPanel;
    private AdminDashboardPanel adminDashboardPanel;
    private TakeAssessmentPanel takeAssessmentPanel;
    private ManageUsersPanel manageUsersPanel;
    private ManageQuestionsPanel manageQuestionsPanel;
    private ManageSponsorsPanel manageSponsorsPanel;
    private ViewHistoryPanel viewHistoryPanel;
    private GlobalReportsPanel globalReportsPanel; // Đã khai báo

    // Layout and Menu
    private JPanel mainPanelContainer;
    private CardLayout cardLayout;
    private JMenuBar menuBar;
    private JMenu adminManageMenu = null;

    // Card names (Constants)
    public static final String USER_DASHBOARD_CARD = "UserDashboard";
    public static final String ADMIN_DASHBOARD_CARD = "AdminDashboard";
    public static final String TAKE_ASSESSMENT_CARD = "TakeAssessment";
    public static final String MANAGE_USERS_CARD = "ManageUsers";
    public static final String MANAGE_QUESTIONS_CARD = "ManageQuestions";
    public static final String MANAGE_SPONSORS_CARD = "ManageSponsors";
    public static final String VIEW_HISTORY_CARD = "ViewHistoryPanel";
    public static final String GLOBAL_REPORTS_CARD = "GlobalReports"; // Đã khai báo

    public MainFrame(AuthService authService, UserService userService,
                     QuestionService questionService, AssessmentService assessmentService, SponsorService sponsorService,
                     UserDataDAO userDataDAO, AccountDAO accountDAO) {
        this.authService = authService;
        this.userService = userService;
        this.questionService = questionService;
        this.assessmentService = assessmentService;
        this.sponsorService = sponsorService;
        this.userDataDAO = userDataDAO;
        this.accountDAO = accountDAO;

        setTitle("Health Response Application");
        setSize(1000, 800); // Tăng kích thước để có không gian cho báo cáo
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        determineInitialPanel();
    }

    private void initComponents() {
        // --- Menu Bar ---
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Tệp");
        fileMenu.setMnemonic('T');
        JMenuItem logoutMenuItem = new JMenuItem("Đăng xuất");
        logoutMenuItem.setMnemonic('X');
        logoutMenuItem.addActionListener(e -> performLogout());
        fileMenu.add(logoutMenuItem);
        JMenuItem exitMenuItem = new JMenuItem("Thoát");
        exitMenuItem.setMnemonic('h');
        exitMenuItem.addActionListener(e -> System.exit(0));
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        JMenu helpMenu = new JMenu("Trợ giúp");
        helpMenu.setMnemonic('G');
        JMenuItem aboutMenuItem = new JMenuItem("Thông tin");
        aboutMenuItem.setMnemonic('T');
        aboutMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Health Response App v1.0\nPhát triển bởi Kim Quyen", "Thông tin", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutMenuItem);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // --- Main Panel Container with CardLayout ---
        cardLayout = new CardLayout();
        mainPanelContainer = new JPanel(cardLayout);

        // --- Khởi tạo các panel con ---
        userDashboardPanel = new UserDashboardPanel(this, this.assessmentService);
        adminDashboardPanel = new AdminDashboardPanel(this, this.userService, this.questionService, this.assessmentService);
        takeAssessmentPanel = new TakeAssessmentPanel(this, this.assessmentService);
        manageUsersPanel = new ManageUsersPanel(this, this.userService);
        manageQuestionsPanel = new ManageQuestionsPanel(this, this.questionService);
        manageSponsorsPanel = new ManageSponsorsPanel(this, this.sponsorService);
        viewHistoryPanel = new ViewHistoryPanel(this, this.assessmentService);
        globalReportsPanel = new GlobalReportsPanel(this, this.assessmentService, this.userService); // Đã khởi tạo

        // --- Thêm các panel (card) vào mainPanelContainer ---
        mainPanelContainer.add(userDashboardPanel, USER_DASHBOARD_CARD);
        mainPanelContainer.add(adminDashboardPanel, ADMIN_DASHBOARD_CARD);
        mainPanelContainer.add(takeAssessmentPanel, TAKE_ASSESSMENT_CARD);
        mainPanelContainer.add(manageUsersPanel, MANAGE_USERS_CARD);
        mainPanelContainer.add(manageQuestionsPanel, MANAGE_QUESTIONS_CARD);
        mainPanelContainer.add(manageSponsorsPanel, MANAGE_SPONSORS_CARD);
        mainPanelContainer.add(viewHistoryPanel, VIEW_HISTORY_CARD);
        mainPanelContainer.add(globalReportsPanel, GLOBAL_REPORTS_CARD); // Đã thêm

        add(mainPanelContainer, BorderLayout.CENTER);
    }

    private void determineInitialPanel() {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn() && session.getCurrentAccount() != null) {
            Account currentUser = session.getCurrentAccount();
            if (currentUser.getRole() == Role.ADMIN) {
                showPanel(ADMIN_DASHBOARD_CARD);
                addAdminSpecificUI();
            } else {
                showPanel(USER_DASHBOARD_CARD);
                removeAdminSpecificUI();
            }
        } else {
            performLogout();
        }
    }

    private void addAdminSpecificUI() {
        if (adminManageMenu == null) {
            adminManageMenu = new JMenu("Quản Lý (Admin)");
            adminManageMenu.setMnemonic('Q');

            JMenuItem manageUsersItem = new JMenuItem("Quản Lý Người Dùng");
            manageUsersItem.setMnemonic('N');
            manageUsersItem.addActionListener(e -> showPanel(MANAGE_USERS_CARD));
            adminManageMenu.add(manageUsersItem);

            JMenuItem manageQuestionsItem = new JMenuItem("Quản Lý Câu Hỏi");
            manageQuestionsItem.setMnemonic('H');
            manageQuestionsItem.addActionListener(e -> showPanel(MANAGE_QUESTIONS_CARD));
            adminManageMenu.add(manageQuestionsItem);

            JMenuItem manageSponsorsItem = new JMenuItem("Quản Lý Nhà Tài Trợ");
            manageSponsorsItem.setMnemonic('S');
            manageSponsorsItem.addActionListener(e -> showPanel(MANAGE_SPONSORS_CARD));
            adminManageMenu.add(manageSponsorsItem);

            adminManageMenu.addSeparator(); // <<==== THÊM DÒNG KẺ PHÂN CÁCH

            JMenuItem viewReportsItem = new JMenuItem("Xem Báo Cáo Tổng Thể"); // <<==== THÊM MENU ITEM NÀY
            viewReportsItem.setMnemonic('B'); // 'B' cho Báo cáo
            viewReportsItem.addActionListener(e -> showPanel(GLOBAL_REPORTS_CARD));
            adminManageMenu.add(viewReportsItem);


            if (menuBar != null) {
                boolean menuExists = false;
                for (int i = 0; i < menuBar.getMenuCount(); i++) {
                    if (menuBar.getMenu(i) == adminManageMenu) {
                        menuExists = true;
                        break;
                    }
                }
                if (!menuExists) {
                    menuBar.add(adminManageMenu, menuBar.getMenuCount() - 1);
                    menuBar.revalidate();
                    menuBar.repaint();
                }
            }
        }
    }

    private void removeAdminSpecificUI() {
        if (adminManageMenu != null && menuBar != null) {
            menuBar.remove(adminManageMenu);
            menuBar.revalidate();
            menuBar.repaint();
            adminManageMenu = null;
        }
    }

    public void showPanel(String panelNameCard) {
        if (cardLayout == null || mainPanelContainer == null) {
            System.err.println("Lỗi MainFrame: CardLayout hoặc mainPanelContainer chưa được khởi tạo.");
            return;
        }

        String panelTitle = "Health Response Application";

        if (USER_DASHBOARD_CARD.equals(panelNameCard) && userDashboardPanel != null) {
            userDashboardPanel.panelVisible();
            panelTitle = "Bảng Điều Khiển Người Dùng";
        } else if (ADMIN_DASHBOARD_CARD.equals(panelNameCard) && adminDashboardPanel != null) {
            adminDashboardPanel.panelVisible();
            panelTitle = "Bảng Điều Khiển Admin";
        } else if (TAKE_ASSESSMENT_CARD.equals(panelNameCard) && takeAssessmentPanel != null) {
            takeAssessmentPanel.panelVisible();
            panelTitle = "Làm Bài Đánh Giá";
        } else if (MANAGE_USERS_CARD.equals(panelNameCard) && manageUsersPanel != null) {
            manageUsersPanel.panelVisible();
            panelTitle = "Quản Lý Người Dùng";
        } else if (MANAGE_QUESTIONS_CARD.equals(panelNameCard) && manageQuestionsPanel != null) {
            manageQuestionsPanel.panelVisible();
            panelTitle = "Quản Lý Câu Hỏi";
        } else if (MANAGE_SPONSORS_CARD.equals(panelNameCard) && manageSponsorsPanel != null) {
            manageSponsorsPanel.panelVisible();
            panelTitle = "Quản Lý Nhà Tài Trợ";
        } else if (VIEW_HISTORY_CARD.equals(panelNameCard) && viewHistoryPanel != null) {
            viewHistoryPanel.panelVisible();
            panelTitle = "Lịch Sử Đánh Giá";
        } else if (GLOBAL_REPORTS_CARD.equals(panelNameCard) && globalReportsPanel != null) { // <<==== THÊM CASE NÀY
            globalReportsPanel.panelVisible();
            panelTitle = "Báo Cáo Tổng Thể";
        }
        else {
            System.err.println("Cảnh báo MainFrame: Không tìm thấy panel với tên card '" + panelNameCard + "' hoặc panel là null. Hiển thị User Dashboard (nếu có).");
            if (userDashboardPanel != null) {
                cardLayout.show(mainPanelContainer, USER_DASHBOARD_CARD);
                userDashboardPanel.panelVisible();
                panelTitle = "Bảng Điều Khiển Người Dùng";
            } else {
                mainPanelContainer.removeAll();
                mainPanelContainer.add(new JLabel("Lỗi: Không thể hiển thị panel '" + panelNameCard + "'. Vui lòng liên hệ quản trị viên.", SwingConstants.CENTER));
                mainPanelContainer.revalidate();
                mainPanelContainer.repaint();
                panelTitle = "Lỗi Hiển Thị";
            }
        }

        setTitle(panelTitle + " - Health App");
        cardLayout.show(mainPanelContainer, panelNameCard);
    }

    public void performLogout() {
        SessionManager.getInstance().logout();
        this.dispose();

        if (this.authService != null && this.userDataDAO != null) {
            SwingUtilities.invokeLater(() -> {
                new LoginFrame(this.authService, this.userDataDAO).setVisible(true);
            });
        } else {
            System.err.println("LỖI NGHIÊM TRỌNG: Không thể khởi tạo lại LoginFrame do authService hoặc userDataDAO là null. Vui lòng khởi động lại ứng dụng.");
            JOptionPane.showMessageDialog(null, "Lỗi nghiêm trọng khi đăng xuất. Vui lòng khởi động lại ứng dụng.", "Lỗi Đăng Xuất", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}