package com.logoutbye.garbagemanagementapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.live.earth.map.satellite.map.street.view.gps.navigation.utils.InternetAvailable
import com.logoutbye.garbagemanagementapp.R
import com.logoutbye.garbagemanagementapp.databinding.ActivityNearByPlacesBinding
import com.logoutbye.garbagemanagementapp.utils.BinPoint
import com.logoutbye.garbagemanagementapp.utils.RoutPointClass

import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMap.InfoWindowAdapter
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import java.util.*


class PlacesNearByActivity : AppCompatActivity(), OnMapReadyCallback,
    Style.OnStyleLoaded {

    private lateinit var binding: ActivityNearByPlacesBinding

    private var mOriginPoint: Point? = null
    private var model: BinPoint? = null
    var arrayBins = mutableListOf<BinPoint>()
    private lateinit var _mapboxMap: MapboxMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        Mapbox.getInstance(this, getString(R.string.MAPBOX_TOKEN));

//        mOriginPoint = Point.fromLngLat(0.0, 0.0)

        binding = ActivityNearByPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)



//
//        if (intent.hasExtra("bins")) {
//            arrayBins = intent.getSerializableExtra("bins") as MutableList<BinPoint>
////            originPoint = Point.fromLngLat(model!!.originLong, model!!.originLat)
//            mOriginPoint = Point.fromLngLat(arrayBins[0].longi, arrayBins[0].lati)
////            mesageFcm=model!!.description
//
//        }





        binding.toolbaar.imgBack.setOnClickListener {
            onBackPressed()
        }



    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        Log.d("ssss", "onMapReady: ")
        this._mapboxMap = mapboxMap
        _mapboxMap.setStyle(Style.MAPBOX_STREETS, this)
    }

    override fun onStyleLoaded(style: Style) {
        Log.d("ssss", "onStyleLoaded: ")



        if (intent.hasExtra("bins")) {
            arrayBins = intent.getSerializableExtra("bins") as MutableList<BinPoint>
            model = intent.getSerializableExtra("currentPoints") as BinPoint
//            originPoint = Point.fromLngLat(model!!.originLong, model!!.originLat)
            mOriginPoint = Point.fromLngLat(arrayBins[0].longi, arrayBins[0].lati)
//            mesageFcm=model!!.description


            Log.d("pppp", "onStyleLoaded:  POints Origin: "+mOriginPoint)




            populateListBin()
            addMarker()



        }



    }





    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }


    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

//    override fun onResponse(response: Response<NearByMainModel?>?) {


    private fun populateListBin() {
        try {
            var index = 0
            for (binObject in arrayBins) {

                addNearByMarkerWithInfoWindow(
                    binObject.lati,
                    binObject.longi,
                "Go there"
                )

                if (index == 0) {
                    index++
                    setCameraFocusAtFirstMarker(
                        Point.fromLngLat(
                            binObject.lati,
                            binObject.longi
                        ),
                    )
                }
            }

        } catch (e: Exception) {
            Log.d("rrrr", "Exception: NearBy  api  " + e.message)

            e.printStackTrace()
        }
    }




    private fun addNearByMarkerWithInfoWindow(lati: Double, longi: Double, distance: String,
    ) {
        _mapboxMap.addMarker(
            MarkerOptions()
                .position(LatLng(lati, longi))
                .title(":$distance")
                .snippet(" Snipet")
        )


        _mapboxMap.setInfoWindowAdapter(InfoWindowAdapter { marker ->
            val v: View = layoutInflater.inflate(R.layout.custom_marker_window_venu, null)
            v.layoutParams = ViewGroup.LayoutParams(200, 180)

            val imgIconNearByInfoWindow = v.findViewById<ImageView>(R.id.imgIconNearByInfoWindow)
            val txtGo = v.findViewById<TextView>(R.id.txtGo)
            val txtTitleNearbyInfoWindow = v.findViewById<TextView>(R.id.txtTitleNearbyInfoWindow)
            val txtTitleNearbyDistanceInfoWindo = v.findViewById<TextView>(R.id.txtTitleNearbyDistanceInfoWindo)
            val strTitleMarker = marker.title
            val parts = strTitleMarker.split(":").toTypedArray()
            val partAddress = parts[0]
            val partDist = parts[1]
            txtTitleNearbyInfoWindow.text = partAddress
            txtTitleNearbyDistanceInfoWindo.text = partDist


            txtGo.setOnClickListener {
                if (InternetAvailable.isInternetAvailable(applicationContext)) {
                    if (marker.title != null) {
                        val position = marker.position
                        val placeLatLng = Point.fromLngLat(
                            position.longitude,
                            position.latitude
                        )


                        val intent = Intent(this@PlacesNearByActivity, LocationToRouteActivity::class.java)


                        val bundle=Bundle();

                        bundle.putSerializable("model",RoutPointClass(
                            model!!.lati,
                            model!!.longi,
                            position.latitude,
                            position.longitude,
                        "mesage"))

                        intent.putExtras(bundle)

                        startActivity(intent)

                    }
                } else {
                    Toast.makeText(applicationContext, "Internet is off", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            v
        })
    }


    fun setCameraFocusAtFirstMarker(startPoint: Point) {

        val position = CameraPosition.Builder()
            .target(LatLng(startPoint.latitude(), startPoint.longitude()))
            .zoom(10.0)
            .build()

        _mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000)
    }


    private fun addMarker() {




        val position = CameraPosition.Builder()
            .target(LatLng(mOriginPoint!!.latitude(), mOriginPoint!!.longitude()))
            .zoom(14.0)
            .build()

        _mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3500)
    }

}