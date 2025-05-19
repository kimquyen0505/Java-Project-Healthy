package com.kimquyen.healthapp.model;


public class Sponsor {
    private int id;
    private String name;

    // Constructors
    public Sponsor() {
    }

    public Sponsor(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
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

    // toString
    @Override
    public String toString() {
        return "Sponsor{" +
               "id=" + id +
               ", name='" + name + '\'' +
               '}';
    }
}