package com.example.examapplication;

public class Exam {
    public String id;
    public String name;
    public long dateMillis;

    public Exam() {} // richiesto da Firebase

    public Exam(String id, String name, long dateMillis) {
        this.id = id;
        this.name = name;
        this.dateMillis = dateMillis;
    }
}
