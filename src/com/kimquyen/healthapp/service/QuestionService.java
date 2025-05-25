package com.kimquyen.healthapp.service; 

import com.kimquyen.healthapp.dao.HraQuestionDAO;
 import com.kimquyen.healthapp.model.HraQuestion; 

import java.util.List;

public class QuestionService { 
    private final HraQuestionDAO hraQuestionDAO;

    public QuestionService(HraQuestionDAO hraQuestionDAO) {
        if (hraQuestionDAO == null) {
            throw new IllegalArgumentException("HraQuestionDAO không được null khi khởi tạo QuestionService.");
        }
        this.hraQuestionDAO = hraQuestionDAO;
    }

    public List<HraQuestion> getAllQuestions() {
        return hraQuestionDAO.getAllQuestions(); 
    }

    public HraQuestion getQuestionById(int questionId) {
        if (questionId <= 0) {
            System.err.println("SERVICE (getQuestionById): Question ID '" + questionId + "' không hợp lệ.");
            return null;
        }
        return hraQuestionDAO.getQuestionById(questionId); 
    }

    public boolean addQuestion(HraQuestion question) {
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
        String type = question.getType();
        if (HraQuestion.TYPE_SINGLE_CHOICE.equals(type) || HraQuestion.TYPE_MULTIPLE_CHOICE.equals(type)) {
            if (question.getChoices() == null || question.getChoices().isEmpty()) {
                System.err.println("SERVICE (addQuestion): Câu hỏi trắc nghiệm (SINGLE/MULTIPLE_CHOICE) phải có ít nhất một lựa chọn.");
                return false;
            }
        } else if (HraQuestion.TYPE_TEXT_INPUT.equals(type)) {
            if (question.getGeneralScore() == null) {
                System.err.println("SERVICE (addQuestion): Câu hỏi dạng TEXT_INPUT phải có điểm chung (generalScore).");
                return false;
            }
        } else {
            System.err.println("SERVICE (addQuestion): Loại câu hỏi '" + type + "' không được hỗ trợ.");
            return false;
        }

        return hraQuestionDAO.addQuestion(question);
    }

    public boolean updateQuestion(HraQuestion question) {
        if (question == null) {
            System.err.println("SERVICE (updateQuestion): Đối tượng câu hỏi là null.");
            return false;
        }
        if (question.getQuestionId() == 0) {
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

        return hraQuestionDAO.updateQuestion(question);
    }

    public boolean deleteQuestion(int questionId) {
        if (questionId <= 0) {
            System.err.println("SERVICE (deleteQuestion): Question ID '" + questionId + "' không hợp lệ để xóa.");
            return false;
        }

        return hraQuestionDAO.deleteQuestion(questionId);
    }
}