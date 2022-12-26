package com.logoutbye.garbagemanagementapp.activities


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.live.earth.map.satellite.map.street.view.gps.navigation.utils.InternetAvailable
import com.logoutbye.garbagemanagementapp.R
import com.logoutbye.garbagemanagementapp.models.Employee
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class FcmTrashRequestSendActivity : AppCompatActivity(),
    com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    private var locationRequest: LocationRequest? = null
    private var googleApiClient: GoogleApiClient? = null
    private val REQUEST_LOCATION = 1
    private var locationManager: LocationManager? = null
    private var locationProgress: ProgressDialog? = null
    var arrayEmployee = mutableListOf<Employee>()
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"

    private val serverKey =
        "key=" + "AAAA0cD5q9o:APA91bFeZhp7RlgaaE_lXywOCYfe5sQVPUgz0OzNdSQ4I_TeMGtF2Mk6i218h-qHyp6jY7GAtEZH72n_GD2QoMtf7gXrP8lH3Awmr1bO_sgNwhktmG220sAwMVsnCcb1ZXKm0rL_kEgD"
    private val contentType = "application/json"
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }

    private var submit: Button? = null;
    private var btnGetLocationAndSubmit: Button? = null;
    private var msg: EditText? = null;
    private var etLoc: EditText? = null;
    private var imgBack: ImageView? = null;
    private var txtTop: TextView? = null;


    private var strAuth: String? = null;
    private var lati: Double = 0.0;
    private var longi: Double = 0.0;
    private var strLocation: String? = null;
    private var  employeeID : String? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fcm_trash_request_send)

        val bundle = intent.extras
        if (bundle != null) {
            strAuth = bundle.getString("auth")
        }



        msg = findViewById(R.id.msg)
        etLoc = findViewById(R.id.etLoc)
        btnGetLocationAndSubmit = findViewById(R.id.btnGetLocationAndSubmit)



        imgBack = findViewById(R.id.imgBack)
        txtTop = findViewById(R.id.txtTop)


        imgBack!!.setOnClickListener() {
            onBackPressed()
        }
        txtTop!!.setText("Trash Request")



        FirebaseMessaging.getInstance().subscribeToTopic("/topics/general")


        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                return@addOnCompleteListener
            }
            val token = it.result //this is the token retrieved

            Log.i("TAG", "onResponse Device Token: " + token)

        }



        btnGetLocationAndSubmit!!.setOnClickListener() {

            if (InternetAvailable.isInternetAvailable(this@FcmTrashRequestSendActivity)) {


                locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    turnOnGPS()
                } else {

                    Toast.makeText(this@FcmTrashRequestSendActivity,
                        "Wait for a moment ",
                        Toast.LENGTH_SHORT)
                        .show()
                    btnGetLocationAndSubmit!!.setEnabled(false)
                    btnGetLocationAndSubmit!!.postDelayed(Runnable { btnGetLocationAndSubmit!!.setEnabled(true) }, 8000)

                    getingNewCurrentLocation()
                }

            } else {

                Toast.makeText(this@FcmTrashRequestSendActivity,
                    "Please check your internet",
                    Toast.LENGTH_SHORT).show();
            }

        }


    }


    override fun onBackPressed() {
        super.onBackPressed()
    }


    private fun sendNotification(notification: JSONObject) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> { response ->


                Toast.makeText(this@FcmTrashRequestSendActivity,
                    response.toString(),
                    Toast.LENGTH_LONG).show()

                Log.i("TAG", "onResponse: $response")
                msg!!.setText("")

                locationProgress!!.dismiss()
            },
            Response.ErrorListener {


                Toast.makeText(this@FcmTrashRequestSendActivity,
                    it.message.toString(),
                    Toast.LENGTH_LONG).show()
                Log.i("TAG", "onErrorResponse: Didn't work: " + it.message)

                locationProgress!!.dismiss()

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


    /////////////////////Location////////////////////////////


    private fun getingNewCurrentLocation() {
        try {

            locationProgress = ProgressDialog.show(this@FcmTrashRequestSendActivity, null, "Creating Trash Request to nearby worker. Please wait...", true)

            locationRequest = LocationRequest()
            locationRequest!!.setInterval(4000)
            locationRequest!!.setFastestInterval(2000)
            locationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            googleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
            googleApiClient!!.connect()
        } catch (e: Exception) {
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onLocationChanged(location: Location) {
        try {
            if (location.latitude != 0.0 && location.longitude != 0.0) {


                lati = location.latitude
                longi = location.longitude
                strLocation = lati.toString() + "," + longi.toString()

                etLoc!!.setText(strLocation);

                if (location.latitude != 0.0 && location.longitude != 0.0) {
                    findAddressLocation(location.latitude, location.longitude)

                    try {
                        if (googleApiClient!!.isConnected) {
                            googleApiClient!!.disconnect()
                        }
                        stopLocationUpdates()
                    } catch (e: Exception) {
                    }


                    getAllEmployee()


                }

            }
        } catch (e: Exception) {
        }
    }

    private fun findAddressLocation(latGPS: Double, lngGPS: Double) {}

    override fun onDestroy() {
        Log.i("MyLocation", "onDestroy")
        try {
            if (googleApiClient!!.isConnected) {
                googleApiClient!!.disconnect()
            }
            stopLocationUpdates()
        } catch (e: Exception) {
        }


        super.onDestroy()
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient!!,
                locationRequest!!,
                this
            )
        } catch (e: Exception) {
        }
    }

    override fun onConnectionSuspended(p0: Int) {}
    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }

    private fun stopLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient!!, this)
        } catch (e: Exception) {
        }
    }


    private fun turnOnGPS() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Enable GPS to get Location").setCancelable(false)
            .setPositiveButton("Yes"
            ) { dialog, which -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton("No"
            ) { dialog, which -> dialog.cancel() }
        val alertDialog = builder.create()
        alertDialog.show()
    }



    private fun employeeDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        unit: Char,
    ): Double {
        val theta = lon1 - lon2
        var dist =
            Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(
                deg2rad(lat2)) * Math.cos(deg2rad(theta))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60 * 1.1515
        //        if (unit == 'K') {
//            dist = dist * 1.609344;
//        }
//        else if (unit == 'N') {
//            dist = dist * 0.8684;
//        }
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }


    private fun getAllEmployee() {

        arrayEmployee = mutableListOf<Employee>();


        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("employee")
        var emp: Employee

        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (ds in dataSnapshot.children) {
                    emp = Employee();
                    val name = ds.child("name").getValue(String::class.java)
                    val employeeWorkLoc = ds.child("employeeWorkLoc").getValue(String::class.java)

//                    Log.d("fbbb", "name: " + name!!)
//                    Log.d("fbbb", "employeeWorkLoc:" + employeeWorkLoc)


                    val splited = employeeWorkLoc!!.split(",").toTypedArray()

//                    Log.d("dddd", "onDataChange: Split: 0 " + splited[0])
//                    Log.d("dddd", "onDataChange: Split: 1 " + splited[1])
//                    Log.d("dddd", "Location lat 0 $lati")
//                    Log.d("dddd", "Location Long: 1 $longi")


                    val dist = employeeDistance(splited[0].toDouble(),
                        splited[1].toDouble(),
                        lati,
                        longi,
                        'K')


//                    Log.d("dddd", "Location Distance Diff:  $dist")


                    emp.employeeID = ds.child("employeeID").getValue(String::class.java)
                    emp.employeeWorkLoc = ds.child("employeeWorkLoc").getValue(String::class.java)
                    emp.employeeEcoCash = dist
                    arrayEmployee.add(emp)

                }


                    sendTrashRequestToNearestEmployee(arrayEmployee)

            }

            override fun onCancelled(databaseError: DatabaseError) {

                Toast.makeText(this@FcmTrashRequestSendActivity,
                    databaseError.message,
                    Toast.LENGTH_SHORT).show()
            }
        }
        usersdRef.addListenerForSingleValueEvent(eventListener)
    }


    fun sendTrashRequestToNearestEmployee(arrayEmployee: MutableList<Employee> = mutableListOf<Employee>()) {


        val sortedEmpList = arrayEmployee.sortedWith(compareBy({ it.employeePoints }));

         employeeID = sortedEmpList.get(0).employeeID


        Log.d("dddd", "sendTrashRequestToNearestEmployee: Employeee ID: "+employeeID)
        //todo
//        FirebaseMessaging.getInstance().subscribeToTopic("/topics/" +employeeID)


        createTrashFcmToNearby(employeeID!!)


    }


    fun createTrashFcmToNearby(strUserId: String) {
        if (!TextUtils.isEmpty(etLoc!!.text)) {

            val topic = "/topics/" + strUserId //topic has to match what the receiver subscribed to

            val notification = JSONObject()
            val notifcationBody = JSONObject()



            try {
                notifcationBody.put("title", "Trash Pic Request")
                notifcationBody.put("message", msg!!.text)
                notifcationBody.put("action", "trash")
                notifcationBody.put("lati", lati)
                notifcationBody.put("longi", longi)
                notification.put("to", topic)
                notification.put("data", notifcationBody)
                Log.e("TAG", "try")
            } catch (e: JSONException) {
                Log.e("TAG", "onCreate: " + e.message)

                Toast.makeText(this@FcmTrashRequestSendActivity, e.message, Toast.LENGTH_SHORT).show()
            }

            sendNotification(notification)
        }
    }





}