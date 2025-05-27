package com.kimquyen.healthapp;

import com.kimquyen.healthapp.dao.AccountDAO;
import com.kimquyen.healthapp.dao.SponsorDAO;
import com.kimquyen.healthapp.dao.HraQuestionDAO;
import com.kimquyen.healthapp.dao.HraResponseDAO;
import com.kimquyen.healthapp.dao.UserDataDAO;
import com.kimquyen.healthapp.dao.UserAssessmentAttemptDAO;

import com.kimquyen.healthapp.service.AssessmentService;
import com.kimquyen.healthapp.service.AuthService;
import com.kimquyen.healthapp.service.BCryptPasswordHashingServiceImpl;
import com.kimquyen.healthapp.service.PasswordHashingService;
import com.kimquyen.healthapp.service.QuestionService;
import com.kimquyen.healthapp.service.SponsorService;
import com.kimquyen.healthapp.service.UserService;

import javax.swing.UIManager;
import com.kimquyen.healthapp.ui.LoginFrame;
import javax.swing.SwingUtilities;

public class MainApp {

    // Khai báo các DAO và Service là public static
    public static AccountDAO accountDAO;
    public static UserDataDAO userDataDAO;
    public static HraQuestionDAO hraQuestionDAO;
    public static HraResponseDAO hraResponseDAO;
    public static UserAssessmentAttemptDAO userAssessmentAttemptDAO;
    public static SponsorDAO sponsorDAO;

    public static PasswordHashingService passwordHashingService;
    public static AuthService authService;
    public static UserService userService;
    public static QuestionService questionService;
    public static AssessmentService assessmentService;
    public static SponsorService sponsorService;

    public static void main(String[] args) {
        // 1. Thiết lập Look and Feel (nên làm đầu tiên)
        try {
           
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarculaLaf());
            System.out.println("FlatLaf Darcula Look and Feel được áp dụng thành công.");
        } catch (Exception ex) {
  
            System.err.println("Không thể khởi tạo FlatLaf, sử dụng L&F mặc định: " + ex.getMessage());

        }

        // 2. Khởi tạo DAOs
        accountDAO = new AccountDAO();
        userDataDAO = new UserDataDAO();
        hraQuestionDAO = new HraQuestionDAO();
        hraResponseDAO = new HraResponseDAO();
        userAssessmentAttemptDAO = new UserAssessmentAttemptDAO();
        sponsorDAO = new SponsorDAO();

        // 3. Khởi tạo PasswordHashingService
        passwordHashingService = new BCryptPasswordHashingServiceImpl();

        // 4. Khởi tạo Services
        authService = new AuthService(accountDAO, userDataDAO, passwordHashingService);
        userService = new UserService(userDataDAO, accountDAO, passwordHashingService, sponsorDAO);
        questionService = new QuestionService(hraQuestionDAO);
        assessmentService = new AssessmentService(hraQuestionDAO, hraResponseDAO, userAssessmentAttemptDAO);
        sponsorService = new SponsorService(sponsorDAO, userDataDAO);

        // 5. Khởi chạy UI trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new LoginFrame(authService, userDataDAO).setVisible(true);
        });
    }
}