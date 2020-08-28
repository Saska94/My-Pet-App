package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.saska.mypetapp.db.ClientFactory;
import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.db.Pet;
import com.saska.mypetapp.helper.Camera;
import com.saska.mypetapp.helper.Helper;
import com.saska.mypetapp.helper.Toaster;

import java.io.File;

public class AddPetActivity extends AppCompatActivity {

    private static String CLASS_NAME;
    private Toaster toaster;
    private ProgressBar progressBarAddPet;
    private Spinner spinnerPetTypes, spinnerLost;
    private Camera camera;

    public AddPetActivity(){
        CLASS_NAME = getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);

        toaster = new Toaster(this);
        camera = new Camera(this);
        progressBarAddPet = (ProgressBar) findViewById(R.id.progressBarAddPet);
        progressBarAddPet.setVisibility(View.INVISIBLE);


        String[] petTypes = {"Dog", "Cat", "Turtle", "Rabbit", "Bird", "Other"};
        spinnerPetTypes = (Spinner) findViewById(R.id.petType);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, petTypes);
        adapter.setDropDownViewResource(R.layout.custom_spinner);
        spinnerPetTypes.setAdapter(adapter);

        String[] lostAdoption = {"Lost", "For adoption"};
        spinnerLost = (Spinner) findViewById(R.id.lost);
        ArrayAdapter<String> adapterLost = new ArrayAdapter<String>(this, R.layout.custom_spinner, lostAdoption);
        adapterLost.setDropDownViewResource(R.layout.custom_spinner);
        spinnerLost.setAdapter(adapterLost);

    }

    public void cancel(View view){
        Intent cancel = new Intent(this, Pets.class);
        startActivity(cancel);
    }

    public void addPet(View view){

        progressBarAddPet.setVisibility(View.VISIBLE);
        Helper.blockTouch(getWindow());
        if (camera.getPicturePath() != null){
            uploadWithTransferUtility(camera.getPicturePath());
        }
        else {
            save();
        }

    }

    public void choosePhoto (View view){
        camera.openGallery();
    }

    public void displayPhoto(Intent intent){

        ImageView petImage = (ImageView) findViewById(R.id.addPetImage);
        camera.displayPhoto(petImage, intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case Camera.PHOTO_TAKEN: {
                if (resultCode == RESULT_OK) {
                    displayPhoto(intent);
                }
                break;
            }
            case Camera.PICK_IMAGE: {
                if (resultCode == RESULT_OK) {
                    displayPhoto(intent);
                }
            }
            default:
                Log.w(CLASS_NAME, "Unknown request code " + Integer.toString(requestCode));
        }

    }

    private String getS3Key(String localPath) {
        //We have read and write ability under the public folder
        return "public/pets/" + new File(localPath).getName();
    }

    public void uploadWithTransferUtility(String localPath) {
        String key = getS3Key(localPath);

        Log.d(CLASS_NAME, "Uploading file from " + localPath + " to " + key);

        TransferObserver uploadObserver =
                ClientFactory.transferUtility().upload(
                        key,
                        new File(localPath));

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Log.d(CLASS_NAME, "Upload is completed. Saving to db next... ");

                    // Upload is successful. Save the rest and send the mutation to server.
                    save();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d(CLASS_NAME, "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
                Log.e(CLASS_NAME, "Failed to upload photo. ", ex);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddPetActivity.this, "Failed to upload photo", Toast.LENGTH_LONG).show();
                    }
                });
            }

        });
    }

    private void save(){

        String name = ((EditText) findViewById(R.id.petName)).getText().toString();
        String description =((EditText) findViewById(R.id.petDescription)).getText().toString();
        String location = ((EditText) findViewById(R.id.petLocation)).getText().toString();
        String petChip = ((EditText) findViewById(R.id.petChip)).getText().toString();

        if (name.isEmpty() || location.isEmpty()){
            progressBarAddPet.setVisibility(View.INVISIBLE);
            Helper.unblockTouch(getWindow());
            toaster.make("Name and location are required fields.");
        }
        else{
            String type = spinnerPetTypes.getSelectedItem().toString();
            int adoption = (spinnerLost.getSelectedItem().toString().equals("Lost")) ? 0 : 1;
            String picture = (camera.getPicturePath()!= null) ? getS3Key(camera.getPicturePath()) : null;
            Pet pet = new Pet();
            pet.setReserved(0);
            pet.setAdoption(adoption);
            pet.setDescription(description);
            pet.setType(type);
            pet.setName(name);
            pet.setLocation(location);
            pet.setPicture(picture);
            pet.setChip(petChip);
            DBHelper.addPet(toaster, progressBarAddPet, getWindow(), pet);
        }

    }
}
