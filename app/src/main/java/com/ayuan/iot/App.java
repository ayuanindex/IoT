package com.ayuan.iot;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class App extends Application {
    private static Context context;
    private static Toast toast;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    @SuppressLint("ShowToast")
    public static void showToast(String msg) {
        if (toast != null) {
            toast.cancel();
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        }
        toast.show();
    }
}
