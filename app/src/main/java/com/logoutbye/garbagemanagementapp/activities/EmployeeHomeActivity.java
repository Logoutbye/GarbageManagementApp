package com.logoutbye.garbagemanagementapp.activities;


import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.live.earth.map.satellite.map.street.view.gps.navigation.utils.InternetAvailable;
import com.logoutbye.garbagemanagementapp.MyApplication;
import com.logoutbye.garbagemanagementapp.R;
import com.logoutbye.garbagemanagementapp.dialogue.ComplainDialogue;
import com.logoutbye.garbagemanagementapp.models.Employee;
import com.logoutbye.garbagemanagementapp.receiver.AlertReceiver;
import com.logoutbye.garbagemanagementapp.utils.AttendanceUtil;
import com.logoutbye.garbagemanagementapp.utils.BinPoint;
import com.logoutbye.garbagemanagementapp.utils.InBoxUtil;
import com.logoutbye.garbagemanagementapp.utils.RoutPointClass;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class EmployeeHomeActivity extends AppCompatActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;


    private FirebaseAuth auth;
    private FirebaseUser userF;
    private ImageView btnSignOut, btnAttendance, btnNearbyBin, btnInbox, btnComplain, imgBack;
    private TextView txtAttendace;

    private double lati, longi;

    private boolean isPresent = false;
    private String strLocation = "";
    private AttendanceUtil attendanceUtil;
    private int isClickedForWhat = -1;
    private List<BinPoint> arrayListBin;
    private ProgressDialog locationProgress;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_home);
        auth = FirebaseAuth.getInstance();
        btnSignOut = findViewById(R.id.btnSignOut);
        btnAttendance = findViewById(R.id.btnAttendance);
        btnNearbyBin = findViewById(R.id.btnNearbyBin);
        txtAttendace = findViewById(R.id.txtAttendace);
        btnInbox = findViewById(R.id.btnInbox);
        btnComplain = findViewById(R.id.btnComplain);
        imgBack = findViewById(R.id.imgBack);


        attendanceUtil = new AttendanceUtil();
        if (attendanceUtil.getAttendancePrefrence(this)) {
            btnAttendance.setVisibility(View.INVISIBLE);
            txtAttendace.setVisibility(View.INVISIBLE);
        }


        FirebaseMessaging.getInstance().subscribeToTopic("/topics/employee");
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/" + auth.getUid());


        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        btnInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (InternetAvailable.Companion.isInternetAvailable(EmployeeHomeActivity.this)) {


                    if (MyApplication.isTrashRequest) {
                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            turnOnGPS();
                        } else {

                            try {
                                locationProgress = ProgressDialog.show(EmployeeHomeActivity.this, null,
                                        "Checking for request in inbox...",
                                        true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //2 for trash inbox
                            isClickedForWhat = 2;

                            getingNewCurrentLocation();
                        }
                    } else {
                        Toast.makeText(EmployeeHomeActivity.this, "No trash Request found in inbox", Toast.LENGTH_SHORT).show();

                    }

                } else {
                    Toast.makeText(EmployeeHomeActivity.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                }


            }
        });


        btnComplain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ComplainDialogue complainDicoalogue = new ComplainDialogue(EmployeeHomeActivity.this);
                    complainDicoalogue.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnNearbyBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (InternetAvailable.Companion.isInternetAvailable(EmployeeHomeActivity.this)) {

                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        turnOnGPS();
                    } else {

                        try {
                            locationProgress =
                                    ProgressDialog.show(EmployeeHomeActivity.this, null,
                                            "Getting Bin Locations Please wait...",
                                            true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // 1 for nearby bin
                        isClickedForWhat = 0;
                        arrayListBin = new ArrayList<BinPoint>();

                        getingNewCurrentLocation();
                    }


                } else {
                    Toast.makeText(EmployeeHomeActivity.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                }

            }
        });


        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (InternetAvailable.Companion.isInternetAvailable(EmployeeHomeActivity.this)) {

                    FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/employee");
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/" + auth.getUid());
                    auth.signOut();
                    userF = FirebaseAuth.getInstance().getCurrentUser();
                    if (userF == null) {
                        // user auth state is changed - user is null
                        // launch login activity
                        cancelAlarm();

                        Intent intent = new Intent(EmployeeHomeActivity.this, MainLauncherActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(EmployeeHomeActivity.this, "Some Error. Try Again", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EmployeeHomeActivity.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                }

            }
        });


        btnAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (InternetAvailable.Companion.isInternetAvailable(EmployeeHomeActivity.this)) {

                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        turnOnGPS();
                    } else {

                        Toast.makeText(EmployeeHomeActivity.this, "Wait for a moment ", Toast.LENGTH_SHORT).show();
                        btnAttendance.setEnabled(false);
                        btnAttendance.postDelayed(new Runnable() {
                            public void run() {
                                btnAttendance.setEnabled(true);
                            }
                        }, 8000);


                        //2 for attendance
                        isClickedForWhat = 1;
                        getingNewCurrentLocation();
                    }


                } else {

                    Toast.makeText(EmployeeHomeActivity.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                }
            }
        });


        cancelAlarm();
        startAlarm();


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void getingNewCurrentLocation() {
        try {

            locationRequest = new LocationRequest();


            locationRequest.setInterval(4000);
            locationRequest.setFastestInterval(4000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            googleApiClient.connect();
        } catch (Exception e) {


        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationChanged(Location location) {
        try {
            if (location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {

                try {
                    if (googleApiClient.isConnected()) {
                        googleApiClient.disconnect();
                    }
                    stopLocationUpdates();
                } catch (Exception e) {

                }

                lati = location.getLatitude();
                longi = location.getLongitude();

                if (location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {

                    if (isClickedForWhat == 0) {
                        getAllBinsFromFirebase();
                    } else if (isClickedForWhat == 1) {

                        getEmployeeAttendanceLocationFromFirebase();
                    } else if (isClickedForWhat == 2) {

                        Intent intent = new Intent(EmployeeHomeActivity.this, InboxForEmployeeHomeActivity.class);
                        Bundle bundle = new Bundle();


                        InBoxUtil inBoxUtil= new InBoxUtil();


                        RoutPointClass pointModel = new RoutPointClass(
                                lati,
                                longi,
                                Double.parseDouble(inBoxUtil.getInBoxDestLatPrefrence(this)),
                                Double.parseDouble(inBoxUtil.getInBoxDestLongiPrefrence(this)),
                                inBoxUtil.getInBoxMessagePrefrence(this));



                        bundle.putSerializable("model ", pointModel);
                        intent.putExtras(bundle);


                        locationProgress.dismiss();
                        MyApplication.isTrashRequest=false;


                        startActivity(intent);

                    }

                    try {
                        if (googleApiClient.isConnected()) {
                            googleApiClient.disconnect();
                        }
                        stopLocationUpdates();
                    } catch (Exception e) {
                        Log.d("eeee", "googleApiClient.isConnected() Exception: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {

            Log.d("eeee", "onLocationChanged: Exception: " + e.getMessage());

        }

    }


    @Override
    public void onDestroy() {
        Log.i("MyLocation", "onDestroy");
        try {
            if (googleApiClient.isConnected()) {
                googleApiClient.disconnect();
            }
            stopLocationUpdates();
        } catch (Exception e) {
        }
        super.onDestroy();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(Bundle p0) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient,
                    locationRequest,
                    this
            );
        } catch (Exception e) {
        }
    }

    @Override
    public void onConnectionSuspended(int p0) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult p0) {
    }

    private void stopLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        } catch (Exception e) {
        }
    }


    private void turnOnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(EmployeeHomeActivity.this);
        builder.setMessage("Enable GPS to get Location").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void getEmployeeAttendanceLocationFromFirebase() {
        DatabaseReference databaseReference =
                FirebaseDatabase.getInstance().getReference("employee");
        databaseReference.child(auth.getUid().toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Employee model = snapshot.getValue(Employee.class);
                if (model != null) {
                    Log.d("MYTOKENNEW: ", " Called ");

                    strLocation = model.getEmployeeOfficeLoc();

                    String[] splited = strLocation.split(",");


                    Double dist = attendanceDistance(Double.parseDouble(splited[0].trim()), Double.parseDouble(splited[1].trim()), lati, longi, 'K');


                    Log.d("dddd", "Location Distance Diff:  " + dist);

                    if (dist >= 0.0 && dist <= 200.0) {
                        Toast.makeText(EmployeeHomeActivity.this, "Attendance Marked", Toast.LENGTH_SHORT).show();

                        try {


                            DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("employee").child(auth.getUid());

                            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
                            String dateString = formatter.format(new Date(System.currentTimeMillis()));

                            HashMap<String, Object> dateMap = new HashMap<>();
                            dateMap.put("employeeAttendance", dateString);


                            db.updateChildren(dateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        btnAttendance.setVisibility(View.INVISIBLE);
                                        txtAttendace.setVisibility(View.INVISIBLE);

                                        attendanceUtil.submitAttendancePrefrence(EmployeeHomeActivity.this, true);


                                    } else {
                                        Toast.makeText(EmployeeHomeActivity.this, "Some Error! Try Again", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });


                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(EmployeeHomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("LoadAds", error.getMessage());
                Log.d("shah", "Database Error: " + error.getMessage());
            }
        });
    }


    private double attendanceDistance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
//        if (unit == 'K') {
//            dist = dist * 1.609344;
//        }
//        else if (unit == 'N') {
//            dist = dist * 0.8684;
//        }
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    private void startAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        try {

//            AlarmManager.INTERVAL_HALF_DAY

            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 60000,
                    60000,
                    pendingIntent
            );
        } catch (Exception e) {
        }


    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        alarmManager.cancel(pendingIntent);
    }


    private void getAllBinsFromFirebase() {


        DatabaseReference databaseReference =
                FirebaseDatabase.getInstance().getReference("bin");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String strBinLoc = "";
                for (DataSnapshot snap : snapshot.getChildren()) {
                    strBinLoc = snap.getValue(String.class);

                    if (strBinLoc != null) {
                        Log.d("Looping : ", " Called ");

                        String[] splited = strBinLoc.split(",");
                        arrayListBin.add(new BinPoint(Double.parseDouble(splited[0]), Double.parseDouble(splited[1])));

                        /*Log.d("dddd", "onDataChange: Split: 0 " + splited[0]);
                        Log.d("dddd", "onDataChange: Split: 1 " + splited[1]);

                        Log.d("dddd", "Location lat 0 " + lati);
                        Log.d("dddd", "Location Long: 1 " + longi);*/

                    }

                }


                Bundle bundle = new Bundle();
                bundle.putSerializable("bins", (Serializable) arrayListBin);
                bundle.putSerializable("currentPoints", new BinPoint(lati, longi));
                Intent intent = new Intent(EmployeeHomeActivity.this, PlacesNearByActivity.class);
                intent.putExtras(bundle);


                locationProgress.dismiss();
                startActivity(intent);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Log.d("shah", "Database Error: " + error.getMessage());

                Toast.makeText(EmployeeHomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }


}
