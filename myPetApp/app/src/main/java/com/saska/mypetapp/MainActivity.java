package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.saska.mypetapp.db.ClientFactory;
import com.saska.mypetapp.helper.Helper;
import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.services.AwsClient;

public class MainActivity extends AppCompatActivity {

    private AwsClient awsClient;
    private Toaster toaster;
    private ProgressBar progressBar;
    private static String CLASS_NAME;

    public MainActivity(){
        CLASS_NAME = getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        awsClient = new AwsClient(this);
        awsClient.initialize();
        toaster = new Toaster(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBarMain);

        ClientFactory.init(this);

        Helper.requestReadPermission(this, CLASS_NAME);
        Helper.requestWritePermission(this,CLASS_NAME);

    }


    public void goToNewUser(View view){
        Intent newUser = new Intent(MainActivity.this, NewUserActivity.class);
        startActivity(newUser);
    }

    public void login(View view){

        String username = ((EditText) findViewById(R.id.usernameLogin)).getText().toString();
        String password = ((EditText) findViewById(R.id.passwordLogin)).getText().toString();

        if (username.isEmpty() || password.isEmpty()){
            toaster.make("Please provide both username and password");
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            Helper.blockTouch(getWindow());
            // Sign-in with Cognito
            awsClient.signIn(progressBar, getWindow(), username, password);
        }
    }

}
