package com.example.onmyway.Models;

public class User {

    private String fullName;
    private String email;
    private String password;
    //user id==cin
    private String cin;
    private boolean transporter;
    public User()
    {
        //this Constructor is for DataSnapShot for firebase
        
    }

    public User(String fullname, String email, String password, String id, boolean transporter) {
        this.fullName = fullname;
        this.email = email;
        this.password = password;
        this.cin = id;
        this.transporter = transporter;
    }

    public String getfullName() {
        return fullName;
    }

    public void setfullName(String fullName) {
        this.fullName = fullName;
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return cin;
    }

    public void setId(String id) {
        this.cin = id;
    }

    public boolean isTransporter() {
        return transporter;
    }

    public void setTransporter(boolean transporter) {
        this.transporter = transporter;
    }
}
