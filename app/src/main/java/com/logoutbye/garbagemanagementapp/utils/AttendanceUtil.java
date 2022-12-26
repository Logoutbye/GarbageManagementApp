package com.logoutbye.garbagemanagementapp.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class AttendanceUtil {

public  Context context;
//    public AttendanceUtil(Context context) {
//        this.context=context;
//    }

    public void submitAttendancePrefrence(Context context, boolean yes ) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

// Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

// Storing the key and its value as the data fetched from edittext
        myEdit.putBoolean("todayAttend", yes);

        myEdit.commit();
    }


    public boolean getAttendancePrefrence(Context context) {

        SharedPreferences sh = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return sh.getBoolean("todayAttend", false);

    }


}
