package com.kimquyen.healthapp.dao;

import com.kimquyen.healthapp.model.UserAssessmentAttempt;
import com.kimquyen.healthapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class UserAssessmentAttemptDAO {

    // GIỮ LẠI MỘT PHIÊN BẢN CỦA getAllAttempts()
    public List<UserAssessmentAttempt> getAllAttempts() {
        List<UserAssessmentAttempt> attempts = new ArrayList<>();
        String sql = "SELECT attempt_id, user_data_id, assessment_date, total_score, risk_level " +
                     "FROM user_assessment_attempts ORDER BY assessment_date DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UserAssessmentAttempt attempt = new UserAssessmentAttempt(
                        rs.getInt("attempt_id"),
                        rs.getInt("user_data_id"),
                        rs.getTimestamp("assessment_date"),
                        rs.getInt("total_score"),
                        rs.getString("risk_level")
                );
                attempts.add(attempt);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả UserAssessmentAttempts");
            e.printStackTrace();
        }
        return attempts;
    }

    // XÓA PHIÊN BẢN TRÙNG LẶP CỦA getAllAttempts() Ở ĐÂY

    /**
     * Đếm số lượng bài đánh giá theo từng mức độ rủi ro.
     * @return Map với key là risk_level (String) và value là số lượng (Long).
     */
    public Map<String, Long> countAttemptsByRiskLevel() {
        Map<String, Long> counts = new HashMap<>();
        String sql = "SELECT risk_level, COUNT(*) as count FROM user_assessment_attempts GROUP BY risk_level";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String riskLevel = rs.getString("risk_level");
                long count = rs.getLong("count");
                if (riskLevel != null && !riskLevel.trim().isEmpty()) { // Bỏ qua nếu risk_level là null/rỗng
                    counts.put(riskLevel, count);
                } else {
                    counts.put("Chưa xác định", count); // Hoặc một label khác cho null/rỗng
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đếm UserAssessmentAttempts theo risk_level");
            e.printStackTrace();
        }
        return counts;
    }

    /**
     * Lưu một bản ghi kết quả làm bài vào database.
     * @param attempt Đối tượng UserAssessmentAttempt cần lưu.
     * @return true nếu lưu thành công, false nếu thất bại.
     */
    public boolean saveAttempt(UserAssessmentAttempt attempt) {
        String sql = "INSERT INTO user_assessment_attempts (user_data_id, assessment_date, total_score, risk_level) " +
                     "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, attempt.getUserDataId());
            pstmt.setTimestamp(2, attempt.getAssessmentDate());
            pstmt.setInt(3, attempt.getTotalScore());
            pstmt.setString(4, attempt.getRiskLevel());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Lấy ID tự tăng (attempt_id) nếu cần
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        attempt.setAttemptId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu UserAssessmentAttempt cho user ID: " + attempt.getUserDataId());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy tất cả các lần làm bài của một người dùng, sắp xếp theo ngày mới nhất trước.
     * @param userId ID của người dùng.
     * @return Danh sách các UserAssessmentAttempt.
     */
    public List<UserAssessmentAttempt> getAttemptsByUserId(int userId) {
        List<UserAssessmentAttempt> attempts = new ArrayList<>();
        String sql = "SELECT attempt_id, user_data_id, assessment_date, total_score, risk_level " +
                     "FROM user_assessment_attempts WHERE user_data_id = ? " +
                     "ORDER BY assessment_date DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                UserAssessmentAttempt attempt = new UserAssessmentAttempt(
                        rs.getInt("attempt_id"),
                        rs.getInt("user_data_id"),
                        rs.getTimestamp("assessment_date"),
                        rs.getInt("total_score"),
                        rs.getString("risk_level")
                );
                attempts.add(attempt);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy UserAssessmentAttempt cho user ID: " + userId);
            e.printStackTrace();
        }
        return attempts;
    }

    // (Tùy chọn) Thêm các phương thức khác nếu cần:
    // - getAttemptById(int attemptId)
    // - deleteAttempt(int attemptId) // Đã có getAllAttempts() rồi
}