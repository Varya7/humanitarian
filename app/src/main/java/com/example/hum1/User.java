package com.example.hum1;

public class User {
    private String role;
    private String centerName;
    // другие поля...

    public User() {}

    // Геттеры и сеттеры
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCenterName() { return centerName; }
    public void setCenterName(String centerName) { this.centerName = centerName; }
}