package com.saska.mypetapp;

import android.annotation.SuppressLint;
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
import com.saska.mypetapp.db.Post;
import com.saska.mypetapp.helper.Helper;
import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.singletons.AppContext;

import java.io.File;

public class PostDetailsActivity extends AppCompatActivity {

    private static String CLASS_NAME;

    private TextView postInfoText, postHeading, postText;
    private Post selectedPost;
    private ProgressBar progressBarPostDetails;
    private Toaster toaster;
    private ImageView postImage;

    public PostDetailsActivity(){
        CLASS_NAME = getClass().getName();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        postInfoText = (TextView) findViewById(R.id.postInfoText);
        postHeading = (TextView) findViewById(R.id.postHeading);
        postText = (TextView) findViewById(R.id.postText);

        selectedPost = AppContext.getContext().getSelectedPost();


        postInfoText.setText("Post from user " + selectedPost.getUser().getUsername() + " posted on " + Helper.formatTime(selectedPost.getCreatedAt()));
        postHeading.setText(selectedPost.getHeading());
        postText.setText(selectedPost.getText());

        progressBarPostDetails = (ProgressBar) findViewById(R.id.progressBarPostDetails);
        progressBarPostDetails.setVisibility(View.INVISIBLE);
        postImage = (ImageView) findViewById(R.id.postDetailsImage);
        toaster = new Toaster(this);

        loadPostImage();


    }

    public void goToPosts(View view){
        Intent i = new Intent(PostDetailsActivity.this, PostsActivity.class);
        startActivity(i);
    }

    public void deletePost(View view){
        progressBarPostDetails.setVisibility(View.VISIBLE);
        Helper.blockTouch(getWindow());
        DBHelper.deletePost(progressBarPostDetails, getWindow(), toaster, this, selectedPost.getId());
    }

    private void loadPostImage(){
        Log.i(CLASS_NAME, "loading post image");
        if (selectedPost.getPicture()!= null){
            Log.i(CLASS_NAME, "selected post does have image");
            final String localPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).getAbsolutePath().concat("/").concat(selectedPost.getPicture());
            File file = new File(localPath);
            if (file.exists()){
                Log.i(CLASS_NAME, "selected post image is already downloaded");
                postImage.setImageBitmap(BitmapFactory.decodeFile(localPath));
            }
            else{
                Log.i(CLASS_NAME, "selected post image has to download the image firs.");
                downloadWithTransferUtility(selectedPost.getPicture());
            }
        }
    }

    private void downloadWithTransferUtility(final String photo) {

        progressBarPostDetails.setVisibility(View.VISIBLE);
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
                    Log.i(CLASS_NAME, "Successfully downloaded image.");
                    postImage.setImageBitmap(BitmapFactory.decodeFile(localPath));
                    progressBarPostDetails.setVisibility(View.INVISIBLE);
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
                progressBarPostDetails.setVisibility(View.INVISIBLE);
                Helper.unblockTouch(getWindow());
            }
        });
    }

}
