package com.saska.mypetapp.helper;

import android.view.Window;
import android.view.WindowManager;

public class Helper {

    public static void blockTouch(Window window){
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static void unblockTouch(Window window){
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}
