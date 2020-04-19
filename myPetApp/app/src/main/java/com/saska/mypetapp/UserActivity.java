package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.saska.mypetapp.db.User;

public class UserActivity extends AppCompatActivity {

    private User activeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        activeUser = (User) getIntent().getSerializableExtra("USER");

        TextView welcomeText = (TextView) findViewById(R.id.welcomeText);
        String newText = welcomeText.getText().toString().concat(activeUser.getName()).concat("!");
        welcomeText.setText(newText);

    }

    public void logOut(View view){
            AWSMobileClient.getInstance().signOut();
            Intent i = new Intent(UserActivity.this, MainActivity.class);
            startActivity(i);
    }

}
