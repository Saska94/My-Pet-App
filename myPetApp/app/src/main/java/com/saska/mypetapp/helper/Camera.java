package com.saska.mypetapp.helper;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.util.List;

public class Camera {

    private static String className;
    public static final int PHOTO_TAKEN = 1001;
    public static final int PICK_IMAGE = 100;
    private final Activity activity;
    private Bitmap image;
    private String picturePath;

    public Camera(Activity activity){
        className = getClass().getName();
        this.activity = activity;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        activity.startActivityForResult(gallery, PICK_IMAGE);
    }

    public void takePhoto(){
        Log.i(className, "Taking photo...");
        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivityForResult(takePhoto,PHOTO_TAKEN);
    }

    public void displayPhoto(ImageView imageView, Intent intent){
        Log.i(className, "Displaying photo...");
        Bundle extras = intent.getExtras();
        Bitmap bitmap = (Bitmap) extras.get("data");
        Uri uri = intent.getData();
        if (bitmap != null){
            image = bitmap;
            imageView.setImageBitmap(image);
        }
        else if (uri != null){
            imageView.setImageURI(uri);
        }

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(uri,
                filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        this.picturePath = picturePath;
    }

    public boolean hasCamera(){
        Log.i(className, "Checking if device has camera...");
        PackageManager manager = activity.getPackageManager();
        return manager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public boolean hasCameraApplication(){
        Log.i(className, "Checking if device has camera application...");
        PackageManager manager = activity.getPackageManager();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> list = manager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public Bitmap getImage(){
        return image;
    }


}
