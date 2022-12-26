package com.logoutbye.garbagemanagementapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.logoutbye.garbagemanagementapp.R;
import com.logoutbye.garbagemanagementapp.models.User;

public class RegisterActivity extends AppCompatActivity {

    public ProgressDialog dialog;
    ProgressDialog loginProgress;
    String username, email, password;
    private EditText inputEmail, inputPassword, Name;
    private TextView btnSignIn;
    private Button btnSignUp;
    private ImageView imgBack;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    String userType;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    public final String TAG = "ffff";





    private static final int REQUEST_LOCATION = 1;
    TextView showLocation;
    LocationManager locationManager;
    String latitude, longitude;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_register);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userType = bundle.getString("type");

            }

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        Name = findViewById(R.id.name);
        btnSignUp = findViewById(R.id.signUpButton);
        imgBack = findViewById(R.id.imgBack);


        mAuth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(RegisterActivity.this);
        dialog.setMessage("Creating your account...");
        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


imgBack.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        onBackPressed();
    }
});

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                email = inputEmail.getText().toString().trim();
                password = inputPassword.getText().toString().trim();
                username = Name.getText().toString().trim();

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
                    loginProgress = ProgressDialog.show(RegisterActivity.this, null, "Please wait...", true);
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
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        Log.d("Info", "inside onComplete()");

                        if (!task.isSuccessful()) {

                            Log.d("Info", "inside task not successfull");
                            dialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Try Again", Toast.LENGTH_SHORT).show();

                        } else {

                            String userID = auth.getUid();


                            if (userID == null) {
                                dialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "User ID failed. Try again after some time", Toast.LENGTH_SHORT).show();

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

                                    if (userType.equalsIgnoreCase("general")) {
                                        try {

                                            String tempToken = task.getResult().getToken();


                                            Log.d(TAG, "onComplete: tempToken: " + tempToken);
                                            User user = new User(username, email, 0, "no", "no", "no",
                                                    "no", 0, 0, 0, tempToken, 0);
                                            DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("general").child(userID);
                                            db.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "onComplete: ");
                                                        Toast.makeText(RegisterActivity.this, "User Registered Successfully", Toast.LENGTH_SHORT).show();


                                                    } else {
                                                        Toast.makeText(RegisterActivity.this, "User Registration Error! Try Again", Toast.LENGTH_SHORT).show();
                                                    }

                                                }

                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(RegisterActivity.this, "User Registeration Fail! Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                                }
                                            });


                                        } catch (Exception e) {
                                            e.printStackTrace();

                                            Toast.makeText(RegisterActivity.this, "!!" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    }


                                    if (userType.equalsIgnoreCase("general")) {
                                        dialog.dismiss();
                                        startActivity(new Intent(RegisterActivity.this, GeneralUserHomeActivity.class));
                                        finish();
                                    } else if (userType.equalsIgnoreCase("admin")) {
                                        dialog.dismiss();
                                        startActivity(new Intent(RegisterActivity.this, AdminHomeActivity.class));
                                        finish();
                                    }

//
                                }

                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //tasl fail
                                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

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

//    public void signin(View view) {
//
//        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
//        startActivity(i);
//        finish();
//
//    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

