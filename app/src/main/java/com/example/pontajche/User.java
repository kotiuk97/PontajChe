package com.example.pontajche;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

    private long userUId;
    private String email;
    private String password;
    private Date registrationDate;

    public User() {
    }

    public long getUserUId() {
        return userUId;
    }

    public void setUserUId(long userUId) {
        this.userUId = userUId;
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

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }
}
