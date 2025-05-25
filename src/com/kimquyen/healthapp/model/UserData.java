package com.kimquyen.healthapp.model;

import java.sql.Timestamp;

public class UserData {
    private int id;
    private String name;
    private int sponsorId;
    private Timestamp createdAt;

    public UserData() {
    }

    public UserData(int id, String name, int sponsorId, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.sponsorId = sponsorId;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSponsorId() {
        return sponsorId;
    }

    public void setSponsorId(int sponsorId) {
        this.sponsorId = sponsorId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UserData{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", sponsorId=" + sponsorId +
               ", createdAt=" + createdAt +
               '}';
    }
}