package com.example.examapplication;

public class User {
    private String name;
    private String username;
    private String email;

    // Costruttore vuoto richiesto per Firebase
    public User() {
    }

    public User(String name, String username, String email) {
        this.name = name;
        this.username = username;
        this.email = email;
    }

    // Metodi getter e setter per gli attributi

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}