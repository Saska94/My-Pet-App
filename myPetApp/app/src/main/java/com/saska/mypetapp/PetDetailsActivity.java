package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.saska.mypetapp.db.Pet;
import com.saska.mypetapp.singletons.AppContext;

public class PetDetailsActivity extends AppCompatActivity {

    ProgressBar progressBarPetDetails;
    Pet selectedPet;

    TextView name, description, location, type;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_details);


        selectedPet = AppContext.getContext().getSelectedPet();
        name = (TextView) findViewById(R.id.detailsName);
        name.setText(selectedPet.getName());
        description = (TextView) findViewById(R.id.detailsDescription);
        description.setText(selectedPet.getDescription());
        location = (TextView) findViewById(R.id.detailsLocation);
        location.setText(selectedPet.getLocation());
        type = (TextView) findViewById(R.id.detailsType);
        type.setText(selectedPet.getType());

    }

    public void goToPets(View view){
        Intent i = new Intent(PetDetailsActivity.this, Pets.class);
        startActivity(i);
    }
}
