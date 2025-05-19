package com.kimquyen.healthapp.model;


import java.sql.Timestamp;
import java.util.Map;

public class AssessmentResult {
    private UserData user; // Thông tin người dùng thực hiện đánh giá
    private Timestamp assessmentDate; // Thời điểm thực hiện đánh giá
    private Map<HraQuestion, String> responses; // Map câu hỏi -> câu trả lời của người dùng
    private int totalScore; // Tổng điểm của bài đánh giá
    private String riskLevel; // Mức độ rủi ro (ví dụ: "Low", "Medium", "High")

    // Constructors
    public AssessmentResult() {
    }

    public AssessmentResult(UserData user, Timestamp assessmentDate, Map<HraQuestion, String> responses, int totalScore, String riskLevel) {
        this.user = user;
        this.assessmentDate = assessmentDate;
        this.responses = responses;
        this.totalScore = totalScore;
        this.riskLevel = riskLevel;
    }

    // Getters and Setters
    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    public Timestamp getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(Timestamp assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public Map<HraQuestion, String> getResponses() {
        return responses;
    }

    public void setResponses(Map<HraQuestion, String> responses) {
        this.responses = responses;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    // toString
    @Override
    public String toString() {
        return "AssessmentResult{" +
               "user=" + (user != null ? user.getName() : "null") + // Tránh NullPointerException
               ", assessmentDate=" + assessmentDate +
               ", responsesCount=" + (responses != null ? responses.size() : 0) +
               ", totalScore=" + totalScore +
               ", riskLevel='" + riskLevel + '\'' +
               '}';
    }
}