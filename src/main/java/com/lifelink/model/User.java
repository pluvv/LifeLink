package com.lifelink.model;

public class User {
    private long id;
    private String fullName;
    private String email;
    private String role;
    private String status;

    public User(long id, String fullName, String email, String role, String status) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.status = status;
    }
    public long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
}
