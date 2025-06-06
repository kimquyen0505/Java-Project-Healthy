// package com.kimquyen.healthapp.ui;
package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.MainApp; // Quan trọng: Để truy cập các service/DAO static
import com.kimquyen.healthapp.model.Account;
import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.service.AuthService;
// Các service khác sẽ được lấy từ MainApp khi tạo MainFrame
import com.kimquyen.healthapp.dao.UserDataDAO; // UserDataDAO được truyền vào constructor
import com.kimquyen.healthapp.util.SessionManager;
import com.kimquyen.healthapp.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L; // Thêm để tránh cảnh báo

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    // Các service này được LoginFrame trực tiếp sử dụng
    private AuthService authService;
    private UserDataDAO userDataDAO;

    // Constructor nhận các service mà LoginFrame trực tiếp cần
    public LoginFrame(AuthService authService, UserDataDAO userDataDAO) {
        this.authService = authService;
        this.userDataDAO = userDataDAO;

        setTitle("Log In - Health Response App");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        // Tiêu đề (Mới)
        JLabel titleLabel = new JLabel("Log In Health App", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20)); // Hoặc UIConstants.FONT_TITLE
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 20, 0); // Khoảng cách dưới tiêu đề
        add(titleLabel, gbc);
        gbc.gridwidth = 1; // Reset gridwidth
        gbc.insets = new Insets(10, 10, 10, 10); // Reset insets


        // Username
        gbc.gridx = 0; gbc.gridy = 1; // Điều chỉnh gridy
        add(new JLabel("Name:"), gbc);
        usernameField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2; // Điều chỉnh gridy
        add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 2;
        add(passwordField, gbc);

        // Login Button
        loginButton = new JButton("Log In");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14)); // Hoặc UIConstants.FONT_BUTTON
        loginButton.setPreferredSize(new Dimension(120, 35)); // Hoặc UIConstants.DIM_BUTTON_STANDARD
        gbc.gridx = 0; gbc.gridy = 3; // Điều chỉnh gridy
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(20, 10, 10, 10);
        add(loginButton, gbc);


        // ActionListener cho nút đăng nhập
        loginButton.addActionListener(e -> performLogin());

        // ActionListener cho phím Enter trên các trường text
        ActionListener enterLoginAction = e -> performLogin();
        usernameField.addActionListener(enterLoginAction);
        passwordField.addActionListener(enterLoginAction);
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Sử dụng ValidationUtil
        if (ValidationUtil.isNullOrEmpty(username)) {
            JOptionPane.showMessageDialog(this, "Please enter your username.", "Login Error", JOptionPane.WARNING_MESSAGE);
            usernameField.requestFocus();
            return;
        }
        if (ValidationUtil.isNullOrEmpty(password)) {
            JOptionPane.showMessageDialog(this, "Please enter your password.", "login Error", JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocus();
            return;
        }

        Account loggedInAccount = authService.login(username, password);

        if (loggedInAccount != null) {
            UserData loggedInUserData = null;
            if (loggedInAccount.getUserId() != 0) {
                loggedInUserData = userDataDAO.getUserById(loggedInAccount.getUserId());
            }

            if (loggedInUserData != null) {
                SessionManager.getInstance().login(loggedInAccount, loggedInUserData);
                JOptionPane.showMessageDialog(this, "Login successful! Welcome " + loggedInUserData.getName() + ".", "Login successful", JOptionPane.INFORMATION_MESSAGE);

                MainFrame mainFrame = new MainFrame(
                    MainApp.authService,
                    MainApp.userService,
                    MainApp.questionService,
                    MainApp.assessmentService,
                    MainApp.sponsorService,   
                    MainApp.userDataDAO,
                    MainApp.accountDAO
                );
                mainFrame.setVisible(true);
                this.dispose();

            } else {
                JOptionPane.showMessageDialog(this, "Login successful with the account '" + loggedInAccount.getUsername() + "',\nbut user information (UserData) linked with (ID:  " + loggedInAccount.getUserId() + ").\n  was not found.", "User Data Error", JOptionPane.ERROR_MESSAGE);
                SessionManager.getInstance().logout();
            }
        } else {
            JOptionPane.showMessageDialog(this, "The username or password is incorrect. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

}