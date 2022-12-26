package com.logoutbye.garbagemanagementapp.activities;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.logoutbye.garbagemanagementapp.R;

public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private ProgressBar pb;
    private Button imgeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                , WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_splash);

        pb = findViewById(R.id.pb);
        imgeRefresh = findViewById(R.id.imgeRefresh);


        auth = FirebaseAuth.getInstance();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                gotoNextActivity();
            }
        }, 3000);
    }


    private void gotoNextActivity() {

        if (auth.getCurrentUser() != null) {

            String uid = auth.getUid();
            Task<DataSnapshot> taskMain = FirebaseDatabase.getInstance().getReference().child("user_details")
                    .child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {

                            if (task.isSuccessful()) {
//                                Log.d("uuuu", "onComplete: " + task.getResult().getValue());
                                Log.d("uuuu", "onComplete: Task Splash");
                                Object object = task.getResult().getValue();
                                String temp = "";
                                if (object != null) {
                                    temp = object.toString();

                                    if (temp.equalsIgnoreCase("admin")) {

                                        startActivity(new Intent(SplashActivity.this, AdminHomeActivity.class));
                                        finish();
                                    } else if (temp.equalsIgnoreCase("employee")) {

                                        startActivity(new Intent(SplashActivity.this, EmployeeHomeActivity.class));
                                        finish();
                                    } else if (temp.equalsIgnoreCase("general")) {

                                        startActivity(new Intent(SplashActivity.this, GeneralUserHomeActivity.class));
                                        finish();
                                    }


                                } else {


                                    Log.d("uuuu", "not onComplete: ");
                                    Toast.makeText(SplashActivity.this, "Some Error. Check Internet Or Try again", Toast.LENGTH_SHORT).show();


                                    imgeRefresh.setVisibility(View.VISIBLE);
                                    pb.setVisibility(View.INVISIBLE);
                                    Log.d("uuuu", "not onComplete: ");


                                    imgeRefresh.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            pb.setVisibility(View.VISIBLE);
                                            imgeRefresh.setVisibility(View.INVISIBLE);
                                            gotoNextActivity();
                                        }
                                    });
                                }


                            } else {
                                Log.d("uuuu", "not onComplete: ");
                                Toast.makeText(SplashActivity.this, "Some Error. Check Internet Or Try again", Toast.LENGTH_SHORT).show();


                                imgeRefresh.setVisibility(View.VISIBLE);
                                pb.setVisibility(View.INVISIBLE);
                                Log.d("uuuu", "not onComplete: ");


                                imgeRefresh.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        pb.setVisibility(View.VISIBLE);
                                        imgeRefresh.setVisibility(View.INVISIBLE);
                                        gotoNextActivity();
                                    }
                                });

                            }
                        }
                    });
        } else {

            Intent intent = new Intent(SplashActivity.this, MainLauncherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}