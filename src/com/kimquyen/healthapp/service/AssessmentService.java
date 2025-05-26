package com.kimquyen.healthapp.service;

import com.kimquyen.healthapp.dao.HraQuestionDAO;
import com.kimquyen.healthapp.dao.HraResponseDAO;
import com.kimquyen.healthapp.dao.UserAssessmentAttemptDAO;
import com.kimquyen.healthapp.model.AssessmentResult;
import com.kimquyen.healthapp.model.HraQuestion;
import com.kimquyen.healthapp.model.HraResponse;
import com.kimquyen.healthapp.model.UserAssessmentAttempt;
import com.kimquyen.healthapp.model.UserData;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap; 
import java.util.List;
import java.util.Map;

public class AssessmentService {
    private final HraQuestionDAO hraQuestionDAO;
    private final HraResponseDAO hraResponseDAO;
    private final UserAssessmentAttemptDAO userAssessmentAttemptDAO;

    public AssessmentService(HraQuestionDAO hraQuestionDAO, HraResponseDAO hraResponseDAO, UserAssessmentAttemptDAO userAssessmentAttemptDAO) {
        if (hraQuestionDAO == null || hraResponseDAO == null || userAssessmentAttemptDAO == null) {
            throw new IllegalArgumentException("DAOs không được null khi khởi tạo AssessmentService.");
        }
        this.hraQuestionDAO = hraQuestionDAO;
        this.hraResponseDAO = hraResponseDAO;
        this.userAssessmentAttemptDAO = userAssessmentAttemptDAO;
    }

    public List<HraQuestion> getAssessmentQuestions() {
        List<HraQuestion> questions = hraQuestionDAO.getAllQuestions();
        return (questions != null) ? questions : Collections.emptyList();
    }

    public AssessmentResult submitAssessment(UserData user, Map<HraQuestion, String> responsesMap) {
        if (user == null || user.getId() == 0 || responsesMap == null) {
            System.err.println("SERVICE (submitAssessment): Thông tin người dùng hoặc câu trả lời không hợp lệ.");
            return null;
        }

        Timestamp submissionTime = new Timestamp(System.currentTimeMillis());
        int totalScore = 0;
        Map<HraQuestion, String> detailedResponsesForReport = new HashMap<>();

        for (Map.Entry<HraQuestion, String> entry : responsesMap.entrySet()) {
            HraQuestion questionFromUI = entry.getKey();
            String userResponseString = entry.getValue();

            HraResponse hraResponse = new HraResponse();
            hraResponse.setUserId(user.getId());
            hraResponse.setQuestionId(questionFromUI.getQuestionId());
            hraResponse.setResponse(userResponseString);
            hraResponse.setCreatedAt(submissionTime);

            int pointsForThisQuestion = calculatePointsForResponse(questionFromUI, userResponseString);
            totalScore += pointsForThisQuestion;

            boolean saved = hraResponseDAO.addResponse(hraResponse);
            if (saved) {
                detailedResponsesForReport.put(questionFromUI, userResponseString);
            } else {
                System.err.println("SERVICE (submitAssessment): Không thể lưu câu trả lời cho câu hỏi ID: " + questionFromUI.getQuestionId());
            }
        }

        String riskLevel = determineRiskLevel(totalScore);
        AssessmentResult result = new AssessmentResult(user, submissionTime, detailedResponsesForReport, totalScore, riskLevel);

        UserAssessmentAttempt attemptToSave = new UserAssessmentAttempt();
        attemptToSave.setUserDataId(user.getId());
        attemptToSave.setAssessmentDate(submissionTime);
        attemptToSave.setTotalScore(totalScore);
        attemptToSave.setRiskLevel(riskLevel);

        boolean attemptSaved = userAssessmentAttemptDAO.saveAttempt(attemptToSave);
        if (!attemptSaved) {
            System.err.println("SERVICE WARNING (submitAssessment): Không thể lưu kết quả tổng thể của bài đánh giá cho user ID: " + user.getId());
        } else {
            System.out.println("Kết quả tổng thể bài đánh giá đã được lưu với attempt_id: " + attemptToSave.getAttemptId());
        }
        return result;
    }

