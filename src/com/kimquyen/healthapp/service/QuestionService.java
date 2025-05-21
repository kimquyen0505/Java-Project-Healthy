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

    public HraQuestion getQuestionById(int questionId) {
        if (questionId <= 0) {
            System.err.println("SERVICE (getQuestionById): Question ID '" + questionId + "' không hợp lệ.");
            return null;
        }
        return hraQuestionDAO.getQuestionById(questionId); // Giả sử DAO có phương thức này
    }

    public boolean addQuestion(HraQuestion question) {
        // Kiểm tra các điều kiện cơ bản
        if (question == null) {
            System.err.println("SERVICE (addQuestion): Đối tượng câu hỏi là null.");
            return false;
        }
        if (question.getText() == null || question.getText().trim().isEmpty()) {
            System.err.println("SERVICE (addQuestion): Nội dung câu hỏi không được để trống.");
            return false;
        }
        if (question.getType() == null || question.getType().trim().isEmpty()) {
            System.err.println("SERVICE (addQuestion): Loại câu hỏi không được để trống.");
            return false;
        }

        // Kiểm tra dựa trên loại câu hỏi
        String type = question.getType();
        if (HraQuestion.TYPE_SINGLE_CHOICE.equals(type) || HraQuestion.TYPE_MULTIPLE_CHOICE.equals(type)) {
            if (question.getChoices() == null || question.getChoices().isEmpty()) {
                System.err.println("SERVICE (addQuestion): Câu hỏi trắc nghiệm (SINGLE/MULTIPLE_CHOICE) phải có ít nhất một lựa chọn.");
                return false;
            }
            // Có thể thêm validation cho từng OptionChoice nếu cần (ví dụ: label không trống, score hợp lệ)
        } else if (HraQuestion.TYPE_TEXT_INPUT.equals(type)) {
            if (question.getGeneralScore() == null) {
                System.err.println("SERVICE (addQuestion): Câu hỏi dạng TEXT_INPUT phải có điểm chung (generalScore).");
                return false;
            }
        } else {
            System.err.println("SERVICE (addQuestion): Loại câu hỏi '" + type + "' không được hỗ trợ.");
            return false;
        }

        // Nếu tất cả validation đều qua, gọi DAO để thêm
        return hraQuestionDAO.addQuestion(question);
    }

    public boolean updateQuestion(HraQuestion question) {
        // Kiểm tra các điều kiện cơ bản
        if (question == null) {
            System.err.println("SERVICE (updateQuestion): Đối tượng câu hỏi là null.");
            return false;
        }
        if (question.getQuestionId() == 0) { // ID 0 thường không hợp lệ cho bản ghi đã tồn tại
            System.err.println("SERVICE (updateQuestion): Question ID không hợp lệ (0) để cập nhật.");
            return false;
        }
        if (question.getText() == null || question.getText().trim().isEmpty()) {
            System.err.println("SERVICE (updateQuestion): Nội dung câu hỏi không được để trống khi cập nhật.");
            return false;
        }
        if (question.getType() == null || question.getType().trim().isEmpty()) {
            System.err.println("SERVICE (updateQuestion): Loại câu hỏi không được để trống khi cập nhật.");
            return false;
        }

        // Kiểm tra dựa trên loại câu hỏi (tương tự như addQuestion)
        String type = question.getType();
        if (HraQuestion.TYPE_SINGLE_CHOICE.equals(type) || HraQuestion.TYPE_MULTIPLE_CHOICE.equals(type)) {
            if (question.getChoices() == null || question.getChoices().isEmpty()) {
                System.err.println("SERVICE (updateQuestion): Câu hỏi trắc nghiệm (SINGLE/MULTIPLE_CHOICE) phải có ít nhất một lựa chọn khi cập nhật.");
                return false;
            }
        } else if (HraQuestion.TYPE_TEXT_INPUT.equals(type)) {
            if (question.getGeneralScore() == null) {
                System.err.println("SERVICE (updateQuestion): Câu hỏi dạng TEXT_INPUT phải có điểm chung (generalScore) khi cập nhật.");
                return false;
            }
        } else {
            System.err.println("SERVICE (updateQuestion): Loại câu hỏi '" + type + "' không được hỗ trợ khi cập nhật.");
            return false;
        }

        // Nếu tất cả validation đều qua, gọi DAO để cập nhật
        return hraQuestionDAO.updateQuestion(question);
    }

    public boolean deleteQuestion(int questionId) {
        if (questionId <= 0) { // ID thường là số dương
            System.err.println("SERVICE (deleteQuestion): Question ID '" + questionId + "' không hợp lệ để xóa.");
            return false;
        }
        // Cân nhắc logic kiểm tra ràng buộc trước khi xóa ở đây nếu cần
        // Ví dụ: Kiểm tra xem câu hỏi có đang được sử dụng trong các bản ghi hra_responses không.
        // Tuy nhiên, việc này có thể làm chậm thao tác xóa và có thể được xử lý bởi ràng buộc khóa ngoại của DB.
        return hraQuestionDAO.deleteQuestion(questionId);
    }
}