package com.saska.mypetapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.amplify.generated.graphql.ListPostsQuery;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.saska.mypetapp.db.ClientFactory;
import com.saska.mypetapp.db.Post;
import com.saska.mypetapp.helper.MyPostsAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class PostsActivity extends AppCompatActivity {

    private static String CLASS_NAME;
    MyPostsAdapter mAdapter;
    RecyclerView mRecyclerView;

    private ArrayList<ListPostsQuery.Item> posts;
    private TextView emptyMsg;
    private List<Post> postList;
    ProgressBar progressBarPosts;


    public PostsActivity(){
        CLASS_NAME = getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);


        mRecyclerView = findViewById(R.id.rv_posts);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new MyPostsAdapter(this);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);

        emptyMsg = (TextView) findViewById(R.id.postEmptyMsg);
        emptyMsg.setVisibility(View.INVISIBLE);

        progressBarPosts = (ProgressBar) findViewById(R.id.progressBarPosts);
        progressBarPosts.setVisibility(View.INVISIBLE);
        mAdapter.setProgressBarPosts(progressBarPosts);

    }

    @Override
    public void onResume() {
        super.onResume();
        // Query list data when we return to the screen
        query();
    }

    public void query(){
        ClientFactory.appSyncClient().query(ListPostsQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    private GraphQLCall.Callback<ListPostsQuery.Data> queryCallback = new GraphQLCall.Callback<ListPostsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListPostsQuery.Data> response) {

            posts = new ArrayList<>(response.data().listPosts().items());

            Log.i(CLASS_NAME, "Retrieved Posts: " + posts.toString());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    postList = new ArrayList<>();
                    for (ListPostsQuery.Item post: posts) {
                        Post dbPost = new Post(post);
                        postList.add(dbPost);
                    }
                    mAdapter.setItems(postList);
                    mAdapter.notifyDataSetChanged();
                    if(postList.isEmpty()){
                        Log.i(CLASS_NAME, "setting text to visible");
                        emptyMsg.setVisibility(View.VISIBLE);
                    }
                    else {
                        emptyMsg.setVisibility(View.INVISIBLE);
                        Log.i(CLASS_NAME, "setting text to invisible");
                    }
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(CLASS_NAME, e.toString());
        }
    };

    public void goToUser(View view){
        Intent i = new Intent(PostsActivity.this, UserActivity.class);
        startActivity(i);
    }



    public void addPost(View view){
        Intent i = new Intent(PostsActivity.this, AddPostActivity.class);
        startActivity(i);
    }
}
