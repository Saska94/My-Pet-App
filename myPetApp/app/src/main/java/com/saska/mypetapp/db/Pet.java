package com.saska.mypetapp.db;

import android.graphics.Bitmap;

import com.amazonaws.amplify.generated.graphql.ListPetsQuery;

import java.io.Serializable;

public class Pet implements Serializable {

    private int id, adoption;
    private String name, description, location, type, picture;
    private Bitmap imageBitmap;

    public Pet(){}

    public Pet(String name, String description, String location, String type, String picture) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.type = type;
        this.picture = picture;
    }

    public Pet(ListPetsQuery.Item pet){
        this.name = pet.name();
        this.description = pet.description();
        this.location = pet.location();
        this.type = pet.type();
        this.picture = pet.picture();
        this.adoption = pet.addoption();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public int getAdoption() {
        return adoption;
    }

    public void setAdoption(int addoption) {
        this.adoption = addoption;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }
}
