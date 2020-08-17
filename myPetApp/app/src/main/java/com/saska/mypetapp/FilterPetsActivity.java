package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.saska.mypetapp.db.Pet;
import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.singletons.AppContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilterPetsActivity extends AppCompatActivity {


    private Spinner spinnerPetTypes, spinnerPetLocations;
    private Toaster toaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_pets);

        toaster = new Toaster(this);

        String[] petTypes = {"", "Dog", "Cat", "Turtle", "Rabbit", "Bird", "Other"};
        spinnerPetTypes = (Spinner) findViewById(R.id.petType);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, petTypes);
        adapter.setDropDownViewResource(R.layout.custom_spinner);
        spinnerPetTypes.setAdapter(adapter);

        spinnerPetLocations = (Spinner) findViewById(R.id.petLocation);
        List<Pet> allPets = AppContext.getContext().getAllPets();
        Set<String> locations = new HashSet<>();
        locations.add("");
        for (Pet pet : allPets){
            locations.add(pet.getLocation());
        }
        ArrayAdapter<String> adapterLoc = new ArrayAdapter<String>(this, R.layout.custom_spinner, locations.toArray(new String[locations.size()]));
        adapterLoc.setDropDownViewResource(R.layout.custom_spinner);
        spinnerPetLocations.setAdapter(adapterLoc);

    }

    public void goToPets(View view){
        Intent i = new Intent(FilterPetsActivity.this, Pets.class);
        startActivity(i);
    }

    public void search(View view){

        String petType = spinnerPetTypes.getSelectedItem().toString();
        String petLocation = spinnerPetLocations.getSelectedItem().toString();
        Log.i("ASD", "CRITERIA IS : " + petType + " and " + petLocation);
        Log.i("ASD", "ALL PETS SIZE IS " + AppContext.getContext().getAllPets().size());
        List<Pet> filteredPets = new ArrayList<>();
        for (Pet pet : AppContext.getContext().getAllPets()){
            Log.i("ASD", "Looking for pet :" + pet.getName() + " , " + pet.getType() + " , " + pet.getLocation());
            if (petType.isEmpty() && petLocation.isEmpty()){
                toaster.make("You haven't selected any filters.");
                return;
            }
            else if (!petType.isEmpty() && !petLocation.isEmpty()){
                if (petType.equals(pet.getType()) && petLocation.equals(pet.getLocation())){
                    Log.i("ASD", "PET ADDED TO FILTER - " + pet.getName());
                    filteredPets.add(pet);
                }
            }
            else if (petType.isEmpty()){
                if (petLocation.equals(pet.getLocation())){
                    Log.i("ASD", "PET ADDED TO FILTER - " + pet.getName());
                    filteredPets.add(pet);
                }
            }
            else {
                if (petType.equals(pet.getType())){
                    Log.i("ASD", "PET ADDED TO FILTER - " + pet.getName());
                    filteredPets.add(pet);
                }
            }


        }
        AppContext.getContext().setFilteredPets(filteredPets);
        goToPets(view);

    }
}
