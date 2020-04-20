package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.saska.mypetapp.db.User;
import com.saska.mypetapp.helper.Camera;

public class EditProfile extends AppCompatActivity {

    private static String CLASS_NAME;
    private User activeUser;
    private EditText editName, editSurname, editPhone;
    private Camera camera;
    ImageView profileImage;

    public EditProfile(){
        CLASS_NAME = getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        activeUser = (User) getIntent().getSerializableExtra("USER");
        camera = new Camera(this);

        profileImage = (ImageView) findViewById(R.id.profileImage);

        editName = (EditText)findViewById(R.id.editTextName);
        editName.setText(activeUser.getName());

        editSurname = (EditText)findViewById(R.id.editTextSurname);
        editSurname.setText(activeUser.getSurname());

        editPhone = (EditText)findViewById(R.id.editTextPhone);
        editPhone.setText(activeUser.getPhone());


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

}
