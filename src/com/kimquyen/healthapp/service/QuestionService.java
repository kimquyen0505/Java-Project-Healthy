// package com.kimquyen.healthapp.service;
package com.kimquyen.healthapp.service; // Sử dụng package của bạn

import com.kimquyen.healthapp.dao.HraQuestionDAO; // Đảm bảo HraQuestionDAO tồn tại
import com.kimquyen.healthapp.model.HraQuestion; // Đảm bảo HraQuestion model tồn tại

import java.util.List;

public class QuestionService { // Đây là một lớp, không phải interface
    private final HraQuestionDAO hraQuestionDAO;

    // Constructor nhận HraQuestionDAO
    public QuestionService(HraQuestionDAO hraQuestionDAO) {
        if (hraQuestionDAO == null) {
            throw new IllegalArgumentException("HraQuestionDAO không được null khi khởi tạo QuestionService.");
        }
        this.hraQuestionDAO = hraQuestionDAO;
    }

    public List<HraQuestion> getAllQuestions() {
        return hraQuestionDAO.getAllQuestions(); // Giả sử DAO có phương thức này
    }

    public boolean addQuestion(HraQuestion question) {
        if (question == null || question.getText() == null || question.getText().trim().isEmpty()) { // Sửa: thường kiểm tra getText() hơn là getTitle() cho nội dung chính
            System.err.println("SERVICE: Nội dung câu hỏi không được để trống.");
            return false;
        }
        // Thêm các validation khác nếu cần
        return hraQuestionDAO.addQuestion(question); // Giả sử DAO có phương thức này
    }

    public boolean updateQuestion(HraQuestion question) {
        if (question == null || question.getQuestionId() == 0 ||
            question.getText() == null || question.getText().trim().isEmpty()) {
            System.err.println("SERVICE: Thông tin câu hỏi không hợp lệ để cập nhật.");
            return false;
        }
        return hraQuestionDAO.updateQuestion(question); // Giả sử DAO có phương thức này
    }

    public boolean deleteQuestion(int questionId) {
        if (questionId <= 0) { // ID thường là số dương
            System.err.println("SERVICE: Question ID không hợp lệ để xóa.");
            return false;
        }
        // Cân nhắc logic kiểm tra ràng buộc trước khi xóa
        return hraQuestionDAO.deleteQuestion(questionId); // Giả sử DAO có phương thức này
    }

    // Thêm phương thức này nếu ManageQuestionsPanel hoặc các phần khác cần
    public HraQuestion getQuestionById(int questionId) {
        if (questionId <= 0) {
            return null;
        }
        return hraQuestionDAO.getQuestionById(questionId); // Giả sử DAO có phương thức này
    }
}