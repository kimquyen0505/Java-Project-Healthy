package com.kimquyen.healthapp; // Hoặc package gốc của bạn

// Import tất cả các DAO
import com.kimquyen.healthapp.dao.AccountDAO;
import com.kimquyen.healthapp.dao.HraQuestionDAO;
import com.kimquyen.healthapp.dao.HraResponseDAO;
// import com.kimquyen.healthapp.dao.SponsorDAO; // Nếu có
import com.kimquyen.healthapp.dao.UserDataDAO;

// Import tất cả các Service
import com.kimquyen.healthapp.service.AssessmentService;
import com.kimquyen.healthapp.service.AuthService;
import com.kimquyen.healthapp.service.BCryptPasswordHashingServiceImpl; // Giả sử bạn đã tạo lớp này
import com.kimquyen.healthapp.service.PasswordHashingService;
import com.kimquyen.healthapp.service.QuestionService;
import com.kimquyen.healthapp.service.UserService;

// Import UI
import com.kimquyen.healthapp.ui.LoginFrame;

import javax.swing.SwingUtilities;

public class MainApp {

    // Khai báo các DAO và Service là public static để dễ dàng truy cập từ các phần khác
    // LƯU Ý: Trong ứng dụng lớn, nên sử dụng Dependency Injection thay vì static fields.
    public static AccountDAO accountDAO;
    public static UserDataDAO userDataDAO;
    public static HraQuestionDAO hraQuestionDAO;
    public static HraResponseDAO hraResponseDAO;
    // public static SponsorDAO sponsorDAO;

    public static PasswordHashingService passwordHashingService;
    public static AuthService authService;
    public static UserService userService;
    public static QuestionService questionService;
    public static AssessmentService assessmentService;

    public static void main(String[] args) {
        // 1. Khởi tạo DAOs
        accountDAO = new AccountDAO();
        userDataDAO = new UserDataDAO();
        hraQuestionDAO = new HraQuestionDAO();
        hraResponseDAO = new HraResponseDAO();
        // sponsorDAO = new SponsorDAO(); // Khởi tạo nếu có

        // 2. Khởi tạo PasswordHashingService (SỬ DỤNG TRIỂN KHAI THỰC TẾ)
        passwordHashingService = new BCryptPasswordHashingServiceImpl(); // Quan trọng!

        // 3. Khởi tạo Services
        // Truyền các DAO đã khởi tạo vào constructor của Service
        authService = new AuthService(accountDAO, userDataDAO, passwordHashingService);
        userService = new UserService(userDataDAO, accountDAO, passwordHashingService);
        questionService = new QuestionService(hraQuestionDAO);
        assessmentService = new AssessmentService(hraQuestionDAO, hraResponseDAO);
        // Khởi tạo các service khác nếu có


        // 4. Khởi chạy UI trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Tạo LoginFrame VÀ TRUYỀN CÁC SERVICE CẦN THIẾT CHO NÓ
            // LoginFrame hiện tại chỉ cần authService và userDataDAO trong constructor của nó
            new LoginFrame(authService, userDataDAO).setVisible(true);
        });
    }

    // Tùy chọn: Bạn có thể tạo các getter tĩnh nếu không muốn để các trường là public.
    // Tuy nhiên, nếu đã là public static, LoginFrame có thể truy cập trực tiếp.
    // public static AuthService getAuthService() { return authService; }
    // public static UserService getUserService() { return userService; }
    // public static QuestionService getQuestionService() { return questionService; }
    // public static AssessmentService getAssessmentService() { return assessmentService; }
    // public static UserDataDAO getUserDataDAO() { return userDataDAO; }
    // public static AccountDAO getAccountDAO() { return accountDAO; }
}