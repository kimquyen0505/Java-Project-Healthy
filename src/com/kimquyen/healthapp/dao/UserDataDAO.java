package com.kimquyen.healthapp.dao;

import com.kimquyen.healthapp.model.UserData;

import java.util.Map; 
import java.util.HashMap;
import com.kimquyen.healthapp.util.DatabaseUtil;
import com.kimquyen.healthapp.util.ValidationUtil;

import java.util.LinkedHashMap;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDataDAO {

	public UserData getUserById(int userId) {
	    String sql = "SELECT id, name, sponsor_id, created_at FROM users_data WHERE id = ?";
	    System.out.println("DAO DEBUG: UserDataDAO.getUserById - SQL: " + sql + " - Parameter: " + userId); // Giữ lại dòng debug này
	    UserData user = null;
	    try (Connection conn = DatabaseUtil.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, userId);
	        try (ResultSet rs = pstmt.executeQuery()) { 
	            if (rs.next()) {
	                user = new UserData(); 
	                user.setId(rs.getInt("id"));
	                user.setName(rs.getString("name"));
	                user.setSponsorId(rs.getInt("sponsor_id")); 
	                if (rs.wasNull()) { 	                   
	                }
	                user.setCreatedAt(rs.getTimestamp("created_at"));

	                System.out.println("DAO DEBUG: UserDataDAO.getUserById - Tìm thấy và tạo UserData: " + (user != null ? user.getName() : "null object created?"));
	            } else {
	                System.out.println("DAO DEBUG: UserDataDAO.getUserById - KHÔNG tìm thấy UserData cho id: " + userId);
	            }
	        } 
	    } catch (SQLException e) {
	        System.err.println("Lỗi SQL trong UserDataDAO.getUserById cho id " + userId + ": " + e.getMessage());
	        e.printStackTrace();
	    }
	    return user; 
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

    public UserData addUser(UserData user) { 
        String sql = "INSERT INTO users_data (name, sponsor_id, created_at) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getName());
            if (user.getSponsorId() == 0) { 
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
                        return user; 
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm UserData: " + user.getName());
            e.printStackTrace();
        }
        return null; 
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
    public List<UserData> getUsersBySponsorId(int sponsorId) {
        List<UserData> users = new ArrayList<>();
        String sql = "SELECT id, name, sponsor_id, created_at FROM users_data WHERE sponsor_id = ? ORDER BY name ASC";
        System.out.println("DAO DEBUG: UserDataDAO.getUsersBySponsorId - SQL: " + sql + " - Parameter: " + sponsorId);
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sponsorId);
            ResultSet rs = pstmt.executeQuery();

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
            System.err.println("Lỗi khi lấy UserData theo sponsor_id: " + sponsorId);
            e.printStackTrace();
        }
        if (users.isEmpty()) {
            System.out.println("DAO DEBUG: UserDataDAO.getUsersBySponsorId - KHÔNG tìm thấy UserData cho sponsor_id: " + sponsorId);
        }
        return users;
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
    
    public Map<Integer, Long> countUsersBySponsorId() {
        Map<Integer, Long> counts = new HashMap<>();
        String sql = "SELECT sponsor_id, COUNT(*) as user_count FROM users_data GROUP BY sponsor_id";
        System.out.println("DAO DEBUG: UserDataDAO.countUsersBySponsorId - SQL: " + sql);

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int sponsorId = rs.getInt("sponsor_id"); 
                long userCount = rs.getLong("user_count");
                counts.put(sponsorId, userCount);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi đếm người dùng theo sponsor_id: " + e.getMessage());
            e.printStackTrace();
        }
        if (counts.isEmpty()) {
             System.out.println("DAO DEBUG: UserDataDAO.countUsersBySponsorId - Không tìm thấy dữ liệu đếm.");
        }
        return counts;
    }
    
    public Map<String, Long> countNewUsersByMonth() {
        Map<String, Long> userCountsByMonth = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(created_at, '%Y-%m') as creation_month, COUNT(*) as user_count " +
                     "FROM users_data " +
                     "GROUP BY creation_month " +
                     "ORDER BY creation_month ASC"; 
        System.out.println("DAO DEBUG: UserDataDAO.countNewUsersByMonth - SQL: " + sql);

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String monthYear = rs.getString("creation_month");
                long count = rs.getLong("user_count");
                if (monthYear != null) { 
                    userCountsByMonth.put(monthYear, count);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi đếm người dùng mới theo tháng: " + e.getMessage());
            e.printStackTrace();
        }
        if (userCountsByMonth.isEmpty()) {
            System.out.println("DAO DEBUG: UserDataDAO.countNewUsersByMonth - Không tìm thấy dữ liệu đếm.");
        }
        return userCountsByMonth;
    }
}