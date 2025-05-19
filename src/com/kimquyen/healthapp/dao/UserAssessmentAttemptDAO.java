package com.kimquyen.healthapp.dao;

import com.kimquyen.healthapp.model.UserAssessmentAttempt;
import com.kimquyen.healthapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserAssessmentAttemptDAO {

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
    // - getAllAttempts() (cho admin báo cáo)
    // - deleteAttempt(int attemptId)
}