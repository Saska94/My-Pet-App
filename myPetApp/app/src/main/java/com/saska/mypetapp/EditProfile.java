package com.saska.mypetapp;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.saska.mypetapp.db.ClientFactory;
import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.db.User;
import com.saska.mypetapp.helper.Camera;
import com.saska.mypetapp.helper.Helper;
import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.singletons.AppContext;

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

        activeUser = AppContext.getContext().getActiveUser();
        camera = new Camera(this);

        profileImage = (ImageView) findViewById(R.id.profileImage);

        editName = (EditText)findViewById(R.id.editTextName);
        editName.setText(activeUser.getName());

        editSurname = (EditText)findViewById(R.id.editTextSurname);
        editSurname.setText(activeUser.getSurname());

        editPhone = (EditText)findViewById(R.id.editTextPhone);
        editPhone.setText(activeUser.getPhone());

        loadImage();


    }

    private void loadImage(){
        if (activeUser.getPicture() != null){
            final String localPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).getAbsolutePath().concat("/").concat(activeUser.getPicture());
            profileImage.setImageBitmap(BitmapFactory.decodeFile(localPath));
        }
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
        return "public/profile/" + new File(localPath).getName();
    }

    public void uploadWithTransferUtility(final ProgressBar progressBar,final Window window, String localPath) {
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
                    progressBar.setVisibility(View.INVISIBLE);
                    Helper.unblockTouch(window);

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
                        progressBar.setVisibility(View.INVISIBLE);
                        Helper.unblockTouch(window);
                        Toast.makeText(EditProfile.this, "Failed to upload photo", Toast.LENGTH_LONG).show();
                    }
                });
            }

        });
    }

    public void uploadAndSave(View view){

        if (camera.getPicturePath()!= null){
            /*final String localPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).getAbsolutePath().concat("/").concat(camera.getPicturePath());
            File file = new File(localPath);
            if (file.exists()){
                save();
            }
            else{*/
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarEditProfile);
                progressBar.setVisibility(View.VISIBLE);
                Helper.blockTouch(getWindow());
                uploadWithTransferUtility(progressBar, getWindow(), camera.getPicturePath());
            //}
        }
        else{
            save();
        }

    }


    public void save(){
        String name = ((EditText) findViewById(R.id.editTextName)).getText().toString();
        String surname = ((EditText) findViewById(R.id.editTextSurname)).getText().toString();
        String phone = ((EditText) findViewById(R.id.editTextPhone)).getText().toString();


        AppContext.getContext().getActiveUser().setName(name);
        AppContext.getContext().getActiveUser().setSurname(surname);
        AppContext.getContext().getActiveUser().setPhone(phone);
        String image = (camera.getPicturePath() == null)? null : getS3Key(camera.getPicturePath());
        AppContext.getContext().getActiveUser().setPicture(image);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarEditProfile);
        progressBar.setVisibility(View.VISIBLE);
        Helper.blockTouch(getWindow());
        DBHelper.updateUser(progressBar, getWindow(), toaster, AppContext.getContext().getActiveUser());


    }

    public void clearImage(View view){
        final String localPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).getAbsolutePath().concat("/").concat("public/avatar.png");
        activeUser.setPhone(null);
        profileImage.setImageBitmap(BitmapFactory.decodeFile(localPath));
    }


    public void goToUser(View view){
        Intent i = new Intent(EditProfile.this, UserActivity.class);
        startActivity(i);
    }


}
