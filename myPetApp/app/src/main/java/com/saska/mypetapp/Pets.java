package com.saska.mypetapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.amplify.generated.graphql.ListPetsQuery;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.saska.mypetapp.db.ClientFactory;
import com.saska.mypetapp.db.Pet;
import com.saska.mypetapp.helper.MyAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class Pets extends AppCompatActivity {

    RecyclerView mRecyclerView;
    MyAdapter mAdapter;

    private static String CLASS_NAME;
    private ArrayList<ListPetsQuery.Item> mPets;
    ProgressBar petProgressBar;
    TextView msgText;
    private List<Pet> petList;

    public Pets(){
        CLASS_NAME = getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pets);

        mRecyclerView = findViewById(R.id.recycler_view);

        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(this);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);

        petProgressBar = (ProgressBar) findViewById(R.id.petsProgressBar);
        petProgressBar.setVisibility(View.VISIBLE);
        msgText = (TextView) findViewById(R.id.msgText);
        msgText.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onResume() {
        super.onResume();
        msgText.setVisibility(View.INVISIBLE);

        // Query list data when we return to the screen
        query();
    }

    public void query(){
        ClientFactory.appSyncClient().query(ListPetsQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    private GraphQLCall.Callback<ListPetsQuery.Data> queryCallback = new GraphQLCall.Callback<ListPetsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListPetsQuery.Data> response) {

            mPets = new ArrayList<>(response.data().listPets().items());

            Log.i(CLASS_NAME, "Retrieved list items: " + mPets.toString());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    petList = new ArrayList<>();
                    for (ListPetsQuery.Item pet: mPets) {
                        Pet dbPet = new Pet(pet);
                        final String localPath = Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS).getAbsolutePath().concat("/").concat(pet.picture());
                        File file = new File(localPath);
                        if (!file.exists()){
                            downloadImage(pet.picture());
                        }
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        dbPet.setImageBitmap(BitmapFactory.decodeFile(localPath, options));
                        petList.add(dbPet);
                    }
                    petProgressBar.setVisibility(View.INVISIBLE);
                    mAdapter.setItems(petList);
                    mAdapter.notifyDataSetChanged();
                    if(mPets.isEmpty()){
                        msgText.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(CLASS_NAME, e.toString());
        }
    };

    private void downloadImage(final String photo) {

        final String localPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + photo;

        TransferObserver downloadObserver =
                ClientFactory.transferUtility().download(
                        photo,
                        new File(localPath));

        // Attach a listener to the observer to get state update and progress notifications
        downloadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                   Log.i(CLASS_NAME, "Image downloaded");
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e(CLASS_NAME, "Unable to download the file.", ex);
            }
        });
    }

}
