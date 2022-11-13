package com.specknet.pdiotapp.bean;

public class User {
    private String name;
    private String UserID;
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", UserID='" + UserID + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
