package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.saska.mypetapp.db.ClientFactory;
import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.db.User;
import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.services.AwsClient;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {

    private AwsClient awsClient;
    private Toaster toaster;

    private ArrayList<ListUsersQuery.Item> mUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        awsClient = new AwsClient(this);
        awsClient.initialize();
        toaster = new Toaster(this);

        ClientFactory.init(this);

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
            //get the user from DB
            try {
                final CountDownLatch loginLatch = new CountDownLatch (1);
                awsClient.signIn(loginLatch, username,password);
                loginLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ListUsersQuery.Item user = DBHelper.getUserByUsername(true, username);
            if (user == null){
                Log.i(this.getClass().getName(), "No user found!");
                // First time login, update the database
                Intent registerIntent = new Intent(this, RegisterActivity.class);
                registerIntent.putExtra("USERNAME", username);
                startActivity(registerIntent);
            }
            else {
                // User already exists in the DB, proceed to registered page
                Intent userIntent = new Intent(this, UserActivity.class);
                userIntent.putExtra("USER", new User(user));
                startActivity(userIntent);

            }

        }


    }


    private String getUser(String username){
        return null;
    }
}
