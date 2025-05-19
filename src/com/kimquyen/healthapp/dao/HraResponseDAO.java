package com.kimquyen.healthapp.dao;


import com.kimquyen.healthapp.model.HraResponse;
import com.kimquyen.healthapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HraResponseDAO {

    public HraResponse getResponseById(int responseId) {
        String sql = "SELECT id, user_id, question_id, response, created_at FROM hra_responses WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, responseId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new HraResponse(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("question_id"),
                    rs.getString("response"),
                    rs.getTimestamp("created_at")
                );
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy HraResponse theo id: " + responseId);
            e.printStackTrace();
        }
        return null;
    }

    public List<HraResponse> getResponsesByUserId(int userId) {
        List<HraResponse> responses = new ArrayList<>();
        String sql = "SELECT id, user_id, question_id, response, created_at FROM hra_responses WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                HraResponse response = new HraResponse(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("question_id"),
                    rs.getString("response"),
                    rs.getTimestamp("created_at")
                );
                responses.add(response);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy HraResponses theo user_id: " + userId);
            e.printStackTrace();
        }
        return responses;
    }

    public List<HraResponse> getAllResponses() {
        List<HraResponse> responses = new ArrayList<>();
        String sql = "SELECT id, user_id, question_id, response, created_at FROM hra_responses ORDER BY created_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                HraResponse response = new HraResponse(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("question_id"),
                    rs.getString("response"),
                    rs.getTimestamp("created_at")
                );
                responses.add(response);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả HraResponses");
            e.printStackTrace();
        }
        return responses;
    }

    public boolean addResponse(HraResponse hraResponse) {
        // Giả sử id là tự tăng trong DB
        String sql = "INSERT INTO hra_responses (user_id, question_id, response, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hraResponse.getUserId());
            pstmt.setInt(2, hraResponse.getQuestionId());
            pstmt.setString(3, hraResponse.getResponse());
            pstmt.setTimestamp(4, hraResponse.getCreatedAt() != null ? hraResponse.getCreatedAt() : new Timestamp(System.currentTimeMillis()));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm HraResponse cho user: " + hraResponse.getUserId());
            e.printStackTrace();
            return false;
        }
    }

    // Các phương thức update/delete cho HraResponse có thể không cần thiết
    // tùy theo logic nghiệp vụ (thường thì câu trả lời đã gửi sẽ không sửa/xóa)
    // Nếu cần, bạn có thể thêm:
    // public boolean updateResponse(HraResponse hraResponse) { ... }
    // public boolean deleteResponse(int responseId) { ... }
}
