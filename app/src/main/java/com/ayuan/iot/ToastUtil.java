package com.ayuan.iot;

import android.app.Activity;
import android.widget.Toast;

public class ToastUtil {
    private static Toast toast;

    public static void showToast(Activity activity, String msg) {
        if (toast == null) {
            toast = Toast.makeText(activity, "", Toast.LENGTH_SHORT);
        }
        toast.setText(msg);
        toast.show();
    }
}
