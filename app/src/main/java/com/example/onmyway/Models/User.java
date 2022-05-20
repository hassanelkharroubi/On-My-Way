package com.example.onmyway.Models;

public class User {

    private String fullName;
    private String email;
    private String password;
    //user id==cin
    private String cin;

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    private String admin;
    public User()
    {
        //this Constructor is for DataSnapShot for firebase
        
    }

    public User(String fullname, String email, String password, String id) {
        this.fullName = fullname;
        this.email = email;
        this.password = password;
        this.cin = id;
    }

    public User(String fullname, String email, String password, String id,String admin) {
        this.fullName = fullname;
        this.email = email;
        this.password = password;
        this.cin = id;
        this.admin=admin;
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

}
