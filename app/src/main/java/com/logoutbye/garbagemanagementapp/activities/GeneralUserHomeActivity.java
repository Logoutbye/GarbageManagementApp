package com.logoutbye.garbagemanagementapp.activities;


import android.annotation.SuppressLint;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.live.earth.map.satellite.map.street.view.gps.navigation.utils.InternetAvailable;
import com.logoutbye.garbagemanagementapp.R;
import com.logoutbye.garbagemanagementapp.dialogue.FeedBackDialogue;
import com.logoutbye.garbagemanagementapp.utils.BinPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GeneralUserHomeActivity extends AppCompatActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;

    private String strLocation = "";

    private double lati, longi;
    private List<BinPoint> arrayListBin;
    private ProgressDialog locationProgress;

    private FirebaseAuth auth;
    private FirebaseUser userF;
    private ImageView btnSignOut, btnTrashRequest,btnNearByBin,btnFeedBack,imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_user_home);
        auth = FirebaseAuth.getInstance();

        btnTrashRequest = findViewById(R.id.btnTrashRequest);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnNearByBin = findViewById(R.id.btnNearByBin);
        btnFeedBack = findViewById(R.id.btnFeedBack);
        imgBack = findViewById(R.id.imgBack);


        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnFeedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    FeedBackDialogue feedBackDialogue= new FeedBackDialogue(GeneralUserHomeActivity.this,true);
                    feedBackDialogue.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        FirebaseMessaging.getInstance().subscribeToTopic("/topics/general");
        btnTrashRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GeneralUserHomeActivity.this, FcmTrashRequestSendActivity.class);
                intent.putExtra("auth", auth.getUid());
                startActivity(intent);


            }
        });


        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (InternetAvailable.Companion.isInternetAvailable(GeneralUserHomeActivity.this)) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/general");
                    auth.signOut();
                    userF = FirebaseAuth.getInstance().getCurrentUser();
                    if (userF == null) {
                        // user auth state is changed - user is null
                        // launch login activity

                        Intent intent = new Intent(GeneralUserHomeActivity.this, MainLauncherActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {

                    Toast.makeText(GeneralUserHomeActivity.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                }
            }
        });




        btnNearByBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (InternetAvailable.Companion.isInternetAvailable(GeneralUserHomeActivity.this)) {



                    try {





                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            turnOnGPS();
                        } else {
                            locationProgress =
                                    ProgressDialog.show(GeneralUserHomeActivity.this, null,
                                            "Getting  Bins Location Please wait...",
                                            true);

                            arrayListBin = new ArrayList<BinPoint>();

                            getingNewCurrentLocation();
                        }





                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {
                    Toast.makeText(GeneralUserHomeActivity.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                }

            }

        });







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

            Log.d("eeee", "getingNewCurrentLocation: EXCEPTION:  "+e.getMessage());
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

//                Log.d("dddd", "Location lat 0" + lati);
//                Log.d("dddd", "Location Long: 1 " + longi);


                if (location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {


                        getAllBinsFromFirebase();


                    try {
                        if (googleApiClient.isConnected()) {
                            googleApiClient.disconnect();
                        }
                        stopLocationUpdates();
                    } catch (Exception e) {
                        Log.d("eeee", "googleApiClient.isConnected() Exception: "+e.getMessage());
                    }
                }
            }
        } catch (Exception e) {

            Log.d("eeee", "onLocationChanged: Exception: "+e.getMessage());

        }

    }

//    private void findAddressLocation(Double latGPS, Double lngGPS) {
//
//
//    }

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
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

                        Log.d("dddd", "onDataChange: Split: 0 " + splited[0]);
                        Log.d("dddd", "onDataChange: Split: 1 " + splited[1]);
                        Log.d("dddd", "Location lat 0 " + lati);
                        Log.d("dddd", "Location Long: 1 " + longi);

                    }

                }


                Bundle bundle = new Bundle();
                bundle.putSerializable("bins", (Serializable) arrayListBin);
                bundle.putSerializable("currentPoints", new BinPoint(lati, longi));
                Intent intent = new Intent(GeneralUserHomeActivity.this, PlacesNearByActivity.class);
                intent.putExtras(bundle);


                locationProgress.dismiss();
                startActivity(intent);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("LoadAds", error.getMessage());
                Log.d("shah", "Database Error: " + error.getMessage());

                Toast.makeText(GeneralUserHomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }
}