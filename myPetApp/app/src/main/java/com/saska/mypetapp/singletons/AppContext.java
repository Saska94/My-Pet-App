package com.saska.mypetapp.singletons;

import com.saska.mypetapp.db.Pet;
import com.saska.mypetapp.db.User;

import java.util.List;

public class AppContext {

    private static AppContext context = null;

    private User activeUser;
    private String username;
    private List<Pet> allPets;
    private List<Pet> filteredPets;
    private Pet selectedPet;


    private AppContext(){}

    public static AppContext getContext(){
        if (context == null){
            context = new AppContext();
        }
        return context;
    }

    public void clearContext(){
        context = null;
    }

    public User getActiveUser() {
        return activeUser;
    }

    public void setActiveUser(User activeUser) {
        this.activeUser = activeUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Pet> getAllPets() {
        return allPets;
    }

    public void setAllPets(List<Pet> pets){
        this.allPets = pets;
    }

    public void setSelectedPet(Pet pet){
        this.selectedPet = pet;
    }

    public Pet getSelectedPet(){
        return selectedPet;
    }

    public List<Pet> getFilteredPets() {
        return filteredPets;
    }

    public void setFilteredPets(List<Pet> filteredPets) {
        this.filteredPets = filteredPets;
    }
}
