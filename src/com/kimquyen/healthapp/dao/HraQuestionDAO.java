package com.kimquyen.healthapp.dao;



import com.kimquyen.healthapp.model.HraQuestion;
import com.kimquyen.healthapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HraQuestionDAO {

    public HraQuestion getQuestionById(int questionId) {
        String sql = "SELECT question_id, type, title, text, options, score FROM hra_qna_scores WHERE question_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new HraQuestion(
                    rs.getInt("question_id"),
                    rs.getString("type"),
                    rs.getString("title"),
                    rs.getString("text"),
                    rs.getString("options"),
                    rs.getInt("score")
                );
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy HraQuestion theo id: " + questionId);
            e.printStackTrace();
        }
        return null;
    }

    public List<HraQuestion> getAllQuestions() {
        List<HraQuestion> questions = new ArrayList<>();
        String sql = "SELECT question_id, type, title, text, options, score FROM hra_qna_scores ORDER BY question_id ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                HraQuestion question = new HraQuestion(
                    rs.getInt("question_id"),
                    rs.getString("type"),
                    rs.getString("title"),
                    rs.getString("text"),
                    rs.getString("options"),
                    rs.getInt("score")
                );
                questions.add(question);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả HraQuestions");
            e.printStackTrace();
        }
        return questions;
    }

    public boolean addQuestion(HraQuestion question) {
        // Giả sử question_id là tự tăng hoặc bạn sẽ cung cấp nó
        String sql = "INSERT INTO hra_qna_scores (question_id, type, title, text, options, score) VALUES (?, ?, ?, ?, ?, ?)";
        if(question.getQuestionId() == 0) { // Nếu không có ID, giả sử DB tự tăng
             sql = "INSERT INTO hra_qna_scores (type, title, text, options, score) VALUES (?, ?, ?, ?, ?)";
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            if(question.getQuestionId() != 0) {
                pstmt.setInt(paramIndex++, question.getQuestionId());
            }
            pstmt.setString(paramIndex++, question.getType());
            pstmt.setString(paramIndex++, question.getTitle());
            pstmt.setString(paramIndex++, question.getText());
            pstmt.setString(paramIndex++, question.getOptions());
            pstmt.setInt(paramIndex++, question.getScore());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm HraQuestion: " + question.getTitle());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateQuestion(HraQuestion question) {
        String sql = "UPDATE hra_qna_scores SET type = ?, title = ?, text = ?, options = ?, score = ? WHERE question_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, question.getType());
            pstmt.setString(2, question.getTitle());
            pstmt.setString(3, question.getText());
            pstmt.setString(4, question.getOptions());
            pstmt.setInt(5, question.getScore());
            pstmt.setInt(6, question.getQuestionId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật HraQuestion: " + question.getQuestionId());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteQuestion(int questionId) {
        String sql = "DELETE FROM hra_qna_scores WHERE question_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, questionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa HraQuestion: " + questionId);
            e.printStackTrace();
            return false;
        }
    }
}