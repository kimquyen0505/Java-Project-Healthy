// com/kimquyen/healthapp/dao/HraQuestionDAO.java
package com.kimquyen.healthapp.dao;

import com.kimquyen.healthapp.model.HraQuestion;
import com.kimquyen.healthapp.model.HraQuestion.OptionChoice; // Đảm bảo import
import com.kimquyen.healthapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HraQuestionDAO {

    public List<HraQuestion> getAllQuestions() {
        Map<Integer, HraQuestion> questionMap = new LinkedHashMap<>();
        String sql = "SELECT question_id, type, title, text, options, score " +
                     "FROM hihi.hra_qna_scores ORDER BY question_id ASC, options ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int qId = rs.getInt("question_id");
                HraQuestion question = questionMap.get(qId);
                if (question == null) {
                    question = new HraQuestion(qId, rs.getString("type"), rs.getString("title"), rs.getString("text"));
                    questionMap.put(qId, question);
                }
                String optionLabel = rs.getString("options");
                int optionScore = rs.getInt("score");
                boolean scoreWasNull = rs.wasNull();
                if (HraQuestion.TYPE_SINGLE_CHOICE.equalsIgnoreCase(question.getType()) ||
                    HraQuestion.TYPE_MULTIPLE_CHOICE.equalsIgnoreCase(question.getType())) {
                    if (optionLabel != null && !optionLabel.trim().isEmpty()) {
                        String optionValue = optionLabel.trim().toLowerCase().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_]", "");
                        if (optionValue.isEmpty()) optionValue = "opt_" + qId + "_" + (question.getChoices().size() + 1);
                        question.addChoice(new HraQuestion.OptionChoice(optionValue, optionLabel.trim(), scoreWasNull ? 0 : optionScore));
                    }
                } else if (HraQuestion.TYPE_TEXT_INPUT.equalsIgnoreCase(question.getType())) {
                    if (question.getGeneralScore() == null && !scoreWasNull) {
                         question.setGeneralScore(optionScore);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả HraQuestions (đã nhóm): " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>(questionMap.values());
    }

    public HraQuestion getQuestionById(int questionId) {
        HraQuestion question = null;
        String sql = "SELECT question_id, type, title, text, options, score " +
                     "FROM hihi.hra_qna_scores WHERE question_id = ? ORDER BY options ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                if (question == null) {
                    question = new HraQuestion(rs.getInt("question_id"), rs.getString("type"), rs.getString("title"), rs.getString("text"));
                }
                String optionLabel = rs.getString("options");
                int optionScore = rs.getInt("score");
                boolean scoreWasNull = rs.wasNull();
                if (HraQuestion.TYPE_SINGLE_CHOICE.equalsIgnoreCase(question.getType()) ||
                    HraQuestion.TYPE_MULTIPLE_CHOICE.equalsIgnoreCase(question.getType())) {
                    if (optionLabel != null && !optionLabel.trim().isEmpty()) {
                        String optionValue = optionLabel.trim().toLowerCase().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_]", "");
                        if (optionValue.isEmpty()) optionValue = "opt_" + question.getQuestionId() + "_" + (question.getChoices().size() + 1);
                        question.addChoice(new HraQuestion.OptionChoice(optionValue, optionLabel.trim(), scoreWasNull ? 0 : optionScore));
                    }
                } else if (HraQuestion.TYPE_TEXT_INPUT.equalsIgnoreCase(question.getType())) {
                    if (question.getGeneralScore() == null && !scoreWasNull) {
                        question.setGeneralScore(optionScore);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy HraQuestion theo id (" + questionId + "): " + e.getMessage());
            e.printStackTrace();
        }
        if (question == null) System.err.println("Không tìm thấy câu hỏi với ID: " + questionId);
        return question;
    }

    private int getNextQuestionId(Connection conn) throws SQLException {
        String sql = "SELECT MAX(question_id) FROM hihi.hra_qna_scores";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
            return 1; 
        }
    }

    public boolean addQuestion(HraQuestion question) {
        if (question == null) return false;
        Connection conn = null;
        String insertSql = "INSERT INTO hihi.hra_qna_scores (question_id, type, title, text, options, score) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); 

            int questionIdToUse = getNextQuestionId(conn); 
            question.setQuestionId(questionIdToUse); 

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                if (HraQuestion.TYPE_TEXT_INPUT.equals(question.getType())) {
                    pstmt.setInt(1, questionIdToUse);
                    pstmt.setString(2, question.getType());
                    pstmt.setString(3, question.getTitle());
                    pstmt.setString(4, question.getText());
                    pstmt.setNull(5, Types.VARCHAR); 
                    if (question.getGeneralScore() != null) {
                        pstmt.setInt(6, question.getGeneralScore());
                    } else {
                        pstmt.setNull(6, Types.INTEGER);
                    }
                    pstmt.addBatch();
                } else { 
                    if (question.getChoices() == null || question.getChoices().isEmpty()) {
                        conn.rollback();
                        System.err.println("DAO: Câu hỏi trắc nghiệm phải có lựa chọn.");
                        return false;
                    }
                    for (OptionChoice choice : question.getChoices()) {
                        pstmt.setInt(1, questionIdToUse);
                        pstmt.setString(2, question.getType());
                        pstmt.setString(3, question.getTitle()); 
                        pstmt.setString(4, question.getText());
                        pstmt.setString(5, choice.getOptionLabel()); 
                        pstmt.setInt(6, choice.getOptionScore());
                        pstmt.addBatch();
                    }
                }
                pstmt.executeBatch();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi thêm HraQuestion (ID dự kiến: " + question.getQuestionId() + "): " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Lỗi khi rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Lỗi khi đóng connection: " + ex.getMessage());
                }
            }
        }
    }

    public boolean updateQuestion(HraQuestion question) {
        if (question == null || question.getQuestionId() == 0) {
             System.err.println("DAO: Câu hỏi hoặc ID không hợp lệ để cập nhật.");
            return false;
        }
        Connection conn = null;
        String insertSql = "INSERT INTO hihi.hra_qna_scores (question_id, type, title, text, options, score) VALUES (?, ?, ?, ?, ?, ?)";
        String deleteSql = "DELETE FROM hihi.hra_qna_scores WHERE question_id = ?";

        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); 

            try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
                deletePstmt.setInt(1, question.getQuestionId());
                deletePstmt.executeUpdate();
            }

            try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                if (HraQuestion.TYPE_TEXT_INPUT.equals(question.getType())) {
                    insertPstmt.setInt(1, question.getQuestionId());
                    insertPstmt.setString(2, question.getType());
                    insertPstmt.setString(3, question.getTitle());
                    insertPstmt.setString(4, question.getText());
                    insertPstmt.setNull(5, Types.VARCHAR);
                    if (question.getGeneralScore() != null) {
                        insertPstmt.setInt(6, question.getGeneralScore());
                    } else {
                        insertPstmt.setNull(6, Types.INTEGER);
                    }
                    insertPstmt.addBatch();
                } else {
                     if (question.getChoices() == null || question.getChoices().isEmpty()) {
                        conn.rollback();
                        System.err.println("DAO: Câu hỏi trắc nghiệm phải có lựa chọn khi cập nhật.");
                        return false;
                    }
                    for (OptionChoice choice : question.getChoices()) {
                        insertPstmt.setInt(1, question.getQuestionId());
                        insertPstmt.setString(2, question.getType());
                        insertPstmt.setString(3, question.getTitle());
                        insertPstmt.setString(4, question.getText());
                        insertPstmt.setString(5, choice.getOptionLabel());
                        insertPstmt.setInt(6, choice.getOptionScore());
                        insertPstmt.addBatch();
                    }
                }
                insertPstmt.executeBatch();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi cập nhật HraQuestion ID " + question.getQuestionId() + ": " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Lỗi khi rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Lỗi khi đóng connection: " + ex.getMessage());
                }
            }
        }
    }

    public boolean deleteQuestion(int questionId) {
        if (questionId <= 0) return false;
        String sql = "DELETE FROM hihi.hra_qna_scores WHERE question_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa HraQuestion ID " + questionId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}