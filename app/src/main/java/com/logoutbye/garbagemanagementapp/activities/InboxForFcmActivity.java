package com.logoutbye.garbagemanagementapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.logoutbye.garbagemanagementapp.R;
import com.logoutbye.garbagemanagementapp.utils.RoutPointClass;

public class InboxForFcmActivity extends AppCompatActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    LocationManager locationManager;
    private String strLocation = "";
//    private String strWorkLocation = "";
    private double lati = 0.0;
    private double longi = 0.0;

    private Button btnGetLocationAndProceed;
    private  ImageView imgBack;
    private RoutPointClass routPointInbox;
    private TextView msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_for_fcm);

        imgBack=findViewById(R.id.imgBack);

        msg=findViewById(R.id.msg);

        routPointInbox = (RoutPointClass) getIntent().getExtras().getSerializable("model");

        msg.setText(routPointInbox.getStringMessage());



        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        btnGetLocationAndProceed = findViewById(R.id.btnGetLocationAndProceed);


        btnGetLocationAndProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    turnOnGPS();
                } else {
                    getingNewCurrentLocation();
                }
            }
        });
    }


    private void getingNewCurrentLocation() {
        try {

            locationRequest = new LocationRequest();


            locationRequest.setInterval(5000);
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


                lati = location.getLatitude();
                longi = location.getLongitude();

                stopLocationUpdates();


                if (location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {

                    try {
                        if (googleApiClient.isConnected()) {
                            googleApiClient.disconnect();
                        }


                        stopLocationUpdates();


                        Bundle bundle=new Bundle();

                        Intent intent = new Intent(InboxForFcmActivity.this, LocationToRouteActivity.class);
                        RoutPointClass pointModel = new RoutPointClass(
                                lati,
                                longi,
                                routPointInbox.getDestinationLat(),
                                routPointInbox.getDestinationLong(),
                                routPointInbox.getStringMessage());

                        bundle.putSerializable("model",pointModel);

                        intent.putExtras(bundle);
                        startActivity(intent);




                    } catch (Exception e) {
                        Toast.makeText(InboxForFcmActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(InboxForFcmActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
