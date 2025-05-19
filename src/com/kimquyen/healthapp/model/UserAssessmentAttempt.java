package com.kimquyen.healthapp.model;

import java.sql.Timestamp;

public class UserAssessmentAttempt {
    private int attemptId;
    private int userDataId;
    private Timestamp assessmentDate;
    private int totalScore;
    private String riskLevel;

    // (Tùy chọn) Có thể thêm đối tượng UserData nếu muốn load thông tin user cùng lúc
    // private UserData user;

    public UserAssessmentAttempt() {
    }

    public UserAssessmentAttempt(int attemptId, int userDataId, Timestamp assessmentDate, int totalScore, String riskLevel) {
        this.attemptId = attemptId;
        this.userDataId = userDataId;
        this.assessmentDate = assessmentDate;
        this.totalScore = totalScore;
        this.riskLevel = riskLevel;
    }

    // Getters and Setters
    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public int getUserDataId() {
        return userDataId;
    }

    public void setUserDataId(int userDataId) {
        this.userDataId = userDataId;
    }

    public Timestamp getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(Timestamp assessmentDate) {
        this.assessmentDate = assessmentDate;
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

    @Override
    public String toString() {
        return "UserAssessmentAttempt{" +
                "attemptId=" + attemptId +
                ", userDataId=" + userDataId +
                ", assessmentDate=" + assessmentDate +
                ", totalScore=" + totalScore +
                ", riskLevel='" + riskLevel + '\'' +
                '}';
    }
}