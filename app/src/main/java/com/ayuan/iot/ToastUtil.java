package com.ayuan.iot;

import android.app.Activity;
import android.widget.Toast;

public class ToastUtil {
    public static void showToast(Activity activity, String msg) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }
}
