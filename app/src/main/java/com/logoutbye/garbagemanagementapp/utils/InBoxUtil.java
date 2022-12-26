package com.logoutbye.garbagemanagementapp.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class InBoxUtil {

public  Context context;
//    public AttendanceUtil(Context context) {
//        this.context=context;
//    }

    public void submitInBoxPrefrence(Context context, String lat,String longi ,String message) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

// Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

// Storing the key and its value as the data fetched from edittext
        myEdit.putString("lati",  lat);
        myEdit.putString("longi",  longi);
        myEdit.putString("message",  message);

        myEdit.commit();
    }


    public String getInBoxDestLatPrefrence(Context context) {

        SharedPreferences sh = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return sh.getString("lati",  "0.0");

    }

    public String getInBoxDestLongiPrefrence(Context context) {

        SharedPreferences sh = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return sh.getString("longi",  "0.0");

    }


    public String getInBoxMessagePrefrence(Context context) {

        SharedPreferences sh = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return sh.getString("message", "No details");

    }

}
