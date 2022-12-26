package com.logoutbye.garbagemanagementapp.dialogue;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.logoutbye.garbagemanagementapp.R;

//import com.example.mapnavigationseatlitefinder.AdsPack.MyApplication;
//import com.example.mapnavigationseatlitefinder.R;
//import com.google.android.gms.location.FusedLocationProviderClient;

public class FeedBackDialogue extends Dialog implements View.OnClickListener {

    public Activity c;
    public Dialog d;
    public TextView txtSubmit, txtAskLat;
    public EditText etFeedBack;
    RatingBar ratingBar;
    float ratingVal = 4.5f;
    boolean fromBackBtn = true;

    public FeedBackDialogue(Activity a, boolean fromBackBtn) {
        super(a);
        this.c = a;
        this.fromBackBtn = fromBackBtn;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_feedback);
        txtSubmit = findViewById(R.id.txtSubmit);
        txtAskLat = findViewById(R.id.txtAskLat);
        ratingBar = findViewById(R.id.ratingBar);
        etFeedBack = findViewById(R.id.etFeedBack);


        txtSubmit.setOnClickListener(this);
        txtAskLat.setOnClickListener(this);
        txtSubmit.setOnClickListener(this);


//        if(fromBackBtn){
//            txtAskLat.setText("Close App");
//        }

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                ratingVal = rating;

            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtSubmit:




                    String strTo = "paksaf.app@gmail.com";
                    String strSubject= "SUGGESTION & FEEDBACK PAKSAF APP";
                    String strBody="Rating Star: "+ratingVal+"\n"+"Suggestion: "+etFeedBack.getText().toString();
                    String strMailTo = "mailto:" + strTo +
                            "?&subject=" + Uri.encode(strSubject) +
                            "&body=" + Uri.encode(strBody);
                    Intent emailIntent = new Intent(Intent.ACTION_VIEW);
                    emailIntent.setData(Uri.parse(strMailTo));
                    c.startActivity(emailIntent);



                break;
            case R.id.txtAskLat:


                    dismiss();


                break;


            default:
                break;
        }

    }
}