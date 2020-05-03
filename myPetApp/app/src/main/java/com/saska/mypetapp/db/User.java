package com.saska.mypetapp.db;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.saska.mypetapp.helper.Helper;

import java.io.Serializable;

public class User implements Serializable {

    private String idUser, name, surname, username, phone, oldProfilePicture, newProfilePicture, localPicturePath;
    private int type;

    public User(){}

    public User(String name, String surname, String username, String phone, int type, String oldProfilePicture) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.phone = phone;
        this.type = type;
        this.oldProfilePicture = oldProfilePicture;
    }

    public User(ListUsersQuery.Item user){
        this.idUser = user.id();
        this.name = user.name();
        this.surname = user.surname();
        this.username = user.username();
        this.phone = user.phone();
        this.type = user.type();
        this.oldProfilePicture = user.profilePicture();
        this.newProfilePicture = this.oldProfilePicture;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getOldProfilePicture() {
        return oldProfilePicture;
    }

    public void setOldProfilePicture(String oldProfilePicture) {
        this.oldProfilePicture = oldProfilePicture;
    }

    public String getNewProfilePicture() {
        return newProfilePicture;
    }

    public void setNewProfilePicture(String newProfilePicture) {
        this.newProfilePicture = newProfilePicture;
    }

    public String getLocalPicturePath() {
        return localPicturePath;
    }

    public void setLocalPicturePath(String localPicturePath) {
        this.localPicturePath = localPicturePath;
    }

    public boolean samePictures(){
        boolean result;
        if (oldProfilePicture == null && newProfilePicture == null) {
            result = true;
        }
        else if (oldProfilePicture == null || newProfilePicture == null){
            result = false;
        }
        else{
            result = Helper.getPictureName(oldProfilePicture).equals(Helper.getPictureName(newProfilePicture));
        }
        return result;
    }

}
