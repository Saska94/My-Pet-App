package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.helper.Helper;
import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.singletons.AppContext;

public class AddPostActivity extends AppCompatActivity {

    private static String CLASS_NAME;
    private Toaster toaster;

    private ProgressBar progressBarNewPost;

    public AddPostActivity(){
        CLASS_NAME = getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        toaster = new Toaster(this);

        progressBarNewPost = (ProgressBar) findViewById(R.id.progressBarNewPost);
        progressBarNewPost.setVisibility(View.INVISIBLE);

    }

    public void goToPosts(View view){
        Intent i = new Intent(AddPostActivity.this, PostsActivity.class);
        startActivity(i);
    }

    public void addPost(View view){

        EditText postHeading = (EditText) findViewById(R.id.newPostHeading);
        EditText postText = (EditText) findViewById(R.id.newPostText);

        if (postHeading.getText().toString().isEmpty()){
            toaster.make("Please provide the post heading.");
        }
        else {
            progressBarNewPost.setVisibility(View.VISIBLE);
            Helper.blockTouch(getWindow());
            DBHelper.addPost(progressBarNewPost, getWindow(), toaster, postHeading.getText().toString(), postText.getText().toString(), AppContext.getContext().getActiveUser());
        }

    }

}
