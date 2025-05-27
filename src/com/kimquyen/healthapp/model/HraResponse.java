package com.kimquyen.healthapp.model;

import java.sql.Timestamp;

public class HraResponse {
    private int id;
    private int userId; 
    private int questionId; 
    private String response; 
    private Timestamp createdAt;

    public HraResponse() {
    }

    public HraResponse(int id, int userId, int questionId, String response, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.questionId = questionId;
        this.response = response;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "HraResponse{" +
               "id=" + id +
               ", userId=" + userId +
               ", questionId=" + questionId +
               ", response='" + response + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}