package com.saska.mypetapp;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.saska.mypetapp.db.Pet;
import com.saska.mypetapp.singletons.AppContext;

public class PetDetailsActivity extends AppCompatActivity {

    ProgressBar progressBarPetDetails;
    Pet selectedPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_details);


        selectedPet = AppContext.getContext().getSelectedPet();
        TextView name = (TextView) findViewById(R.id.detailsName);
        name.setText(selectedPet.getName());

    }
}
