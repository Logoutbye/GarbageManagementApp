package com.logoutbye.garbagemanagementapp.activities;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.logoutbye.garbagemanagementapp.R;

public class AdminHomeActivity extends AppCompatActivity {
    private ImageView btnAddEmployee, btnSignOut, btnSendNotif, imgBack;
    private FirebaseAuth auth;
    private FirebaseUser userF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        btnAddEmployee = findViewById(R.id.btnAddEmployee);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnSendNotif = findViewById(R.id.btnSendNotif);
        imgBack = findViewById(R.id.imgBack);

        auth = FirebaseAuth.getInstance();


        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        final int[] checkedItem = {-1};
        btnSendNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // AlertDialog builder instance to build the alert dialog
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(AdminHomeActivity.this);

                // set the custom icon to the alert dialog
                alertDialog.setIcon(R.drawable.ic_launcher2);

                // title of the alert dialog
                alertDialog.setTitle("Select User Category");

                // list of the items to be displayed to
                // the user in the form of list
                // so that user can select the item from
                final String[] listItems = new String[]{"Employee", "General User"};

                // the function setSingleChoiceItems is the function which builds
                // the alert dialog with the single item selection
                alertDialog.setSingleChoiceItems(listItems, checkedItem[0], new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // update the selected item which is selected by the user
                        // so that it should be selected when user opens the dialog next time
                        // and pass the instance to setSingleChoiceItems method
                        checkedItem[0] = which;


                        if (which == 0) {
                            Intent intent = new Intent(AdminHomeActivity.this, FcmEmployeeSendActivity.class);
                            startActivity(intent);
                        } else if (which == 1) {
                            Intent intent = new Intent(AdminHomeActivity.this, FcmGeneralSendActivity.class);
                            startActivity(intent);
                        }
                        // now also update the TextView which previews the selected item
//                        tvSelectedItemPreview.setText("Selected Item is : " + listItems[which]);

                        // when selected an item the dialog should be closed with the dismiss method
                        dialog.dismiss();
                    }
                });

                // set the negative button if the user
                // is not interested to select or change
                // already selected item
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                // create and build the AlertDialog instance
                // with the AlertDialog builder instance
                AlertDialog customAlertDialog = alertDialog.create();

                // show the alert dialog when the button is clicked
                customAlertDialog.show();
            }
        });


        btnAddEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AdminHomeActivity.this, RegisterEmployeeActivity.class);
//                intent.putExtra("type", "employee");
                startActivity(intent);
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                auth.signOut();
                userF = FirebaseAuth.getInstance().getCurrentUser();
                if (userF == null) {
                    // user auth state is changed - user is null
                    // launch login activity

                    Intent intent = new Intent(AdminHomeActivity.this, MainLauncherActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}