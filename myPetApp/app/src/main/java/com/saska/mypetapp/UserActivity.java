package com.saska.mypetapp;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.saska.mypetapp.db.ClientFactory;
import com.saska.mypetapp.db.User;
import com.saska.mypetapp.singletons.AppContext;

import java.io.File;

public class UserActivity extends AppCompatActivity {

    private static String CLASS_NAME;
    private static String WELCOME = "Welcome %s !";

    private static User activeUser;
    private ProgressBar progressBar;

    public UserActivity(){
        CLASS_NAME = getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        activeUser = AppContext.getContext().getActiveUser();

        loadProfileImage();

        TextView welcomeText = (TextView) findViewById(R.id.welcomeText);
        welcomeText.setText(String.format(WELCOME, activeUser.getName()));

        progressBar = (ProgressBar) findViewById(R.id.progressBarUser);

    }

    private void loadProfileImage(){
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.profileImageLayout);
        layout.removeAllViews();
        if (activeUser.getOldProfilePicture() != null){
            final String localPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).getAbsolutePath().concat("/").concat(activeUser.getOldProfilePicture());
            activeUser.setLocalPicturePath(localPath);
            File file = new File(localPath);
            // If we already downloaded image, load it from local, do not contact s3
            if (file.exists()){
                ImageView profileImage = new ImageView(getApplicationContext());
                profileImage.setImageBitmap(BitmapFactory.decodeFile(localPath));
                profileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        goToEditProfile();
                    }
                });
                layout.addView(profileImage);
            }
            else{
                // Profile image not downloaded, get it from s3 bucket
                ProgressBar loadingImage = new ProgressBar(this);
                loadingImage.setForegroundGravity(Gravity.CENTER);
                loadingImage.setVisibility(View.VISIBLE);
                layout.addView(loadingImage);
                downloadWithTransferUtility(layout, loadingImage, activeUser.getOldProfilePicture());
            }

        }
        else{
            // If user does not have profile image, load avatar
            loadAvatarImage(layout);
        }
    }

    private void loadAvatarImage(RelativeLayout layout){
        ImageView profileImage = new ImageView(this);
        profileImage.setImageDrawable(getDrawable(R.drawable.avatar));
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToEditProfile();
            }
        });
        layout.addView(profileImage);
    }

    @Override
    protected void onResume(){
        super.onResume();
        activeUser = AppContext.getContext().getActiveUser();
        TextView welcomeText = (TextView) findViewById(R.id.welcomeText);
        welcomeText.setText(String.format(WELCOME, activeUser.getName()));
        loadProfileImage();
    }

    public void logOut(View view){/*
            progressBar.setVisibility(View.VISIBLE);
            Helper.blockTouch(getWindow());
            AwsClient.signOut(this, progressBar, getWindow());*/
        AppContext.getContext().clearContext();
        AWSMobileClient.getInstance().signOut();
        startActivity(new Intent(this, MainActivity.class));

    }

    public void goToEditProfile(){
        Intent i = new Intent(UserActivity.this, EditProfile.class);
        i.putExtra("USER", activeUser);
        startActivity(i);
    }

    private void downloadWithTransferUtility(final RelativeLayout layout, final ProgressBar progressBar, final String photo) {

        final String localPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + photo;

        TransferObserver downloadObserver =
                ClientFactory.transferUtility().download(
                        photo,
                        new File(localPath));

        // Attach a listener to the observer to get state update and progress notifications
        downloadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    ImageView profileImage = new ImageView(getApplicationContext());
                    profileImage.setImageBitmap(BitmapFactory.decodeFile(localPath));
                    profileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            goToEditProfile();
                        }
                    });
                    layout.removeAllViews();
                    layout.addView(profileImage);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;

                Log.d(CLASS_NAME, "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
                Log.e(CLASS_NAME, "Unable to download the file.", ex);
            }
        });
    }


}
