package com.logoutbye.garbagemanagementapp;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {

    public static double deslat;
    public static double deslong;
    public static String message;
    public static boolean isTrashRequest;

    @Override
    public void onCreate() {
        super.onCreate();


        deslat = 0.0;
        deslong = 0.0;
        message = "No details";
        isTrashRequest = false;


        FirebaseApp.initializeApp(MyApplication.this);


    }

}
