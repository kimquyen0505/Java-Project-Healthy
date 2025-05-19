package com.kimquyen.healthapp.dao;

import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.util.DatabaseUtil;
import com.kimquyen.healthapp.util.ValidationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDataDAO {

    public UserData getUserById(int userId) {
        String sql = "SELECT id, name, sponsor_id, created_at FROM users_data WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new UserData(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("sponsor_id"),
                    rs.getTimestamp("created_at")
                );
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy UserData theo id: " + userId);
            e.printStackTrace();
        }
        return null;
    }

    public List<UserData> getAllUsers() {
        List<UserData> users = new ArrayList<>();
        String sql = "SELECT id, name, sponsor_id, created_at FROM users_data ORDER BY name ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UserData user = new UserData(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("sponsor_id"),
                    rs.getTimestamp("created_at")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả UserData");
            e.printStackTrace();
        }
        return users;
    }

    public UserData addUser(UserData user) { // Trả về UserData với ID đã được tạo
        String sql = "INSERT INTO users_data (name, sponsor_id, created_at) VALUES (?, ?, ?)";
        // Sử dụng Statement.RETURN_GENERATED_KEYS để lấy ID tự tăng
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getName());
            if (user.getSponsorId() == 0) { // Giả sử 0 nghĩa là không có sponsor_id
                pstmt.setNull(2, Types.INTEGER);
            } else {
                pstmt.setInt(2, user.getSponsorId());
            }
            pstmt.setTimestamp(3, user.getCreatedAt() != null ? user.getCreatedAt() : new Timestamp(System.currentTimeMillis()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        return user; // Trả về user với ID đã được cập nhật
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm UserData: " + user.getName());
            e.printStackTrace();
        }
        return null; // Trả về null nếu thất bại
    }


    public boolean updateUser(UserData user) {
        String sql = "UPDATE users_data SET name = ?, sponsor_id = ?, created_at = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
             if (user.getSponsorId() == 0) {
                pstmt.setNull(2, Types.INTEGER);
            } else {
                pstmt.setInt(2, user.getSponsorId());
            }
            pstmt.setTimestamp(3, user.getCreatedAt());
            pstmt.setInt(4, user.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật UserData: " + user.getId());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users_data WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa UserData: " + userId);
            e.printStackTrace();
            return false;
        }
    }
}