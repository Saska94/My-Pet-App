package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.saska.mypetapp.db.ClientFactory;
import com.saska.mypetapp.db.User;
import com.saska.mypetapp.helper.MyUsersAdapter;
import com.saska.mypetapp.singletons.AppContext;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class UsersActivity extends AppCompatActivity {

    public static String CLASS_NAME;

    private Switch workersUsersSwitch;
    private boolean checked;
    private ArrayList<ListUsersQuery.Item> mUsers;
    private List<User> userList;

    RecyclerView mRecyclerView;
    MyUsersAdapter mAdapter;
    TextView msgText;
    ProgressBar usersProgressBar;

    public UsersActivity(){
        CLASS_NAME = getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mRecyclerView = findViewById(R.id.recycler_view);

        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // specify an adapter (see also next example)
        mAdapter = new MyUsersAdapter(this);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        msgText = (TextView) findViewById(R.id.msgText);
        msgText.setVisibility(View.INVISIBLE);
        usersProgressBar = (ProgressBar) findViewById(R.id.progressBarUsers);
        usersProgressBar.setVisibility(View.VISIBLE);
        mAdapter.setProgresBarUsers(usersProgressBar);


        workersUsersSwitch = (Switch) findViewById(R.id.workersUsersSwitch);
        workersUsersSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checked = isChecked;
                loadSwitchItems();
            }
        });
        refreshFromDatabase();
    }


    public void goToUser(View view){
        Intent i = new Intent(UsersActivity.this, UserActivity.class);
        startActivity(i);
    }

    public void refreshFromDatabase(){
        ClientFactory.appSyncClient().query(ListUsersQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    private GraphQLCall.Callback<ListUsersQuery.Data> queryCallback = new GraphQLCall.Callback<ListUsersQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListUsersQuery.Data> response) {

            mUsers = new ArrayList<>(response.data().listUsers().items());

            Log.i(CLASS_NAME, "Retrieved list items: " + mUsers.toString());
            final List<User> allUsers = new ArrayList<>();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    userList = new ArrayList<>();
                    for (ListUsersQuery.Item user: mUsers) {
                        User dbUser = new User(user);
                        allUsers.add(dbUser);

                        if ((checked && dbUser.isUser()) || (!checked && dbUser.isWorker())){
                            userList.add(dbUser);
                        }
                    }
                    AppContext.getContext().setAllUsers(allUsers);

                    mAdapter.setItems(userList);
                    mAdapter.notifyDataSetChanged();
                    if(userList.isEmpty()){
                        msgText.setVisibility(View.VISIBLE);
                    }
                    else {
                        msgText.setVisibility(View.INVISIBLE);
                    }
                    usersProgressBar.setVisibility(View.INVISIBLE);
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(CLASS_NAME, e.toString());
        }
    };

    private void loadSwitchItems(){
        List<User> allUsers  = AppContext.getContext().getAllUsers();
        List<User> listToView = new ArrayList<>();
        for(User user : allUsers){
            if ((checked && user.isUser()) || (!checked && user.isWorker())){
                listToView.add(user);
            }
        }
        mAdapter.setItems(listToView);
        mAdapter.notifyDataSetChanged();

        if(listToView.isEmpty()){
            msgText.setVisibility(View.VISIBLE);
        }
        else {
            msgText.setVisibility(View.INVISIBLE);
        }
    }

}
