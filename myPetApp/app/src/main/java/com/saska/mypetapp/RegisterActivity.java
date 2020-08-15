package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.helper.Helper;
import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.singletons.AppContext;


public class RegisterActivity extends AppCompatActivity {

    private Toaster toaster;
    private String passedUsername;
    private ProgressBar progressBarRegister;

    private static String CLASS_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        CLASS_NAME = getClass().getName();

        toaster = new Toaster(this);
        passedUsername = AppContext.getContext().getUsername();
        progressBarRegister = (ProgressBar) findViewById(R.id.progressBarRegister);

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
            progressBarRegister.setVisibility(View.VISIBLE);
            Helper.blockTouch(getWindow());
            DBHelper.createUser(this, progressBarRegister, getWindow(), passedUsername, name, surname, number);
        }


    }


}
