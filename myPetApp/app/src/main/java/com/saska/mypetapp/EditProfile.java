package com.saska.mypetapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.saska.mypetapp.db.ClientFactory;
import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.db.User;
import com.saska.mypetapp.helper.Camera;
import com.saska.mypetapp.helper.Toaster;

import java.io.File;

public class EditProfile extends AppCompatActivity {

    private static String CLASS_NAME;
    private User activeUser;
    private EditText editName, editSurname, editPhone;
    private Camera camera;
    ImageView profileImage;
    private Toaster toaster;

    public EditProfile(){
        CLASS_NAME = getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        toaster = new Toaster(this);

        activeUser = (User) getIntent().getSerializableExtra("USER");
        camera = new Camera(this);

        profileImage = (ImageView) findViewById(R.id.profileImage);

        editName = (EditText)findViewById(R.id.editTextName);
        editName.setText(activeUser.getName());

        editSurname = (EditText)findViewById(R.id.editTextSurname);
        editSurname.setText(activeUser.getSurname());

        editPhone = (EditText)findViewById(R.id.editTextPhone);
        editPhone.setText(activeUser.getPhone());

        Button updateInfoButton = (Button) findViewById(R.id.updateInfoButton);
        updateInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadAndSave();
            }
        });


    }

    public void takePhoto (View view){

        if (camera.hasCamera() && camera.hasCameraApplication()){
            camera.takePhoto();
        }

    }

    public void choosePhoto (View view){
        camera.openGallery();
    }

    public void displayPhoto(Intent intent){
        camera.displayPhoto(profileImage, intent);
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
        return "public/" + new File(localPath).getName();
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
                    Log.d(CLASS_NAME, "Upload is completed. ");

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
                        Toast.makeText(EditProfile.this, "Failed to upload photo", Toast.LENGTH_LONG).show();
                    }
                });
            }

        });
    }

    private void uploadAndSave(){

        if (camera.getPicturePath() != null) {
            // For higher Android levels, we need to check permission at runtime
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                Log.d(CLASS_NAME, "READ_EXTERNAL_STORAGE permission not granted! Requesting...");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }

            // Upload a photo first. We will only call save on its successful callback.
            uploadWithTransferUtility(camera.getPicturePath());
        } else {
            save();
        }
    }


    public void save(){
        String name = ((EditText) findViewById(R.id.editTextName)).getText().toString();
        String surname = ((EditText) findViewById(R.id.editTextSurname)).getText().toString();
        String phone = ((EditText) findViewById(R.id.editTextPhone)).getText().toString();

        activeUser.setName(name);
        activeUser.setSurname(surname);
        activeUser.setPhone(phone);
        if(camera.getPicturePath() != null){
            activeUser.setProfilePicture(getS3Key(camera.getPicturePath()));
        }
        DBHelper.updateUser(toaster, activeUser);

    }




}
