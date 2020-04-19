package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.helper.Toaster;


public class RegisterActivity extends AppCompatActivity {

    private Toaster toaster;
    private String passedUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        toaster = new Toaster(this);
        passedUsername = getIntent().getStringExtra("USER");

    }


    public void cancel(View view){
        AWSMobileClient.getInstance().signOut();
        Intent cancel = new Intent(this, MainActivity.class);
        startActivity(cancel);
    }

    public void register(View view){

        String name = ((EditText) findViewById(R.id.registerName)).getText().toString();
        String surname =((EditText) findViewById(R.id.registerSurname)).getText().toString();
        String number = ((EditText) findViewById(R.id.registerNumber)).getText().toString();

        if (name.isEmpty() || surname.isEmpty() || number.isEmpty()){
            toaster.make("Please fill in all fields.");
        }
        else{
            DBHelper.createUser(true, passedUsername, name, surname, number);
            Intent userIntent = new Intent(this, UserActivity.class);
            startActivity(userIntent);
        }


    }

}
