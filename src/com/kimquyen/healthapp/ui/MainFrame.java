// package com.kimquyen.healthapp.ui;
package com.kimquyen.healthapp.ui;

// Import các DAO và Service
import com.kimquyen.healthapp.dao.AccountDAO;
import com.kimquyen.healthapp.dao.UserDataDAO;
import com.kimquyen.healthapp.service.AssessmentService;
import com.kimquyen.healthapp.service.AuthService;
import com.kimquyen.healthapp.service.QuestionService;
import com.kimquyen.healthapp.service.UserService;

// Import các Model và Util
import com.kimquyen.healthapp.model.Account;
import com.kimquyen.healthapp.model.Role;
import com.kimquyen.healthapp.util.SessionManager;

// Import Swing components
import javax.swing.*;
import java.awt.*;
// ActionEvent và ActionListener không cần import trực tiếp nếu chỉ dùng lambda
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private AuthService authService;
    private UserService userService;
    private QuestionService questionService;
    private AssessmentService assessmentService;
    private UserDataDAO userDataDAO;
    private AccountDAO accountDAO;

    private UserDashboardPanel userDashboardPanel;
    private AdminDashboardPanel adminDashboardPanel;
    private TakeAssessmentPanel takeAssessmentPanel;
    private ManageUsersPanel manageUsersPanel;
    private ManageQuestionsPanel manageQuestionsPanel; // Khai báo nếu bạn sẽ dùng
    // private ManageSponsorsPanel manageSponsorsPanel; // Khai báo nếu bạn sẽ dùng
    private ViewHistoryPanel viewHistoryPanel;
    private JPanel mainPanelContainer;
    private CardLayout cardLayout;

    private JMenuBar menuBar;
    private JMenu adminManageMenu = null;

    public static final String USER_DASHBOARD_CARD = "UserDashboard";
    public static final String ADMIN_DASHBOARD_CARD = "AdminDashboard";
    public static final String TAKE_ASSESSMENT_CARD = "TakeAssessment";
    public static final String MANAGE_USERS_CARD = "ManageUsers";
    public static final String MANAGE_QUESTIONS_CARD = "ManageQuestions";
    public static final String MANAGE_SPONSORS_CARD = "ManageSponsors"; 
    public static final String VIEW_HISTORY_CARD = "ViewHistoryPanel";


    public MainFrame(AuthService authService, UserService userService,
                     QuestionService questionService, AssessmentService assessmentService,
                     UserDataDAO userDataDAO, AccountDAO accountDAO) {
        // Gán các service và DAO
        this.authService = authService;
        this.userService = userService;
        this.questionService = questionService;
        this.assessmentService = assessmentService;
        this.userDataDAO = userDataDAO;
        this.accountDAO = accountDAO;

        setTitle("Health Response Application");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        determineInitialPanel();
    }

    private void initComponents() {
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Tệp");
        fileMenu.setMnemonic('T'); // Phím tắt Alt+T
        JMenuItem logoutMenuItem = new JMenuItem("Đăng xuất");
        logoutMenuItem.setMnemonic('X'); // Phím tắt Alt+X
        logoutMenuItem.addActionListener(e -> performLogout());
        fileMenu.add(logoutMenuItem);
        JMenuItem exitMenuItem = new JMenuItem("Thoát"); // Thêm nút thoát
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

        cardLayout = new CardLayout();
        mainPanelContainer = new JPanel(cardLayout);
        viewHistoryPanel = new ViewHistoryPanel(this, this.assessmentService); // Truyền MainFrame và AssessmentService
        mainPanelContainer.add(viewHistoryPanel, VIEW_HISTORY_CARD);

        // Khởi tạo các panel con
        userDashboardPanel = new UserDashboardPanel(this, this.assessmentService);
        adminDashboardPanel = new AdminDashboardPanel(this, this.userService, this.questionService, this.assessmentService); // Truyền đủ service
        takeAssessmentPanel = new TakeAssessmentPanel(this, this.assessmentService);
        manageUsersPanel = new ManageUsersPanel(this, this.userService);
        // manageQuestionsPanel = new ManageQuestionsPanel(this, this.questionService); // Bỏ comment và tạo lớp nếu cần
        // manageSponsorsPanel = new ManageSponsorsPanel(this, /*SponsorService nếu có*/); // Bỏ comment và tạo lớp nếu cần

        // Thêm các panel (card) vào mainPanelContainer
        mainPanelContainer.add(userDashboardPanel, USER_DASHBOARD_CARD);
        mainPanelContainer.add(adminDashboardPanel, ADMIN_DASHBOARD_CARD);
        mainPanelContainer.add(takeAssessmentPanel, TAKE_ASSESSMENT_CARD);
        mainPanelContainer.add(manageUsersPanel, MANAGE_USERS_CARD);
        // if (manageQuestionsPanel != null) mainPanelContainer.add(manageQuestionsPanel, MANAGE_QUESTIONS_CARD);
        // if (manageSponsorsPanel != null) mainPanelContainer.add(manageSponsorsPanel, MANAGE_SPONSORS_CARD);

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
            performLogout(); // Nên đưa về LoginFrame nếu không có session hợp lệ
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

            // JMenuItem manageSponsorsItem = new JMenuItem("Quản Lý Nhà Tài Trợ");
            // manageSponsorsItem.setMnemonic('S');
            // manageSponsorsItem.addActionListener(e -> showPanel(MANAGE_SPONSORS_CARD));
            // adminManageMenu.add(manageSponsorsItem);

            if (menuBar != null) {
                menuBar.add(adminManageMenu, menuBar.getMenuCount() -1); // Chèn trước menu "Trợ giúp"
                menuBar.revalidate();
                menuBar.repaint();
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

        String panelTitle = "Health Response Application"; // Tiêu đề mặc định

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
        } else if (VIEW_HISTORY_CARD.equals(panelNameCard) && viewHistoryPanel != null) {
            viewHistoryPanel.panelVisible(); // Gọi để tải dữ liệu lịch sử
            setTitle("Lịch Sử Đánh Giá - Health App");            
        } else if (MANAGE_QUESTIONS_CARD.equals(panelNameCard)) {
             if (manageQuestionsPanel == null) { // Khởi tạo nếu chưa có
                 // manageQuestionsPanel = new ManageQuestionsPanel(this, this.questionService);
                 // mainPanelContainer.add(manageQuestionsPanel, MANAGE_QUESTIONS_CARD);
                 System.err.println("ManageQuestionsPanel chưa được khởi tạo và thêm vào CardLayout!");
                 JOptionPane.showMessageDialog(this, "Chức năng Quản lý Câu hỏi đang phát triển.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                 return; // Không chuyển panel nếu chưa sẵn sàng
             }
            // manageQuestionsPanel.panelVisible();
            panelTitle = "Quản Lý Câu Hỏi";
        }
        // ... else if cho các panel khác (ManageSponsors, etc.) ...
        else {
            System.err.println("Cảnh báo MainFrame: Không tìm thấy panel với tên card '" + panelNameCard + "' hoặc panel là null.");
             // Có thể hiển thị một panel mặc định hoặc thông báo lỗi
            // cardLayout.show(mainPanelContainer, USER_DASHBOARD_CARD); // Ví dụ về panel mặc định
            // setTitle("Bảng Điều Khiển Người Dùng");
            // return;
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
            System.err.println("Không thể khởi tạo lại LoginFrame do thiếu service. Vui lòng khởi động lại ứng dụng.");
            System.exit(1);
        }
    }
}