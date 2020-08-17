package com.saska.mypetapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.db.Post;
import com.saska.mypetapp.helper.Helper;
import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.singletons.AppContext;

public class PostDetailsActivity extends AppCompatActivity {

    private TextView postInfoText, postHeading, postText;
    private Post selectedPost;
    private ProgressBar progressBarPetDetails;
    private Toaster toaster;

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

        progressBarPetDetails = (ProgressBar) findViewById(R.id.progressBarPetDetails);
        progressBarPetDetails.setVisibility(View.INVISIBLE);
        toaster = new Toaster(this);


    }

    public void goToPosts(View view){
        Intent i = new Intent(PostDetailsActivity.this, PostsActivity.class);
        startActivity(i);
    }

    public void deletePost(View view){
        progressBarPetDetails.setVisibility(View.VISIBLE);
        Helper.blockTouch(getWindow());
        DBHelper.deletePost(progressBarPetDetails, getWindow(), toaster, this, selectedPost.getId());
    }

}
