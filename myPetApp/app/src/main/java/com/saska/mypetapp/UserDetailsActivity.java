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

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.saska.mypetapp.db.ClientFactory;
import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.db.User;
import com.saska.mypetapp.helper.Helper;
import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.singletons.AppContext;

import java.io.File;

public class UserDetailsActivity extends AppCompatActivity {

    private static String CLASS_NAME;
    private Toaster toaster;

    private ProgressBar progressBarUserDetails;
    private TextView name, surname, username, typeDetails;
    private User selectedUser;
    private ImageView userImage;

    public UserDetailsActivity(){
        CLASS_NAME = getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        toaster = new Toaster(this);

        selectedUser = AppContext.getContext().getSelectedUser();
        progressBarUserDetails = (ProgressBar) findViewById(R.id.progressBarUserDetails);
        progressBarUserDetails.setVisibility(View.INVISIBLE);
        name = (TextView) findViewById(R.id.detailsName);
        name.setText(selectedUser.getName());
        surname = (TextView) findViewById(R.id.detailsSurname);
        surname.setText(selectedUser.getSurname());
        username = (TextView) findViewById(R.id.detailsUsername);
        username.setText(selectedUser.getUsername());
        typeDetails = (TextView) findViewById(R.id.detailsType);
        String type = (selectedUser.isUser()) ? "User" : "Worker";
        typeDetails.setText(type);
        userImage = (ImageView) findViewById(R.id.userImage);


        loadUserImage();

    }


    public void goToUsers(View view){
        Intent i = new Intent(UserDetailsActivity.this, UsersActivity.class);
        startActivity(i);
    }

    public void removeUser(View view){
        progressBarUserDetails.setVisibility(View.VISIBLE);
        Helper.blockTouch(getWindow());
        DBHelper.deleteUser(progressBarUserDetails, getWindow(), toaster, this, selectedUser.getIdUser());

    }

    public void changeType(View view){
        progressBarUserDetails.setVisibility(View.VISIBLE);
        Helper.blockTouch(getWindow());
        int type = 1;
        if (selectedUser.isUser()){
            type = 2;
        }
        selectedUser.setType(type);
        DBHelper.updateUser(progressBarUserDetails, getWindow(), toaster, selectedUser);
    }

    private void loadUserImage(){
        if (selectedUser.getPicture()!= null){
            final String localPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).getAbsolutePath().concat("/").concat(selectedUser.getPicture());
            File file = new File(localPath);
            if (file.exists()){
                userImage.setImageBitmap(BitmapFactory.decodeFile(localPath));
            }
            else{
                downloadWithTransferUtility(selectedUser.getPicture());
            }
        }
    }

    private void downloadWithTransferUtility(final String photo) {

        progressBarUserDetails.setVisibility(View.VISIBLE);
        Helper.blockTouch(getWindow());

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
                    userImage.setImageBitmap(BitmapFactory.decodeFile(localPath));
                    progressBarUserDetails.setVisibility(View.INVISIBLE);
                    Helper.unblockTouch(getWindow());
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
                progressBarUserDetails.setVisibility(View.INVISIBLE);
                Helper.unblockTouch(getWindow());
            }
        });
    }
}


