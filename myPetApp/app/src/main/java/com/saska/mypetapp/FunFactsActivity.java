package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.amplify.generated.graphql.ListFFactsQuery;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.saska.mypetapp.db.ClientFactory;
import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.db.FFact;
import com.saska.mypetapp.helper.Helper;
import com.saska.mypetapp.helper.MyFFAdapter;
import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.singletons.AppContext;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class FunFactsActivity extends AppCompatActivity {

    private static String CLASS_NAME;
    MyFFAdapter mAdapter;
    RecyclerView mRecyclerView;

    private ArrayList<ListFFactsQuery.Item> ffacts;
    private List<FFact> fflist;
    private TextView emptyMsg;
    private Toaster toaster;
    private ProgressBar progressBarFF;

    private EditText ffText;
    private Button addFFBtn;

    public FunFactsActivity(){
        CLASS_NAME = getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fun_facts);

        mRecyclerView = findViewById(R.id.rv_fun_facts);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        toaster = new Toaster(this);

        mAdapter = new MyFFAdapter(this);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        emptyMsg = (TextView) findViewById(R.id.emptyMsg);
        emptyMsg.setVisibility(View.INVISIBLE);

        progressBarFF = (ProgressBar) findViewById(R.id.progressBarFF);
        progressBarFF.setVisibility(View.INVISIBLE);

        ffText = (EditText) findViewById(R.id.fun_fact_text);
        addFFBtn = (Button) findViewById(R.id.addFact);
        if(AppContext.getContext().getActiveUser().isUser()){
            ffText.setVisibility(View.GONE);
            addFFBtn.setVisibility(View.INVISIBLE);
        }
        else {
            ffText.setVisibility(View.VISIBLE);
            addFFBtn.setVisibility(View.VISIBLE);
        }

    }

    public void goToUser(View view){
        Intent i = new Intent(FunFactsActivity.this, UserActivity.class);
        startActivity(i);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Query list data when we return to the screen
        query();
    }

    public void query(){
        ClientFactory.appSyncClient().query(ListFFactsQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    private GraphQLCall.Callback<ListFFactsQuery.Data> queryCallback = new GraphQLCall.Callback<ListFFactsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListFFactsQuery.Data> response) {

            ffacts = new ArrayList<>(response.data().listFFacts().items());

            Log.i(CLASS_NAME, "Retrieved Fun Facts: " + ffacts.toString());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fflist = new ArrayList<>();
                    for (ListFFactsQuery.Item ffact: ffacts) {
                        FFact dbFfact = new FFact(ffact);
                        fflist.add(dbFfact);
                    }
                    mAdapter.setItems(fflist);
                    mAdapter.notifyDataSetChanged();
                    Log.i(CLASS_NAME, "Size of fflist is :" + fflist.size());
                    if(fflist.isEmpty()){
                        emptyMsg.setVisibility(View.VISIBLE);
                    }
                    else {
                        emptyMsg.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(CLASS_NAME, e.toString());
        }
    };


    public void addFunFact(View view){
        if (ffText.getText().toString().isEmpty()){
            toaster.make("Please fill in the fun fact text");
        }
        else {
            progressBarFF.setVisibility(View.VISIBLE);
            Helper.blockTouch(getWindow());
            DBHelper.addFunFact(toaster, progressBarFF, getWindow(), ffText);
            fflist.add(new FFact(ffText.getText().toString()));
            mAdapter.notifyDataSetChanged();
        }
    }

}
