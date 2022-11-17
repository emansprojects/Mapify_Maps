package com.vc.mapify;

//CLASS TO SAVE AND SET USER DETAILS
public class UserModel {

    //VARIABLES DECLARED
    String email, username;
    boolean isNotificationEnable;

    //DEFAULT CONSTRUCTOR
    public UserModel() {
    }

    //CONSTRUCTOR TO SET THE DATA FOR A USER
    public UserModel(String email, String username, boolean isNotificationEnable) {
        this.email = email;
        this.username = username;
        this.isNotificationEnable = isNotificationEnable;
    }

    //GETTER AND SETTER METHODS
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public boolean isNotificationEnable() {
        return isNotificationEnable;
    }

    public void setNotificationEnable(boolean notificationEnable) {
        isNotificationEnable = notificationEnable;
    }
}