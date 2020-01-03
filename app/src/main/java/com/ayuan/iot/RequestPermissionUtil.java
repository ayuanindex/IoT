package com.ayuan.iot;

import android.app.Activity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class RequestPermissionUtil {
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PermissionIsRequest {
        String[] value();
    }

    public static void requestPermission(Activity context) {
        PermissionIsRequest permissions = context.getClass().getAnnotation(PermissionIsRequest.class);
        context.requestPermissions(permissions.value(), 0);
    }
}
