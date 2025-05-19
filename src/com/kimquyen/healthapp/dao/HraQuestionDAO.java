package com.kimquyen.healthapp.dao;

<<<<<<< HEAD
import com.kimquyen.healthapp.model.HraQuestion; // Quan trọng: Model HraQuestion phải có các hằng số TYPE_...
=======


import com.kimquyen.healthapp.model.HraQuestion;
>>>>>>> 189b9f1304aa56286711462bbdb00d9c490eeb4e
import com.kimquyen.healthapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
<<<<<<< HEAD
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HraQuestionDAO {

    public List<HraQuestion> getAllQuestions() {
        Map<Integer, HraQuestion> questionMap = new LinkedHashMap<>();
        String sql = "SELECT question_id, type, title, text, options, score " +
                     "FROM hihi.hra_qna_scores ORDER BY question_id ASC, score DESC, options ASC"; // Sắp xếp thêm theo score để ưu tiên dòng có điểm (nếu có)

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int qId = rs.getInt("question_id");
                HraQuestion question = questionMap.get(qId);

                if (question == null) {
                    question = new HraQuestion(
                        qId,
                        rs.getString("type"), // DB phải có giá trị chuẩn hóa: SINGLE_CHOICE, TEXT_INPUT
                        rs.getString("title"),
                        rs.getString("text")
                    );
                    questionMap.put(qId, question);
                }

                String optionLabel = rs.getString("options");
                int optionScore = rs.getInt("score");
                boolean scoreWasNull = rs.wasNull();

                if (HraQuestion.TYPE_SINGLE_CHOICE.equalsIgnoreCase(question.getType()) ||
                    HraQuestion.TYPE_MULTIPLE_CHOICE.equalsIgnoreCase(question.getType())) {
                    if (optionLabel != null && !optionLabel.trim().isEmpty()) {
                        // Tạo value đơn giản từ label. Cân nhắc cột value riêng trong DB.
                        String optionValue = optionLabel.trim().toLowerCase()
                                .replaceAll("\\s+", "_")
                                .replaceAll("[^a-zA-Z0-9_]", "");
                        if (optionValue.isEmpty()) { // Xử lý trường hợp label chỉ có ký tự đặc biệt
                            optionValue = "opt_" + qId + "_" + (question.getChoices().size() + 1);
                        }
                        question.addChoice(new HraQuestion.OptionChoice(optionValue, optionLabel.trim(), scoreWasNull ? 0 : optionScore));
                    }
                } else if (HraQuestion.TYPE_TEXT_INPUT.equalsIgnoreCase(question.getType())) {
                    // Cho TEXT_INPUT, score (nếu có) từ DB được coi là generalScore
                    // Chỉ gán một lần (từ dòng đầu tiên có score cho question_id này)
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
        Map<Integer, HraQuestion> questionMap = new LinkedHashMap<>(); // Dùng map để nhóm nếu có nhiều options
        String sql = "SELECT question_id, type, title, text, options, score " +
                     "FROM hihi.hra_qna_scores WHERE question_id = ? ORDER BY options ASC";

=======
import java.util.List;

public class HraQuestionDAO {

    public HraQuestion getQuestionById(int questionId) {
        String sql = "SELECT question_id, type, title, text, options, score FROM hra_qna_scores WHERE question_id = ?";
>>>>>>> 189b9f1304aa56286711462bbdb00d9c490eeb4e
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();

<<<<<<< HEAD
            HraQuestion question = null;
            while (rs.next()) { // Lặp qua tất cả các dòng (options) cho cùng một questionId
                if (question == null) { // Khởi tạo đối tượng HraQuestion ở dòng đầu tiên
                    question = new HraQuestion(
                        rs.getInt("question_id"),
                        rs.getString("type"),
                        rs.getString("title"),
                        rs.getString("text")
                    );
                }
                // Xử lý options và score cho dòng hiện tại
                String optionLabel = rs.getString("options");
                int optionScore = rs.getInt("score");
                boolean scoreWasNull = rs.wasNull();

                if (HraQuestion.TYPE_SINGLE_CHOICE.equalsIgnoreCase(question.getType()) ||
                    HraQuestion.TYPE_MULTIPLE_CHOICE.equalsIgnoreCase(question.getType())) {
                    if (optionLabel != null && !optionLabel.trim().isEmpty()) {
                        String optionValue = optionLabel.trim().toLowerCase().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_]", "");
                         if (optionValue.isEmpty()) {
                            optionValue = "opt_" + question.getQuestionId() + "_" + (question.getChoices().size() + 1);
                        }
                        question.addChoice(new HraQuestion.OptionChoice(optionValue, optionLabel.trim(), scoreWasNull ? 0 : optionScore));
                    }
                } else if (HraQuestion.TYPE_TEXT_INPUT.equalsIgnoreCase(question.getType())) {
                    if (question.getGeneralScore() == null && !scoreWasNull) {
                        question.setGeneralScore(optionScore);
                    }
                }
            }
            return question; // Trả về đối tượng question đã được điền đầy đủ choices

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy HraQuestion theo id (" + questionId + "): " + e.getMessage());
=======
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
>>>>>>> 189b9f1304aa56286711462bbdb00d9c490eeb4e
            e.printStackTrace();
        }
        return null;
    }

<<<<<<< HEAD
    // --- CÁC PHƯƠNG THỨC CRUD CÂU HỎI (add, update, delete) ---
    // Việc triển khai đầy đủ các phương thức này với cấu trúc DB "mỗi option một dòng"
    // và model HraQuestion (có List<OptionChoice>) là phức tạp và cần transaction.
    // Dưới đây là dạng sườn, bạn cần tự hoàn thiện nếu chức năng quản lý câu hỏi là cần thiết.

    public boolean addQuestion(HraQuestion question) {
        if (question == null) return false;
        // Bước 1: Xác định question_id mới (ví dụ: SELECT MAX(question_id) + 1)
        // Bước 2: Bắt đầu transaction
        // Bước 3: Nếu là TEXT_INPUT, INSERT một dòng với options=NULL, score=question.getGeneralScore()
        // Bước 4: Nếu là SINGLE/MULTIPLE_CHOICE:
        //         Lặp qua question.getChoices():
        //              INSERT một dòng cho mỗi choice (cùng question_id, type, title, text nhưng khác options, score)
        // Bước 5: Commit transaction (hoặc rollback nếu lỗi)
        System.err.println("HraQuestionDAO.addQuestion chưa được triển khai đầy đủ cho cấu trúc DB hiện tại.");
        return false;
    }

    public boolean updateQuestion(HraQuestion question) {
        if (question == null) return false;
        // Bước 1: Bắt đầu transaction
        // Bước 2: UPDATE thông tin chung (type, title, text) cho tất cả các dòng có question_id = question.getQuestionId()
        // Bước 3: DELETE tất cả các dòng options cũ của question_id đó.
        // Bước 4: INSERT lại các dòng options mới từ question.getChoices() (tương tự addQuestion)
        // Bước 5: Commit transaction (hoặc rollback)
        System.err.println("HraQuestionDAO.updateQuestion chưa được triển khai đầy đủ.");
        return false;
    }

    public boolean deleteQuestion(int questionId) {
        String sql = "DELETE FROM hihi.hra_qna_scores WHERE question_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa các dòng HraQuestion cho ID: " + questionId);
=======
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
>>>>>>> 189b9f1304aa56286711462bbdb00d9c490eeb4e
            e.printStackTrace();
            return false;
        }
    }
}