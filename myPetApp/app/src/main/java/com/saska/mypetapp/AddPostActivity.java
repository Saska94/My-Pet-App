package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.saska.mypetapp.db.Post;
import com.saska.mypetapp.helper.Camera;
import com.saska.mypetapp.helper.Helper;
import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.singletons.AppContext;

import java.io.File;

public class AddPostActivity extends AppCompatActivity {

    private static String CLASS_NAME;
    private Toaster toaster;

    private ProgressBar progressBarNewPost;
    Camera camera;

    private EditText postHeading, postText;

    public AddPostActivity(){
        CLASS_NAME = getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        toaster = new Toaster(this);
        camera = new Camera(this);

        progressBarNewPost = (ProgressBar) findViewById(R.id.progressBarNewPost);
        progressBarNewPost.setVisibility(View.INVISIBLE);

    }

    public void goToPosts(View view){
        Intent i = new Intent(AddPostActivity.this, PostsActivity.class);
        startActivity(i);
    }

    public void addPost(View view){

        postHeading = (EditText) findViewById(R.id.newPostHeading);
        postText = (EditText) findViewById(R.id.newPostText);

        if (postHeading.getText().toString().isEmpty()){
            toaster.make("Please provide the post heading.");
        }
        else {
            progressBarNewPost.setVisibility(View.VISIBLE);
            Helper.blockTouch(getWindow());
            if(camera.getPicturePath() != null){
                uploadWithTransferUtility(camera.getPicturePath());
            }
            else {
                save();
            }

        }

    }

    public void choosePhoto (View view){
        camera.openGallery();
    }

    public void displayPhoto(Intent intent){

        ImageView postImage = (ImageView) findViewById(R.id.addPostImage);
        camera.displayPhoto(postImage, intent);
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
        return "public/posts/" + new File(localPath).getName();
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
                        Toast.makeText(AddPostActivity.this, "Failed to upload photo", Toast.LENGTH_LONG).show();
                    }
                });
            }

        });
    }

    private void save(){

        progressBarNewPost.setVisibility(View.VISIBLE);
        Helper.blockTouch(getWindow());
        String picture = null;
        if (camera.getPicturePath() != null){
            picture = getS3Key(camera.getPicturePath());
        }
        int approved = 0;
        if (!AppContext.getContext().getActiveUser().isUser()){
            approved = 1;
        }
        Post post = new Post();
        post.setHeading(postHeading.getText().toString());
        post.setText(postText.getText().toString());
        post.setUser(AppContext.getContext().getActiveUser());
        post.setPicture(picture);
        post.setApproved(approved);
        DBHelper.addPost(progressBarNewPost, getWindow(), toaster, post);

    }

}
