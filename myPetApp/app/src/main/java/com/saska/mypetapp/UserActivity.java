package com.saska.mypetapp;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
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

import java.io.File;

public class UserActivity extends AppCompatActivity {

    private static String CLASS_NAME;

    private User activeUser;
    private ProgressBar progressBar;

    public UserActivity(){
        CLASS_NAME = getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        activeUser = (User) getIntent().getSerializableExtra("USER");
        /**if (activeUser.getProfilePicture() != null){
            downloadWithTransferUtility(activeUser.getProfilePicture());
        }*/

        TextView welcomeText = (TextView) findViewById(R.id.welcomeText);
        String newText = welcomeText.getText().toString().concat(" ").concat(activeUser.getName()).concat("!");
        welcomeText.setText(newText);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    public void logOut(View view){/*
            progressBar.setVisibility(View.VISIBLE);
            Helper.blockTouch(getWindow());
            AwsClient.signOut(this, progressBar, getWindow());*/
        AWSMobileClient.getInstance().signOut();
        startActivity(new Intent(this, MainActivity.class));

    }

    public void goToEditProfile(View view){
        Intent i = new Intent(UserActivity.this, EditProfile.class);
        i.putExtra("USER", activeUser);
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
                    ((ImageView)findViewById(R.id.profileImage)).setImageBitmap(BitmapFactory.decodeFile(localPath));
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
