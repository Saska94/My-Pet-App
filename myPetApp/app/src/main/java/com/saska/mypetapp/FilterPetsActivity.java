package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    private EditText filterChip;

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

        filterChip = (EditText) findViewById(R.id.filterChip);

    }

    public void goToPets(View view){
        Intent i = new Intent(FilterPetsActivity.this, Pets.class);
        startActivity(i);
    }

    public void search(View view){

        String petType = spinnerPetTypes.getSelectedItem().toString();
        String petLocation = spinnerPetLocations.getSelectedItem().toString();
        String chip = filterChip.getText().toString();

        if (petType.isEmpty() && petLocation.isEmpty() && chip.isEmpty()){
            toaster.make("You haven't selected any filters.");
            return;
        }

        List<Pet> filteredPets = AppContext.getContext().getAllPets();
        if (!petType.isEmpty()){
            filteredPets = findPetsByType(filteredPets, petType);
        }
        if (!petLocation.isEmpty()){
            filteredPets = findPetsByLocation(filteredPets, petLocation);
        }
        if (!chip.isEmpty()){
            filteredPets = findPetsByChip(filteredPets, chip);
        }

        AppContext.getContext().setFilteredPets(filteredPets);
        goToPets(view);

    }

    private List<Pet> findPetsByType(List<Pet> list, String type){
        List<Pet> result = new ArrayList<>();
        for (Pet pet : list){
            if (type.equals(pet.getType())){
                result.add(pet);
            }
        }
        return result;
    }

    private List<Pet> findPetsByLocation(List<Pet> list, String location){
        List<Pet> result = new ArrayList<>();
        for (Pet pet : list){
            if (location.equals(pet.getLocation())){
                result.add(pet);
            }
        }
        return result;
    }

    private List<Pet> findPetsByChip(List<Pet> list, String chip){
        List<Pet> result = new ArrayList<>();
        for (Pet pet : list){
            if (chip.equals(pet.getChip())){
                result.add(pet);
            }
        }
        return result;
    }


}