    private int calculatePointsForResponse(HraQuestion question, String userResponseString) {
        if (question == null || userResponseString == null) {
            return 0;
        }
        int points = 0;
        String questionType = question.getType();

        if (HraQuestion.TYPE_SINGLE_CHOICE.equalsIgnoreCase(questionType)) {
            if (question.getChoices() != null) {
                for (HraQuestion.OptionChoice choice : question.getChoices()) {
                    if (choice.getOptionValue().equals(userResponseString)) {
                        points = choice.getOptionScore();
                        break;
                    }
                }
            }
        } else if (HraQuestion.TYPE_MULTIPLE_CHOICE.equalsIgnoreCase(questionType)) {
            if (!userResponseString.trim().isEmpty() && question.getChoices() != null) {
                List<String> selectedValues = Arrays.asList(userResponseString.split(","));
                List<String> trimmedSelectedValues = new ArrayList<>();
                for(String s : selectedValues){
                    trimmedSelectedValues.add(s.trim());
                }

                for (HraQuestion.OptionChoice choice : question.getChoices()) {
                    if (trimmedSelectedValues.contains(choice.getOptionValue().trim())) {
                        points += choice.getOptionScore();
                    }
                }
            }
        } else if (HraQuestion.TYPE_TEXT_INPUT.equalsIgnoreCase(questionType)) {
            if (!userResponseString.trim().isEmpty() && question.getGeneralScore() != null) {
                points = question.getGeneralScore();
            }
        }
        return points;
    }

    public List<AssessmentResult> getUserAssessmentHistory(UserData user) {
        if (user == null || user.getId() == 0) {
            return Collections.emptyList();
        }

        List<UserAssessmentAttempt> attempts = userAssessmentAttemptDAO.getAttemptsByUserId(user.getId());
        if (attempts == null || attempts.isEmpty()) {
            return Collections.emptyList();
        }

        List<AssessmentResult> historyResults = new ArrayList<>();
        for (UserAssessmentAttempt attempt : attempts) {
            AssessmentResult summaryResult = new AssessmentResult(
                    user,
                    attempt.getAssessmentDate(),
                    null,
                    attempt.getTotalScore(),
                    attempt.getRiskLevel()
            );
            historyResults.add(summaryResult);
        }
        return historyResults;
    }

    private String determineRiskLevel(int totalScore) {
        if (totalScore < 20) return "Rất Thấp";
        if (totalScore < 40) return "Thấp";
        if (totalScore < 60) return "Trung Bình";
        if (totalScore < 80) return "Cao";
        return "Rất Cao";
    }

    public List<HraResponse> getAllResponsesForAllUsers() {
        List<HraResponse> responses = hraResponseDAO.getAllResponses();
        return (responses != null) ? responses : Collections.emptyList();
    }

    public Map<String, Long> getRiskLevelDistribution() {
        if (userAssessmentAttemptDAO == null) {
            System.err.println("SERVICE (getRiskLevelDistribution): userAssessmentAttemptDAO is null!");
            return Collections.emptyMap();
        }
        return userAssessmentAttemptDAO.countAttemptsByRiskLevel();
    }

    public Map<String, Long> getAssessmentCountByMonth() {
        if (userAssessmentAttemptDAO == null) {
            System.err.println("SERVICE (getAssessmentCountByMonth): userAssessmentAttemptDAO is null!");
            return Collections.emptyMap();
        }

        List<UserAssessmentAttempt> allAttempts = userAssessmentAttemptDAO.getAllAttempts();
        if (allAttempts == null || allAttempts.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Long> countsByMonth = new LinkedHashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");

        allAttempts.sort(Comparator.comparing(UserAssessmentAttempt::getAssessmentDate));

        for (UserAssessmentAttempt attempt : allAttempts) {
            if (attempt.getAssessmentDate() != null) {
                String monthYearKey = monthFormat.format(attempt.getAssessmentDate());
                countsByMonth.put(monthYearKey, countsByMonth.getOrDefault(monthYearKey, 0L) + 1);
            }
        }
        return countsByMonth;

        
    }
    public Map<String, Long> getResponseDistributionForQuestion(int questionId) {
        if (hraQuestionDAO == null) { // Thêm kiểm tra null cho DAO nếu cần
            System.err.println("SERVICE (getResponseDistributionForQuestion): hraQuestionDAO is null!");
            return Collections.emptyMap();
        }
        if (hraResponseDAO == null) { // Thêm kiểm tra null cho DAO nếu cần
            System.err.println("SERVICE (getResponseDistributionForQuestion): hraResponseDAO is null!");
            return Collections.emptyMap();
        }

        // Tùy chọn: Lấy thông tin câu hỏi để làm tiêu đề biểu đồ đẹp hơn sau này,
        // hoặc để kiểm tra xem câu hỏi có tồn tại và là SINGLE_CHOICE không.
        // HraQuestion question = hraQuestionDAO.getQuestionById(questionId);
        // if (question == null || !HraQuestion.TYPE_SINGLE_CHOICE.equalsIgnoreCase(question.getType())) {
        //     System.err.println("SERVICE (getResponseDistributionForQuestion): Question ID " + questionId + " không tồn tại hoặc không phải SINGLE_CHOICE.");
        //     return Collections.emptyMap();
        // }

        return hraResponseDAO.countResponsesForSingleQuestion(questionId);
    }

} 