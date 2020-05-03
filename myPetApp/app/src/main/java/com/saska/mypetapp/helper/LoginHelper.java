package com.saska.mypetapp.helper;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.saska.mypetapp.MainActivity;
import com.saska.mypetapp.RegisterActivity;
import com.saska.mypetapp.UserActivity;
import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.db.User;
import com.saska.mypetapp.singletons.AppContext;

public class LoginHelper {

    private static String CLASS_NAME;
    private Activity activity;

    public LoginHelper(Activity activity){
        this.activity = activity;
        CLASS_NAME = getClass().getName();
    }

    public void proceedLogin(String username){

        ListUsersQuery.Item user = DBHelper.getUserByUsername(true, username);
        if (user == null){
            Log.i(this.getClass().getName(), "No user found!");
            // First time login, update the database
            Intent registerIntent = new Intent(activity, RegisterActivity.class);
            AppContext.getContext().setUsername(username);
            registerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.getApplicationContext().startActivity(registerIntent);
        }
        else {
            // User already exists in the DB, proceed to registered page
            Intent userIntent = new Intent(activity, UserActivity.class);
            AppContext.getContext().setActiveUser(new User(user));
            userIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.getApplicationContext().startActivity(userIntent);
        }

    }

    public static void proceedLogout(Activity activity){
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.getApplicationContext().startActivity(intent);
    }

}
