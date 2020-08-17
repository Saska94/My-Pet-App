package com.saska.mypetapp.helper;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Helper {

    public static void blockTouch(Window window){
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static void unblockTouch(Window window){
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static void requestWritePermission(Activity activity, String CLASS_NAME){
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Log.d(CLASS_NAME, "WRITE_EXTERNAL_STORAGE permission not granted! Requesting...");
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    2);
        }
    }

    public static void requestReadPermission(Activity activity, String CLASS_NAME){
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Log.d(CLASS_NAME, "READ_EXTERNAL_STORAGE permission not granted! Requesting...");
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }
    }

    public static String getPictureName(String picturePath){
        return picturePath.substring(picturePath.lastIndexOf("/"));
    }

    public static String formatTime(String unformatted){
        String[] parts = unformatted.split("T");
        String result = parts[0].concat(" - ").concat(parts[1].substring(0, parts[1].indexOf(".")));
        return result;
    }

}
