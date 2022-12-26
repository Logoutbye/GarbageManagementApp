package com.logoutbye.garbagemanagementapp.dialogue;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.logoutbye.garbagemanagementapp.R;


public class ComplainDialogue extends Dialog implements View.OnClickListener {

    public Activity c;
    public Dialog d;
    public TextView txtCancel,txtSubmit;
    public EditText etComplain;



    public ComplainDialogue(Activity a) {
        super(a);
        this.c = a;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_complain);

        txtCancel = findViewById(R.id.txtCancel);
        etComplain = findViewById(R.id.etComplain);
        txtSubmit = findViewById(R.id.txtSubmit);


        txtCancel.setOnClickListener(this);
        txtSubmit.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtSubmit:




                    String strTo = "paksaf.employee@gmail.com";
                    String strSubject= "EMPLOYEE COMPLAIN PAKSAF";
                    String strBody="Complain: "+"\n"+ etComplain.getText().toString();
                    String strMailTo = "mailto:" + strTo +
                            "?&subject=" + Uri.encode(strSubject) +
                            "&body=" + Uri.encode(strBody);
                    Intent emailIntent = new Intent(Intent.ACTION_VIEW);
                    emailIntent.setData(Uri.parse(strMailTo));
                    c.startActivity(emailIntent);

                break;


            case R.id.txtCancel:

                    dismiss();

                break;


            default:
                break;
        }

    }
}