package com.saska.mypetapp;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

    private ImageView profileImage;
    private Button usersBtn;

    public UserActivity(){
        CLASS_NAME = getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        activeUser = AppContext.getContext().getActiveUser();
        Log.i("RW", "USER - " + "FPS - " + SystemClock.elapsedRealtime());

        loadProfileImage();

        TextView welcomeText = (TextView) findViewById(R.id.welcomeText);
        welcomeText.setText(String.format(WELCOME, activeUser.getName()));

        progressBar = (ProgressBar) findViewById(R.id.progressBarUser);

        usersBtn = (Button) findViewById(R.id.usersBtn);
        if (!activeUser.isAdmin()){
            usersBtn.setVisibility(View.GONE);
        }
        else {
            usersBtn.setVisibility(View.VISIBLE);
        }

        Button ourPets = (Button) findViewById(R.id.ourPetsBtn);
        ourPets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserActivity.this, Pets.class);
                startActivity(i);
            }
        });

    }

    private void loadProfileImage(){
        profileImage = (ImageView) findViewById(R.id.profileImage);
        if (activeUser.getPicture() != null){
            final String localPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).getAbsolutePath().concat("/").concat(activeUser.getPicture());
            File file = new File(localPath);
            // If we already downloaded image, load it from local, do not contact s3
            if (file.exists()){
                profileImage.setImageBitmap(BitmapFactory.decodeFile(localPath));
                profileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        goToEditProfile();
                    }
                });
            }
            else{
                // Profile image not downloaded, get it from s3 bucket
                ProgressBar loadingImage = new ProgressBar(this);
                loadingImage.setForegroundGravity(Gravity.CENTER);
                loadingImage.setVisibility(View.VISIBLE);
                downloadWithTransferUtility(activeUser.getPicture());
            }

        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        activeUser = AppContext.getContext().getActiveUser();
        TextView welcomeText = (TextView) findViewById(R.id.welcomeText);
        welcomeText.setText(String.format(WELCOME, activeUser.getName()));
        loadProfileImage();
    }

    public void logOut(View view){
        AppContext.getContext().clearContext();
        AWSMobileClient.getInstance().signOut();
        startActivity(new Intent(this, MainActivity.class));

    }

    public void goToEditProfile(){
        Intent i = new Intent(UserActivity.this, EditProfile.class);
        startActivity(i);
    }

    public void goToAboutUs(View view){
        Intent i = new Intent(UserActivity.this, AboutUsActivity.class);
        startActivity(i);
    }

    public void goToFunFacts(View view){
        Intent i = new Intent(UserActivity.this, FunFactsActivity.class);
        startActivity(i);
    }

    public void goToPosts(View view){
        Intent i = new Intent(UserActivity.this, PostsActivity.class);
        startActivity(i);
    }

    public void goToUsers(View view){
        Intent i = new Intent(UserActivity.this, UsersActivity.class);
        startActivity(i);
    }

    private void downloadWithTransferUtility(final String photo) {

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
                    profileImage.setImageBitmap(BitmapFactory.decodeFile(localPath));
                    profileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            goToEditProfile();
                        }
                    });
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
