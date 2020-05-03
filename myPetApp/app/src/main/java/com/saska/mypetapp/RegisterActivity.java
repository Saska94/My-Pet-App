package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.db.User;
import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.singletons.AppContext;


public class RegisterActivity extends AppCompatActivity {

    private Toaster toaster;
    private String passedUsername;

    private static String CLASS_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        CLASS_NAME = getClass().getName();

        toaster = new Toaster(this);
        passedUsername = AppContext.getContext().getUsername();

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
            User user = waitForDatabaseUser();
            Intent userIntent = new Intent(this, UserActivity.class);
            userIntent.putExtra("USER", user);
            startActivity(userIntent);
        }


    }

    private User waitForDatabaseUser(){
        while (true){
            ListUsersQuery.Item item =  DBHelper.getUserByUsername(true, passedUsername);
            if (item == null){
                try {
                    Log.i(CLASS_NAME, "Didn't get the user from the database. Going to sleep.");
                    Thread.sleep(3_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                return new User(item);
            }
        }
    }

}
