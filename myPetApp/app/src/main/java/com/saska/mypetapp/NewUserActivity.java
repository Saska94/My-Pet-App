package com.saska.mypetapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.services.AwsClient;

public class NewUserActivity extends AppCompatActivity {

    private AwsClient awsClient;
    private Toaster toaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        awsClient = new AwsClient(this);
        awsClient.initialize();

        toaster = new Toaster(this);

    }

    public void signUp(View view) {
        EditText username = ((EditText) findViewById(R.id.usernameSignIn));
        EditText password = ((EditText) findViewById(R.id.passwordSignIn));
        EditText email = ((EditText) findViewById(R.id.email));
        if (username.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
            toaster.make("Please provide username, password and email.");
        } else {
            EditText code = (EditText) findViewById(R.id.confirmationCode);
            Button confirmButton = (Button) findViewById(R.id.confirmUserBtn);
            awsClient.signUp(username, password, email, code, confirmButton);
        }
    }

    public void confirmCode(View view) {
        String username = ((EditText) findViewById(R.id.usernameSignIn)).getText().toString();
        String code = ((EditText) findViewById(R.id.confirmationCode)).getText().toString();
        if (code.isEmpty()){
            toaster.make("Confirmation code cannot be empty");
        }
        else{
            awsClient.confirmCode(username, code);
        }
    }

}
