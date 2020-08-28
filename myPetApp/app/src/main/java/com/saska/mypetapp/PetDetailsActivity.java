package com.saska.mypetapp;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.saska.mypetapp.db.ClientFactory;
import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.db.Pet;
import com.saska.mypetapp.helper.Helper;
import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.singletons.AppContext;

import java.io.File;

public class PetDetailsActivity extends AppCompatActivity {

    private  static String CLASS_NAME;
    ProgressBar progressBarPetDetails;
    Pet selectedPet;

    TextView name, description, location, type, chip;
    ImageView petImage;
    boolean isImageFitToScreen;
    Button reserved;

    Toaster toaster;

    public PetDetailsActivity(){
        CLASS_NAME = getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_details);

        toaster = new Toaster(this);

        selectedPet = AppContext.getContext().getSelectedPet();
        name = (TextView) findViewById(R.id.detailsName);
        name.setText(selectedPet.getName());
        description = (TextView) findViewById(R.id.detailsDescription);
        description.setText(selectedPet.getDescription());
        location = (TextView) findViewById(R.id.detailsLocation);
        location.setText(selectedPet.getLocation());
        type = (TextView) findViewById(R.id.detailsType);
        type.setText(selectedPet.getType());
        chip = (TextView) findViewById(R.id.detailsChip);
        chip.setText(selectedPet.getChip());
        progressBarPetDetails = (ProgressBar) findViewById(R.id.progressBarPetDetails);
        progressBarPetDetails.setVisibility(View.INVISIBLE);
        petImage = (ImageView) findViewById(R.id.petImage);
        isImageFitToScreen = false;
        final int originalHeight = petImage.getLayoutParams().height;
        final int originalWidth = petImage.getLayoutParams().width;
        final float weight = 4.0f;

        petImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isImageFitToScreen) {
                    isImageFitToScreen=false;
                    petImage.setLayoutParams(new LinearLayout.LayoutParams(originalWidth, originalHeight, weight));
                    petImage.setAdjustViewBounds(true);
                }else{
                    isImageFitToScreen=true;
                    petImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    petImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            }
        });

        reserved = (Button) findViewById(R.id.foundBtn);
        if (selectedPet.getReserved() == 1) {
             reserved.setEnabled(false);
        }
        else {
            reserved.setEnabled(true);
        }

        loadPetImage();

    }

    public void goToPets(View view){
        Intent i = new Intent(PetDetailsActivity.this, Pets.class);
        startActivity(i);
    }

    private void loadPetImage(){
        if (selectedPet.getPicture()!= null){
            final String localPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).getAbsolutePath().concat("/").concat(selectedPet.getPicture());
            File file = new File(localPath);
            if (file.exists()){
                petImage.setImageBitmap(BitmapFactory.decodeFile(localPath));
            }
            else{
                downloadWithTransferUtility(selectedPet.getPicture());
            }
        }
    }

    private void downloadWithTransferUtility(final String photo) {

        progressBarPetDetails.setVisibility(View.VISIBLE);
        Helper.blockTouch(getWindow());

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
                    // Handle a completed upload.
                    petImage.setImageBitmap(BitmapFactory.decodeFile(localPath));
                    progressBarPetDetails.setVisibility(View.INVISIBLE);
                    Helper.unblockTouch(getWindow());
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;

                Log.d(CLASS_NAME, "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
                Log.e(CLASS_NAME, "Unable to download the file.", ex);
                progressBarPetDetails.setVisibility(View.INVISIBLE);
                Helper.unblockTouch(getWindow());
            }
        });
    }

    public void reservePet(View view){
        progressBarPetDetails.setVisibility(View.VISIBLE);
        Helper.blockTouch(getWindow());
        selectedPet.setReserved(1);
        DBHelper.updatePet(progressBarPetDetails, getWindow(), toaster, selectedPet);
        reserved.setEnabled(false);
    }

    public void removePet(View view){
        progressBarPetDetails.setVisibility(View.VISIBLE);
        Helper.blockTouch(getWindow());
        DBHelper.deletePet(progressBarPetDetails, getWindow(), toaster, this, selectedPet.getId());
    }

}
