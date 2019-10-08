package com.example.pontajche;

public class LoginProperties {

    private String email;
    private String password;
    private boolean isRememberMe;
    private boolean isConnectAtStartup;

    public LoginProperties() {
    }

    public LoginProperties(String email, String password, boolean isRememberMe, boolean isConnectAtStartup) {
        this.email = email;
        this.password = password;
        this.isRememberMe = isRememberMe;
        this.isConnectAtStartup = isConnectAtStartup;
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

    public boolean isRememberMe() {
        return isRememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        isRememberMe = rememberMe;
    }

    public boolean isConnectAtStartup() {
        return isConnectAtStartup;
    }

    public void setConnectAtStartup(boolean connectAtStartup) {
        isConnectAtStartup = connectAtStartup;
    }
}
