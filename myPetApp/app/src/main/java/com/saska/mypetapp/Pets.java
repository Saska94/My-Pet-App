package com.saska.mypetapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
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
import com.saska.mypetapp.singletons.AppContext;

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

    private Switch lostAddoptionSwitch;
    private boolean checked;

    private Button clearFilters, addPet;

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

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(this);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);

        petProgressBar = (ProgressBar) findViewById(R.id.petsProgressBar);
        petProgressBar.setVisibility(View.VISIBLE);
        mAdapter.setProgressBarPets(petProgressBar);
        msgText = (TextView) findViewById(R.id.msgText);
        msgText.setVisibility(View.INVISIBLE);

        lostAddoptionSwitch = (Switch) findViewById(R.id.lostAddoptionSwitch);
        checked = lostAddoptionSwitch.isChecked();

        clearFilters = (Button) findViewById(R.id.clearFilters);
        addPet = (Button) findViewById(R.id.addPet);
        if (AppContext.getContext().getActiveUser().isUser()){
            addPet.setVisibility(View.INVISIBLE);
        }
        else {
            addPet.setVisibility(View.VISIBLE);
        }

        lostAddoptionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checked = isChecked;
                loadSwitchItems();
            }
        });

        refreshFromDatabase();

    }


    @Override
    public void onResume() {
        super.onResume();
        msgText.setVisibility(View.INVISIBLE);
        clearFilterButton();

        // Query list data when we return to the screen
        //query();
    }

    private void loadSwitchItems(){
        List<Pet> petList = (AppContext.getContext().getFilteredPets() == null) ? AppContext.getContext().getAllPets() : AppContext.getContext().getFilteredPets();
        List<Pet> listToView = new ArrayList<>();
        for(Pet pet : petList){
            if ((checked && pet.getAdoption()==1) || (!checked && pet.getAdoption()==0)){
                listToView.add(pet);
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

    private void clearFilterButton(){
        if (AppContext.getContext().getFilteredPets() == null){
            clearFilters.setEnabled(false);
        }
        else{
            clearFilters.setEnabled(true);
        }
    }

    public void refreshFromDatabase(){
        ClientFactory.appSyncClient().query(ListPetsQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    private GraphQLCall.Callback<ListPetsQuery.Data> queryCallback = new GraphQLCall.Callback<ListPetsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListPetsQuery.Data> response) {

            mPets = new ArrayList<>(response.data().listPets().items());

            Log.i(CLASS_NAME, "Retrieved list items: " + mPets.toString());
            Log.i("ASD", "RETRIVED SIZE : " + mPets.size());
            final List<Pet> allPets = new ArrayList<>();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    petList = new ArrayList<>();
                    for (ListPetsQuery.Item pet: mPets) {
                        Pet dbPet = new Pet(pet);
                        allPets.add(dbPet);
                            String localPath = Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS).getAbsolutePath().concat("/").concat("public/avatar.png");
                        if (pet.picture() != null){
                            localPath = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS).getAbsolutePath().concat("/").concat(pet.picture());
                        }

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        dbPet.setImageBitmap(BitmapFactory.decodeFile(localPath,options));
                        if ((checked && dbPet.getAdoption()==1) || (!checked && dbPet.getAdoption()==0)){
                            petList.add(dbPet);
                        }
                    }
                    AppContext.getContext().setAllPets(allPets);
                    List<Pet> filteredPets = AppContext.getContext().getFilteredPets();
                    if (filteredPets != null ){
                        petList.clear();
                        for (Pet pet : filteredPets){
                            if ((checked && pet.getAdoption()==1) || (!checked && pet.getAdoption()==0)){
                                petList.add(pet);
                            }
                        }
                    }
                    mAdapter.setItems(petList);
                    mAdapter.notifyDataSetChanged();
                    if(petList.isEmpty()){
                        msgText.setVisibility(View.VISIBLE);
                    }
                    else {
                        msgText.setVisibility(View.INVISIBLE);
                    }
                    petProgressBar.setVisibility(View.INVISIBLE);
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

    public void goToAddNewPet(View view){
        Intent newUser = new Intent(Pets.this, AddPetActivity.class);
        startActivity(newUser);
    }

    public void goToFilters(View view){
        Intent i = new Intent(Pets.this, FilterPetsActivity.class);
        startActivity(i);
    }

    public void clearFilters(View view){
        AppContext.getContext().setFilteredPets(null);
        clearFilterButton();
        loadSwitchItems();
    }

    public void goToUser(View view){
        Intent i = new Intent(Pets.this, UserActivity.class);
        startActivity(i);
    }



}
