package com.saska.mypetapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.saska.mypetapp.db.User;

public class UserActivity extends AppCompatActivity {

    private User activeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        activeUser = (User) getIntent().getSerializableExtra("USER");

        TextView test = (TextView) findViewById(R.id.test);
        String aaa = test.getText().toString().concat(activeUser.getName());
        test.setText(aaa);

    }
}
