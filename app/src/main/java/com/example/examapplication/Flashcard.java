package com.example.examapplication;

public class Flashcard {
    private String id;
    private String question;
    private String answer;

    public Flashcard() { }

    public Flashcard(String id, String question, String answer) {
        this.id = id;
        this.question = question;
        this.answer = answer;
    }

    public String getId() { return id; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }

    public void setId(String id) { this.id = id; }
    public void setQuestion(String question) { this.question = question; }
    public void setAnswer(String answer) { this.answer = answer; }
}
