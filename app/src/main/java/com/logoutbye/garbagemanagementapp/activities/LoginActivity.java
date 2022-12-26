package com.logoutbye.garbagemanagementapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.live.earth.map.satellite.map.street.view.gps.navigation.utils.InternetAvailable;
import com.logoutbye.garbagemanagementapp.R;

public class LoginActivity extends AppCompatActivity {

    ProgressDialog loginProgress;
    TextView name;
    String email;
    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private TextView btnReset, headtitle;
    private Button btnLogin, btnSignup;

    private ImageView imgBack;
    DatabaseReference dbtoken;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userType = bundle.getString("type");

        }


        auth = FirebaseAuth.getInstance();
        loginProgress = ProgressDialog.show(this, null, "Please wait...", true);
        loginProgress.setCancelable(false);

        loginProgress.dismiss();
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        btnSignup = findViewById(R.id.btn_signup);
        btnLogin = findViewById(R.id.btn_login);
        btnReset = findViewById(R.id.btn_reset_password);
        headtitle = findViewById(R.id.headtitle);

        imgBack = findViewById(R.id.imgBack);



        if (userType.equalsIgnoreCase("admin")) {
            btnSignup.setEnabled(false);
            headtitle.setText("Admin Login");
        } else if (userType.equalsIgnoreCase("employee")) {
            btnSignup.setEnabled(false);
            headtitle.setText("Employee Login");
        } else {
            headtitle.setText("User Login/Signup");
        }


        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (InternetAvailable.Companion.isInternetAvailable(LoginActivity.this)) {

                    String email = inputEmail.getText().toString().trim();
                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(LoginActivity.this, "Please enter email to reset password", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "password reset email is sent to " + email, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {

                    Toast.makeText(LoginActivity.this, "Please check internet", Toast.LENGTH_SHORT).show();

                }
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //final EditText workspaceLink = findViewById(R.id.workspace_link);
                email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginProgress = ProgressDialog.show(LoginActivity.this, null, "Logging in...", true);
                loginProgress.setCancelable(false);

                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    if (password.length() < 6) {
                                        inputPassword.setError(getString(R.string.minimum_password));
                                        loginProgress.dismiss();
                                    } else {
                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                        loginProgress.dismiss();
                                    }
                                } else {

                                    //todo orrg
//                                    final String token = FirebaseInstanceId.getInstance().getToken();
                                    final Task<InstallationTokenResult> token = FirebaseInstallations.getInstance().getToken(true).addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<InstallationTokenResult> task) {


                                            dbtoken = FirebaseDatabase.getInstance().getReference().child("user_details").child(auth.getUid());
                                            dbtoken.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    String userType = dataSnapshot.getValue(String.class);

                                                    if (userType == null) {
                                                        Toast.makeText(LoginActivity.this, "Some Error Try Again", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }

                                                    Log.d("dddd", "onDataChange: userType " + userType);

                                                    try {
                                                        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(userType)
                                                                .child(auth.getUid()).child("token");
                                                        db.setValue(task.getResult().getToken()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {


                                                                if (task.isSuccessful()) {

                                                                    if (userType.equalsIgnoreCase("admin")) {
                                                                        startActivity(new Intent(LoginActivity.this, AdminHomeActivity.class));
                                                                        finish();
                                                                    } else if (userType.equalsIgnoreCase("general")) {
                                                                        startActivity(new Intent(LoginActivity.this, GeneralUserHomeActivity.class));
                                                                        finish();
                                                                    }
                                                                    if (userType.equalsIgnoreCase("employee")) {
                                                                        startActivity(new Intent(LoginActivity.this, EmployeeHomeActivity.class));
                                                                        finish();
                                                                    }
                                                                } else {

                                                                    loginProgress.dismiss();
                                                                    Toast.makeText(LoginActivity.this, "Server error or unstable network. Try Again", Toast.LENGTH_SHORT).show();
                                                                }


                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                loginProgress.dismiss();
                                                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });


                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        loginProgress.dismiss();
                                                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                                    }


                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    loginProgress.dismiss();
                                                    Toast.makeText(LoginActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                                                    Log.d("dddd", "onCancelled: databaseError:  " + databaseError.getMessage());


                                                }
                                            });
                                        }
                                    });


                                }//task else true


                            }
                        });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (loginProgress.isShowing()) {
            loginProgress.dismiss();
        }
    }

    public void signup(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        intent.putExtra("type", userType);
        startActivity(intent);
        finish();

    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
