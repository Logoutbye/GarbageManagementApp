package com.logoutbye.garbagemanagementapp.activities


import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessaging
import com.logoutbye.garbagemanagementapp.R
import org.json.JSONException
import org.json.JSONObject

class FcmEmployeeSendActivity : AppCompatActivity() {

    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
        "key=" + "AAAA0cD5q9o:APA91bFeZhp7RlgaaE_lXywOCYfe5sQVPUgz0OzNdSQ4I_TeMGtF2Mk6i218h-qHyp6jY7GAtEZH72n_GD2QoMtf7gXrP8lH3Awmr1bO_sgNwhktmG220sAwMVsnCcb1ZXKm0rL_kEgD"
    private val contentType = "application/json"
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }

    var submit: Button? = null;
    var imgBack: ImageView? = null;
    var msg: EditText? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fcm_employee_send)

        msg = findViewById(R.id.msg)
        submit = findViewById(R.id.submit)
        imgBack = findViewById(R.id.imgBack)

//        FirebaseMessaging.getInstance().subscribeToTopic("/topics/employee")


        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                return@addOnCompleteListener
            }
            val token = it.result //this is the token retrieved


        }


        imgBack!!.setOnClickListener() {
            onBackPressed()
        }



        submit!!.setOnClickListener {
            if (!TextUtils.isEmpty(msg!!.text)) {
                val topic = "/topics/employee" //topic has to match what the receiver subscribed to
                val notification = JSONObject()
                val notifcationBody = JSONObject()

                try {
                    notifcationBody.put("title", "PakSaf Employee Notification")
                    notifcationBody.put("message", msg!!.text)
                    notification.put("to", topic)
                    notification.put("data", notifcationBody)
                    Log.e("TAG", "try")
                } catch (e: JSONException) {
                    Log.e("TAG", "onCreate: " + e.message)
                }

                sendNotification(notification)
            }
        }
    }


    private fun sendNotification(notification: JSONObject) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> { response ->
                Toast.makeText(this@FcmEmployeeSendActivity, "message Sent", Toast.LENGTH_LONG)
                    .show()

                Log.i("TAG", "onResponse: $response")

            },
            Response.ErrorListener {
                Toast.makeText(this@FcmEmployeeSendActivity,
                    it.message.toString(),
                    Toast.LENGTH_LONG).show()
                Log.i("TAG", "onErrorResponse: Didn't work: " + it.message)
                Toast.makeText(this@FcmEmployeeSendActivity, "some error", Toast.LENGTH_LONG)
                    .show()
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }


    override fun onBackPressed() {
        super.onBackPressed()
    }
}