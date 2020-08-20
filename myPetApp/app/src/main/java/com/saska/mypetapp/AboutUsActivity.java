package com.saska.mypetapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
    }

    public void goToUser(View view){
        Intent i = new Intent(AboutUsActivity.this, UserActivity.class);
        startActivity(i);
    }

    public void callUs(View view){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:+381611234567"));

        if (ActivityCompat.checkSelfPermission(AboutUsActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AboutUsActivity.this, new String[]{Manifest.permission.CALL_PHONE},1);
            startActivity(callIntent);
        }

        startActivity(callIntent);
    }

}
