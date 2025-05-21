// package com.kimquyen.healthapp; // Hoặc package gốc của bạn
package com.kimquyen.healthapp;

// Import tất cả các DAO
import com.kimquyen.healthapp.dao.AccountDAO;
import com.kimquyen.healthapp.dao.SponsorDAO;
import com.kimquyen.healthapp.dao.HraQuestionDAO;
import com.kimquyen.healthapp.dao.HraResponseDAO;
import com.kimquyen.healthapp.dao.UserDataDAO;
import com.kimquyen.healthapp.dao.UserAssessmentAttemptDAO;

// Import tất cả các Service
import com.kimquyen.healthapp.service.AssessmentService;
import com.kimquyen.healthapp.service.AuthService;
import com.kimquyen.healthapp.service.BCryptPasswordHashingServiceImpl;
import com.kimquyen.healthapp.service.PasswordHashingService;
import com.kimquyen.healthapp.service.QuestionService;
import com.kimquyen.healthapp.service.SponsorService; // Đã import
import com.kimquyen.healthapp.service.UserService;

// Import UI
import com.kimquyen.healthapp.ui.LoginFrame;
// import com.kimquyen.healthapp.ui.*; // Có thể bỏ nếu không dùng wildcard cho UI ở đây

import javax.swing.SwingUtilities;

public class MainApp {

    // Khai báo các DAO và Service là public static
    public static AccountDAO accountDAO;
    public static UserDataDAO userDataDAO;
    public static HraQuestionDAO hraQuestionDAO;
    public static HraResponseDAO hraResponseDAO;
    public static UserAssessmentAttemptDAO userAssessmentAttemptDAO;
    public static SponsorDAO sponsorDAO; // Chỉ cần một lần khai báo

    public static PasswordHashingService passwordHashingService;
    public static AuthService authService;
    public static UserService userService;
    public static QuestionService questionService;
    public static AssessmentService assessmentService;
    public static SponsorService sponsorService;

    public static void main(String[] args) {
        // 1. Khởi tạo DAOs
        accountDAO = new AccountDAO();
        userDataDAO = new UserDataDAO();
        hraQuestionDAO = new HraQuestionDAO();
        hraResponseDAO = new HraResponseDAO();
        userAssessmentAttemptDAO = new UserAssessmentAttemptDAO();
        sponsorDAO = new SponsorDAO(); // Chỉ cần một lần khởi tạo

        // 2. Khởi tạo PasswordHashingService
        passwordHashingService = new BCryptPasswordHashingServiceImpl();

        // 3. Khởi tạo Services
        authService = new AuthService(accountDAO, userDataDAO, passwordHashingService);
        // <<==== SỬA Ở ĐÂY: Truyền sponsorDAO vào constructor của UserService ====>>
        userService = new UserService(userDataDAO, accountDAO, passwordHashingService, sponsorDAO);
        questionService = new QuestionService(hraQuestionDAO);
        assessmentService = new AssessmentService(hraQuestionDAO, hraResponseDAO, userAssessmentAttemptDAO);
        sponsorService = new SponsorService(sponsorDAO, userDataDAO);

        // 4. Khởi chạy UI trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new LoginFrame(authService, userDataDAO).setVisible(true);
        });
    }

    // Các getter tĩnh (tùy chọn)
    // public static AuthService getAuthService() { return authService; }
    // public static UserService getUserService() { return userService; }
    // ... (và các service/DAO khác nếu cần)
    // public static SponsorService getSponsorService() { return sponsorService; }
}