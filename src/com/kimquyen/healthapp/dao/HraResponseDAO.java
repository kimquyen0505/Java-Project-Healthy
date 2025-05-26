package com.kimquyen.healthapp.dao;

import com.kimquyen.healthapp.model.HraResponse;
import com.kimquyen.healthapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Map<String, Long> countResponsesForSingleQuestion(int questionId) {
        Map<String, Long> counts = new HashMap<>();
        String sql = "SELECT response, COUNT(*) as count FROM hra_responses WHERE question_id = ? GROUP BY response";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String responseText = rs.getString("response");
                if (responseText != null) { // Quan trọng: Xử lý responseText có thể là null
                     counts.put(responseText, rs.getLong("count"));
                } else {
                     // Tùy chọn: Xử lý trường hợp response là NULL trong DB, ví dụ:
                     // counts.put("Chưa trả lời/NULL", counts.getOrDefault("Chưa trả lời/NULL", 0L) + rs.getLong("count"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi đếm câu trả lời cho câu hỏi ID " + questionId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return counts;
    }
}
