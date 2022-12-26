package com.logoutbye.garbagemanagementapp.activities



import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessaging
import com.logoutbye.garbagemanagementapp.R
import org.json.JSONException
import org.json.JSONObject

class FcmGeneralSendActivity : AppCompatActivity() {

    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=" + "AAAA0cD5q9o:APA91bFeZhp7RlgaaE_lXywOCYfe5sQVPUgz0OzNdSQ4I_TeMGtF2Mk6i218h-qHyp6jY7GAtEZH72n_GD2QoMtf7gXrP8lH3Awmr1bO_sgNwhktmG220sAwMVsnCcb1ZXKm0rL_kEgD"
    private val contentType = "application/json"
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }

    var submit:Button?=null;
    var msg:EditText?=null;
    var imgBack:ImageView?=null;
    var txtTop:TextView?=null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fcm_general_send)

        msg=findViewById(R.id.msg)
        submit=findViewById(R.id.submit)


        imgBack = findViewById(R.id.imgBack)
        txtTop = findViewById(R.id.txtTop)


        imgBack!!.setOnClickListener(){
            onBackPressed()
        }





       FirebaseMessaging.getInstance().subscribeToTopic("/topics/general")


        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                return@addOnCompleteListener
            }
            val token = it.result //this is the token retrieved

            Log.i("TAG", "onResponse Device Token: "+token)

        }



        submit!!.setOnClickListener {
            if (!TextUtils.isEmpty(msg!!.text)) {
//                val topic = "/topics/Enter_topic" //topic has to match what the receiver subscribed to
                val topic = "/topics/general" //topic has to match what the receiver subscribed to
//                val topic = "com.logoutbye.garbagemanagementapp" //topic has to match what the receiver subscribed to

                val notification = JSONObject()
                val notifcationBody = JSONObject()

                try {
                    notifcationBody.put("title", "PakSaf App")
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

                Toast.makeText(this@FcmGeneralSendActivity, "message Sent", Toast.LENGTH_LONG)
                    .show()
            },
            Response.ErrorListener {
                Toast.makeText(this@FcmGeneralSendActivity, it.message.toString(), Toast.LENGTH_LONG).show()
                Toast.makeText(this@FcmGeneralSendActivity, "some error", Toast.LENGTH_LONG)
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
}