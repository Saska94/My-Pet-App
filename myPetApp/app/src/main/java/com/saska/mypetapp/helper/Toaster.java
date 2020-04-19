package com.saska.mypetapp.helper;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.saska.mypetapp.R;


public class Toaster {

    private static String CLASS_NAME;
    private Activity activity;

    public Toaster(Activity activity){
        CLASS_NAME = getClass().getName();
        this.activity = activity;


    }

    public void make(String message){
        Log.i(CLASS_NAME, "making Toast");

        LayoutInflater inflater = (LayoutInflater) activity.getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate(R.layout.my_toast, (ViewGroup) activity.findViewById(R.id.my_toast_layout));
        TextView tv = (TextView) layout.findViewById(R.id.txtvw);
        tv.setText(message);
        Toast toast = new Toast(activity.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setView(layout);
        toast.show();
    }

}