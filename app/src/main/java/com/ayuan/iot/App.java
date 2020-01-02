package com.ayuan.iot;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class App extends Application {
    private Context context;
    private static Toast toast;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public Context getContext() {
        return context;
    }

}
