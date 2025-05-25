package com.kimquyen.healthapp.dao;

import com.kimquyen.healthapp.model.Account;
import com.kimquyen.healthapp.model.Role;
import com.kimquyen.healthapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    public Account getAccountByUsername(String username) {
        String sql = "SELECT id, username, password, role, user_data_fk_id FROM account WHERE username = ?"; 
        Account account = null;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String roleStringDb = rs.getString("role");
                Role accountRole = Role.fromString(roleStringDb);

                if (accountRole == null && roleStringDb != null && !roleStringDb.trim().isEmpty()) {
                    System.err.println("Cảnh báo DAO: Giá trị role không hợp lệ ('" + roleStringDb + "') từ DB cho username: " + username + ". Account role sẽ là null.");
                } else if (accountRole == null) {
                     System.err.println("Cảnh báo DAO: Giá trị role là NULL hoặc rỗng từ DB cho username: " + username + ". Account role sẽ là null.");
                }

                account = new Account(
                    rs.getString("username"),
                    rs.getString("password"),
                    accountRole,
                    rs.getInt("user_data_fk_id") 
                );
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy Account theo username: " + username);
            e.printStackTrace();
        }
        return account;
    }

    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT id, username, password, role, user_data_fk_id FROM account"; // Lấy cả id của account
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String roleStringDb = rs.getString("role");
                Role accountRole = Role.fromString(roleStringDb);

                if (accountRole == null && roleStringDb != null && !roleStringDb.trim().isEmpty()) {
                     System.err.println("Cảnh báo DAO (getAll): Giá trị role không hợp lệ ('" + roleStringDb + "') cho username: " + rs.getString("username") + ". Account role sẽ là null.");
                } else if (accountRole == null) {
                     System.err.println("Cảnh báo DAO (getAll): Giá trị role là NULL hoặc rỗng cho username: " + rs.getString("username") + ". Account role sẽ là null.");
                }

                Account account = new Account(
                    rs.getString("username"),
                    rs.getString("password"),
                    accountRole,
                    rs.getInt("user_data_fk_id") 
                );
          
                accounts.add(account);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả Accounts");
            e.printStackTrace();
        }
        return accounts;
    }

    public boolean addAccount(Account account) {
        String sql = "INSERT INTO account (username, password, role, user_data_fk_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getUsername());
            pstmt.setString(2, account.getPassword()); 

            if (account.getRole() != null) {
                pstmt.setString(3, account.getRole().name()); 
            } else {
                pstmt.setNull(3, Types.VARCHAR); 
                System.err.println("Cảnh báo DAO: Thêm Account với Role là null cho username: " + account.getUsername());
            }
           
            if (account.getUserId() != 0) { 
                pstmt.setInt(4, account.getUserId());
            } else {
                pstmt.setNull(4, Types.INTEGER); 
                System.err.println("Cảnh báo DAO: Thêm Account với user_data_fk_id là 0/NULL cho username: " + account.getUsername());
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException | NullPointerException e) { // Bắt cả NullPointerException nếu account.getRole() là null và không kiểm tra
            System.err.println("Lỗi khi thêm Account: " + (account != null ? account.getUsername() : "Đối tượng Account là null"));
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAccount(Account account) {

        String sql = "UPDATE account SET password = ?, role = ? WHERE username = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getPassword()); 
            if (account.getRole() != null) {
                pstmt.setString(2, account.getRole().name());
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }

            pstmt.setString(3, account.getUsername());


            return pstmt.executeUpdate() > 0;
        } catch (SQLException | NullPointerException e) {
            System.err.println("Lỗi khi cập nhật Account: " + (account != null ? account.getUsername() : "Đối tượng Account là null"));
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAccount(String username) {
        String sql = "DELETE FROM account WHERE username = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa Account: " + username);
            e.printStackTrace();
            return false;
        }
    }

    public Account getAccountByUserId(int userId) {
        String sql = "SELECT id, username, password, role, user_data_fk_id FROM account WHERE user_data_fk_id = ?";
        System.out.println("DAO DEBUG: AccountDAO.getAccountByUserId - SQL: " + sql + " - Parameter: " + userId);
        Account account = null;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String roleStringDb = rs.getString("role");
                    Role accountRole = Role.fromString(roleStringDb);
                 
                    account = new Account( 
                        rs.getString("username"),
                        rs.getString("password"),
                        accountRole,
                        rs.getInt("user_data_fk_id") 
                    );
                   
                    System.out.println("DAO DEBUG: AccountDAO.getAccountByUserId - Tìm thấy Account: " + account.getUsername());
                } else {
                    System.out.println("DAO DEBUG: AccountDAO.getAccountByUserId - KHÔNG tìm thấy Account cho user_data_fk_id: " + userId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL trong AccountDAO.getAccountByUserId cho user_data_fk_id " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return account;
    }
 }