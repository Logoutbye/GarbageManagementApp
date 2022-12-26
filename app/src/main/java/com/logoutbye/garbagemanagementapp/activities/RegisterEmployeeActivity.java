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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.live.earth.map.satellite.map.street.view.gps.navigation.utils.InternetAvailable;
import com.logoutbye.garbagemanagementapp.R;
import com.logoutbye.garbagemanagementapp.models.Employee;

public class RegisterEmployeeActivity extends AppCompatActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    public ProgressDialog dialog;
    ProgressDialog loginProgress;
    String username, email, password;
    private EditText inputEmail, inputPassword, Name, etLocation,etWorkLocation;
    private ImageView imgBack;
    private Button btnSignUp, btnGetLocation;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    String userType = "employee";
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    public final String TAG = "ffff";


    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    String strLocation = "";
    String strWorkLocation = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_register_employee);


        btnGetLocation = findViewById(R.id.btnGetLocation);
        etLocation = findViewById(R.id.etLocation);
        etWorkLocation = findViewById(R.id.etWorkLocation);
        btnSignUp = findViewById(R.id.sign_up_button);
        imgBack = findViewById(R.id.imgBack);


        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        Name = findViewById(R.id.name);

        btnGetLocation.setOnClickListener(new View.OnClickListener() {
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




        mAuth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(RegisterEmployeeActivity.this);
        dialog.setMessage("Creating your account...");
        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(!InternetAvailable.Companion.isInternetAvailable(RegisterEmployeeActivity.this)){
                    Toast.makeText(RegisterEmployeeActivity.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.show();
                email = inputEmail.getText().toString().trim();
                password = inputPassword.getText().toString().trim();
                username = Name.getText().toString().trim();
                strWorkLocation = etWorkLocation.getText().toString().trim();
                strLocation = etLocation.getText().toString().trim();


                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(getApplicationContext(), "Enter your Username!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(strLocation)) {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Enter Location Lat Long", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(strWorkLocation)) {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Enter Work Location Lat Long", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //create user    tv.setText(text); // tv is null

                try {
                    loginProgress = ProgressDialog.show(RegisterEmployeeActivity.this, null, "Please wait...", true);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                loginProgress.setCancelable(false);

                Log.d("Info", "Creating...");
                auth.createUserWithEmailAndPassword(email, password).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("dddd", "onFailure: " + e.getMessage());
                        loginProgress.dismiss();
                        dialog.dismiss();
                        Toast.makeText(RegisterEmployeeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnCompleteListener(RegisterEmployeeActivity.this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        Log.d("Info", "inside onComplete()");

                        if (!task.isSuccessful()) {

                            Log.d("Info", "inside task not successfull");
                            dialog.dismiss();
                            Toast.makeText(RegisterEmployeeActivity.this, "Try Again", Toast.LENGTH_SHORT).show();

                        } else {

                            String userID = auth.getUid();


                            if (userID == null) {
                                dialog.dismiss();
                                Toast.makeText(RegisterEmployeeActivity.this, "User ID failed. Try again after some time", Toast.LENGTH_SHORT).show();

                                return;
                            }


                            Log.d("Info", "Task successful..");
                            Log.d("Info", "Commit...");
                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("user_details");
                            //todo org
//                                    String token = FirebaseInstanceId.getInstance().getToken();
                            final Task<InstallationTokenResult> token = FirebaseInstallations.getInstance().getToken(true).addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstallationTokenResult> task) {


                                    mDatabase.child(userID).setValue(userType);

                                    if (userType.equalsIgnoreCase("employee")) {
                                        try {
                                            Employee employee = new Employee(username, email, 0, auth.getUid(), strLocation, strWorkLocation,
                                                    "no", 0, 0, 0, 0, task.getResult().getToken());

                                            DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("employee").child(auth.getUid());

                                            db.setValue(employee).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {

                                                        dialog.dismiss();
                                                        Toast.makeText(RegisterEmployeeActivity.this, "Employee Registered Successfully", Toast.LENGTH_SHORT).show();
                                                        finish();


                                                    } else {
                                                        Toast.makeText(RegisterEmployeeActivity.this, "Some Error! Try Again", Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            });


                                        } catch (Exception e) {
                                            e.printStackTrace();

                                        }
                                    }

//                                     if (userType.equalsIgnoreCase("employee")) {
//                                        dialog.dismiss();
//                                        Toast.makeText(RegisterEmployeeActivity.this, "Employee Registered Successfully", Toast.LENGTH_SHORT).show();
////                                        startActivity(new Intent(RegisterActivity.this, EmployeeHomeActivity.class));
//                                        finish();
//                                    }
//                            }//token else check

                                }

                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //tasl fail
                                    Toast.makeText(RegisterEmployeeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                    Log.d(TAG, "onFailure TAsk : " + e.getMessage());
                                }
                            });

                        }

                    }

                });

            }
        });
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //mAuth.addAuthStateListener(mAuthListener);
    }




    private void getingNewCurrentLocation() {
        try {

            locationRequest = new LocationRequest();


            locationRequest.setInterval(4000);
            locationRequest.setFastestInterval(2000);
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


                strLocation = location.getLatitude() + "," + location.getLongitude();
                etLocation.setText(strLocation);
                stopLocationUpdates();


                if (location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {

//                    findAddressLocation(location.getLatitude(), location.getLongitude());
                    try {
                        if (googleApiClient.isConnected()) {
                            googleApiClient.disconnect();
                        }
                        stopLocationUpdates();
                    } catch (Exception e) {
                        Toast.makeText(RegisterEmployeeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(RegisterEmployeeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }
//
//    private void findAddressLocation(Double latGPS, Double lngGPS) {
//
//        try {
//            Double lat = latGPS;
//            Double longi = lngGPS;
//            val task = GPSNavigationGeocoderAddressFinderAsynchTask(this, latLng, object :
//            GPSNavigationGeocoderAddressFinderAsynchTask.AddressAsynchCallback {
//                override fun onLocationFetched(addressFetched: String?) {
//                    AppConstants.myAddress = addressFetched
//                }
//                override fun onFetchingFailed() {
//                }
//            })
//            task.execute()
//        } catch (Exception e) {
//        }
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

