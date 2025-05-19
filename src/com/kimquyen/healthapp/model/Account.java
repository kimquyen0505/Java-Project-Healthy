package com.kimquyen.healthapp.model;

public class Account {
    private String username;
    private String password;
    private Role role;
    private int userId; // Trường này đã đúng

    // Constructors
    public Account() {
        // Constructor mặc định
    }

    // SỬA CONSTRUCTOR NÀY ĐỂ NHẬN 4 THAM SỐ
    public Account(String username, String password, Role role, int userIdParam) { // Đổi tên tham số để rõ ràng
        this.username = username;
        this.password = password;
        this.role = role;
        this.userId = userIdParam; // Gán giá trị từ tham số userIdParam vào trường this.userId
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    // toString (for debugging)
    @Override
    public String toString() {
        return "Account{" +
               "username='" + username + '\'' +
               // ", password='" + "********" + '\'' + // Che mật khẩu
               ", role=" + role +
               ", userId=" + userId + // Thêm userId vào toString để dễ debug
               '}';
    }
}