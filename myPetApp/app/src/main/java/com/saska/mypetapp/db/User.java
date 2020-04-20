package com.saska.mypetapp.db;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;

import java.io.Serializable;

public class User implements Serializable {

    private String idUser, name, surname, username, phone, profilePicture;
    private int type;

    public User(){}

    public User(String name, String surname, String username, String phone, int type, String profilePicture) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.phone = phone;
        this.type = type;
        this.profilePicture = profilePicture;
    }

    public User(ListUsersQuery.Item user){
        this.idUser = user.id();
        this.name = user.name();
        this.surname = user.surname();
        this.username = user.username();
        this.phone = user.phone();
        this.type = user.type();
        this.profilePicture = user.profilePicture();
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

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
