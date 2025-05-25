package com.kimquyen.healthapp.ui; 

import com.kimquyen.healthapp.model.Account;
import com.kimquyen.healthapp.model.Role;
import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.service.UserService;
import com.kimquyen.healthapp.util.ValidationUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent; 
import javax.swing.event.DocumentListener; 
import java.awt.*;

public class AddEditUserDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private UserService userService;
    private UserData currentUserData;
    private Account currentAccount;
    private boolean isEditMode;
    private boolean succeeded = false;

    // Components
    private JTextField nameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<Role> roleComboBox;
    private JTextField sponsorIdField;

    private JButton saveButton;
    private JButton cancelButton;

    private JLabel confirmPasswordLabel;
    private JPanel formPanel; 

    public AddEditUserDialog(Frame parent, UserService userService, UserData userDataToEdit, Account accountToEdit) {
        super(parent, "Thông Tin Người Dùng", true);
        this.userService = userService;
        this.currentUserData = userDataToEdit;
        this.currentAccount = accountToEdit;
        this.isEditMode = (userDataToEdit != null && accountToEdit != null);

        setTitle(isEditMode ? "Sửa Thông Tin Người Dùng" : "Thêm Người Dùng Mới");
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tên Đầy Đủ
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Tên Đầy Đủ (*):"), gbc);
        nameField = new JTextField(25);
        if (isEditMode && currentUserData != null) {
            nameField.setText(currentUserData.getName());
        }
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        formPanel.add(nameField, gbc);
        gbc.weightx = 0;

        // Tên Đăng Nhập
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Tên Đăng Nhập (*):"), gbc);
        usernameField = new JTextField(25);
        if (isEditMode && currentAccount != null) {
            usernameField.setText(currentAccount.getUsername());
            usernameField.setEditable(false);
        }
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(usernameField, gbc);

        // Mật Khẩu
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel(isEditMode ? "Mật khẩu mới (để trống nếu không đổi):" : "Mật khẩu (*):"), gbc);
        passwordField = new JPasswordField(25);
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(passwordField, gbc);

        // Xác Nhận Mật Khẩu
        gbc.gridx = 0; gbc.gridy = 3;
        confirmPasswordLabel = new JLabel("Xác nhận MK (* nếu thêm mới):"); // Khởi tạo biến instance
        formPanel.add(confirmPasswordLabel, gbc);

        confirmPasswordField = new JPasswordField(25);
        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(confirmPasswordField, gbc);

        if (isEditMode) {
            confirmPasswordLabel.setVisible(false);
            confirmPasswordField.setVisible(false);
        }

        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { checkPasswordFields(); }
            public void removeUpdate(DocumentEvent e) { checkPasswordFields(); }
            public void changedUpdate(DocumentEvent e) { checkPasswordFields(); }
        });

        // Vai Trò
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Vai trò (*):"), gbc);
        roleComboBox = new JComboBox<>(Role.values());
        if (isEditMode && currentAccount != null && currentAccount.getRole() != null) {
            roleComboBox.setSelectedItem(currentAccount.getRole());
        } else {
            roleComboBox.setSelectedItem(Role.USER);
        }
        gbc.gridx = 1; gbc.gridy = 4;
        formPanel.add(roleComboBox, gbc);

        // Sponsor ID
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Sponsor ID (Số, để trống nếu không có):"), gbc);
        sponsorIdField = new JTextField(10);
        if (isEditMode && currentUserData != null && currentUserData.getSponsorId() != 0) {
            sponsorIdField.setText(String.valueOf(currentUserData.getSponsorId()));
        }
        gbc.gridx = 1; gbc.gridy = 5;
        formPanel.add(sponsorIdField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // --- Panel Nút ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Lưu");
        saveButton.setPreferredSize(new Dimension(80, 30));
        cancelButton = new JButton("Hủy");
        cancelButton.setPreferredSize(new Dimension(80, 30));

        saveButton.addActionListener(e -> performSave());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Phương thức checkPasswordFields sử dụng các biến instance
    private void checkPasswordFields() {

        boolean passwordFieldHasText = passwordField.getPassword().length > 0;
        boolean showConfirm = !isEditMode || (isEditMode && passwordFieldHasText);

        confirmPasswordLabel.setVisible(showConfirm);
        confirmPasswordField.setVisible(showConfirm);

        if (isEditMode && !passwordFieldHasText) {
            confirmPasswordLabel.setVisible(false);
            confirmPasswordField.setVisible(false);
        }

        if (formPanel != null) {
            formPanel.revalidate();
            formPanel.repaint();
        }
    }


    private void performSave() {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        Role role = (Role) roleComboBox.getSelectedItem();
        String sponsorIdStr = sponsorIdField.getText().trim();

        if (ValidationUtil.isNullOrEmpty(name)) {
            JOptionPane.showMessageDialog(this, "Tên đầy đủ không được để trống.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }
        if (ValidationUtil.isNullOrEmpty(username)) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập không được để trống.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE);
            usernameField.requestFocus();
            return;
        }
        if (!isEditMode && ValidationUtil.isNullOrEmpty(password)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu không được để trống khi thêm người dùng mới.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocus();
            return;
        }

        if (confirmPasswordField.isVisible() && !password.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                 JOptionPane.showMessageDialog(this, "Mật khẩu và xác nhận mật khẩu không khớp.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE);
                confirmPasswordField.requestFocus();
                return;
            }
        } else if (!isEditMode && password.isEmpty() != confirmPassword.isEmpty() && !password.equals(confirmPassword) ) {
             JOptionPane.showMessageDialog(this, "Khi thêm mới, mật khẩu và xác nhận mật khẩu phải được nhập và khớp.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE);
            confirmPasswordField.requestFocus();
            return;
        }

        if (role == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một vai trò.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE);
            roleComboBox.requestFocus();
            return;
        }

        Integer sponsorId = null;
        if (!sponsorIdStr.isEmpty()) {
            if (ValidationUtil.isValidInteger(sponsorIdStr)) {
                sponsorId = Integer.parseInt(sponsorIdStr);
                if (sponsorId < 0) {
                     JOptionPane.showMessageDialog(this, "Sponsor ID phải là số không âm.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE);
                     sponsorIdField.requestFocus();
                     return;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Sponsor ID không hợp lệ (phải là số).", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE);
                sponsorIdField.requestFocus();
                return;
            }
        }

        boolean operationSuccess = false;
        if (isEditMode) {
            currentUserData.setName(name);
            currentUserData.setSponsorId(sponsorId != null ? sponsorId : 0);

            Account accountChanges = new Account();
            accountChanges.setUsername(currentAccount.getUsername());
            accountChanges.setRole(role);
            accountChanges.setUserId(currentAccount.getUserId());

            if (!password.isEmpty()) {
                accountChanges.setPassword(password);
            } else {
                accountChanges.setPassword(null); 
            }
            operationSuccess = userService.updateUser(currentUserData, accountChanges);

        } else { 
            UserData newUser = new UserData();
            newUser.setName(name);
            if (sponsorId != null) {
                newUser.setSponsorId(sponsorId);
            }
            operationSuccess = userService.createUser(newUser, username, password, role);
        }

        if (operationSuccess) {
            this.succeeded = true;
            JOptionPane.showMessageDialog(this,
                    isEditMode ? "Cập nhật người dùng thành công!" : "Thêm người dùng mới thành công!",
                    "Thành Công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    isEditMode ? "Cập nhật người dùng thất bại." : "Thêm người dùng mới thất bại.\nUsername có thể đã tồn tại hoặc có lỗi khác.",
                    "Lỗi Thao Tác", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}