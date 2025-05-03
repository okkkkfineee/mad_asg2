package com.utarproject.eventu;

public class User {
    private String stuId, name, email, campus, phoneNo, role;

    // Empty constructor for Firestore
    public User() {}

    // Constructor with student ID, name, email, campus, and setup completion flag
    public User(String stuId, String name, String email, String campus, String phoneNo, String role) {
        this.stuId = stuId;
        this.name = name;
        this.email = email;
        this.campus = campus;
        this.phoneNo = phoneNo;
        this.role = role;
    }

    // Getter and Setter for stuId
    public String getStuId() {
        return stuId;
    }

    public void setStuId(String stuId) {
        this.stuId = stuId;
    }

    // Getter and Setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and Setter for campus
    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    // Getter and Setter for phone no
    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    // Getter and Setter for phone no
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
