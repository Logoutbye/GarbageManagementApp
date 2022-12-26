package com.logoutbye.garbagemanagementapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.logoutbye.garbagemanagementapp.R
import com.logoutbye.garbagemanagementapp.databinding.ActivityLocationToRouteBinding
import com.logoutbye.garbagemanagementapp.utils.RoutPointClass
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class LocationToRouteActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var binding: ActivityLocationToRouteBinding

    private var mapbox: MapboxMap? = null
    private var originPoint: Point? = null
    private var destinationPoint: Point? = null
    private var mapBoxDirectionClient: MapboxDirections? = null
    private var mapBoxCurrentRoute: DirectionsRoute? = null
    val ROUTE_LAYER_ID = "ROUTE_LAYER_ID"
    val ROUTE_SOURCE_ID = "ROUTE_SOURCE_ID"
    val ICON_LAYER_ID = "ICON_LAYER_ID"
    val ICON_SOURCE_ID = "ICON_SOURCE_ID"
    val RED_PIN_ICON_ID = "RED_PIN_ICON_ID"

    var locationManager: LocationManager? = null
    var model: RoutPointClass? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.MAPBOX_TOKEN))
        binding = ActivityLocationToRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= 31) {
            binding.navigationCardView.visibility = View.INVISIBLE
        }


//        if (intent.hasExtra("model")) {
//            model = intent.getSerializableExtra("model") as RoutPointClass
////            originPoint = Point.fromLngLat(model!!.originLong, model!!.originLat)
//            originPoint = Point.fromLngLat(model!!.originLong, model!!.originLat)
//
//            Log.d("oooo", "origin points : "+originPoint!!.toJson().toString())
//            destinationPoint = Point.fromLngLat(model!!.destinationLong, model!!.destinationLat)
//
//            Log.d("oooo", "origin points : "+originPoint!!.toJson().toString())
//            Log.d("oooo", "destination points : "+destinationPoint!!.toJson().toString())
//
//
////            mesageFcm=model!!.description
//
//        }
//            originPoint = Point.fromLngLat(72.555919, 33.934608)
//            destinationPoint = Point.fromLngLat(72.552146, 33.941717)
//        modelPoint= RoutPointClass(33.934608,72.555919, model!!.destinationLat,model!!.destinationLong)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.onStart()
        binding.mapView.getMapAsync(this)

        binding.navigationCardView.setOnClickListener {
            if (model != null) {


                locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    turnOnGPS()
                } else {
                    val intent = Intent(this@LocationToRouteActivity, RouteToNavigationActivity::class.java)
                    intent.putExtra("model", model)
                    startActivity(intent)
                }
            }
        }
        binding.toolbaar.imgBack.setOnClickListener {
            onBackPressed()
        }

    }


    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapbox = mapboxMap
        //todo org


//        if (intent.hasExtra("model")) {
            model = intent.getSerializableExtra("model") as RoutPointClass
//            originPoint = Point.fromLngLat(model!!.originLong, model!!.originLat)
            originPoint = Point.fromLngLat(model!!.originLong, model!!.originLat)

            Log.d("oooo", "origin points : " + originPoint!!.toJson().toString())
            destinationPoint = Point.fromLngLat(model!!.destinationLong, model!!.destinationLat)

            Log.d("oooo", "origin points : " + originPoint!!.toJson().toString())
            Log.d("oooo", "destination points : " + destinationPoint!!.toJson().toString())


//            mesageFcm=model!!.description

//        }


        animateLiveEarthToBounds()
        mapboxMap.setStyle(Style.MAPBOX_STREETS, object : Style.OnStyleLoaded {
            @SuppressLint("MissingPermission")
            override fun onStyleLoaded(style: Style) {
                val locationComponent = mapboxMap.locationComponent
                locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(
                        this@LocationToRouteActivity,
                        style
                    ).build()
                )
                locationComponent.isLocationComponentEnabled = true
                locationComponent.renderMode = RenderMode.NORMAL


                initRouteSources(style)
                initRouteLayers(style)



                if (originPoint != null && destinationPoint != null) {
                    getDirectionalRoute(
                        mapboxMap,
                        originPoint!!,
                        destinationPoint!!
                    )
                }

            }

        })
    }

    private fun animateLiveEarthToBounds() {
        val latLngBounds = LatLngBounds.Builder()
            .include(LatLng(originPoint!!.latitude(), originPoint!!.longitude()))
            .include(LatLng(destinationPoint!!.latitude(), destinationPoint!!.longitude()))
            .build()
        try {
            mapbox!!.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, 50),
                5000
            )
        } catch (e: Exception) {
        }
    }

    private fun initRouteSources(style: Style) {
        try {
            style.addSource(
                GeoJsonSource(
                    ROUTE_SOURCE_ID,
                    FeatureCollection.fromFeatures(arrayOf())
                )
            )
            val iconGeoJsonSource = GeoJsonSource(
                ICON_SOURCE_ID, FeatureCollection.fromFeatures(
                    arrayOf(
                        Feature.fromGeometry(
                            Point.fromLngLat(
                                originPoint!!.longitude(),
                                originPoint!!.latitude()
                            )
                        ),
                        Feature.fromGeometry(
                            Point.fromLngLat(
                                destinationPoint!!.longitude(),
                                destinationPoint!!.latitude()
                            )
                        )
                    )
                )
            )
            style.addSource(iconGeoJsonSource)
        } catch (e: Exception) {
        }
    }

    private fun initRouteLayers(style: Style) {
        try {
            val routeLayer = LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)
            routeLayer.setProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineColor(Color.parseColor("#030007"))
            )
            style.addLayer(routeLayer)
            style.addImage(
                RED_PIN_ICON_ID,
                BitmapUtils.getBitmapFromDrawable(resources!!.getDrawable(R.drawable.ic_loc))!!
            )
            style.addLayer(
                SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID)
                    .withProperties(
                        PropertyFactory.iconImage(RED_PIN_ICON_ID),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconOffset(arrayOf(0f, -9f))
                    )
            )
        } catch (e: Exception) {
        }
    }


    private fun getDirectionalRoute(
        mapBoxMap: MapboxMap,
        originPoint: Point,
        destinationPoint: Point,
    ) {
        mapBoxDirectionClient = MapboxDirections.builder()
            .accessToken(getString(R.string.MAPBOX_TOKEN))
            .origin(originPoint)
            .destination(destinationPoint)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .build()
        mapBoxDirectionClient!!.enqueueCall(object : Callback<DirectionsResponse?> {
            override fun onFailure(call: Call<DirectionsResponse?>, t: Throwable) {
                Toast.makeText(
                    this@LocationToRouteActivity,
                    "Cannot Find Route!\nTry again later.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

            override fun onResponse(
                call: Call<DirectionsResponse?>,
                response: Response<DirectionsResponse?>,
            ) {
                if (response.body() == null) {
                    return
                } else if (response.body()!!.routes().size < 1) {
                    return
                }

                val distance =
                    DecimalFormat("#.##").format(response.body()!!.routes()[0].distance() / 1000)
                binding.distanceTxt.text = distance + " Km"

                binding.timeTxt.text = timeConverter(response.body()!!.routes()[0].duration())

                mapBoxCurrentRoute = response.body()!!.routes()[0]

                mapBoxMap.getStyle { style ->
                    val source =
                        style.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID)
                    if (source != null) {
                        source.setGeoJson(
                            FeatureCollection.fromFeature(
                                Feature.fromGeometry(
                                    LineString.fromPolyline(
                                        mapBoxCurrentRoute!!.geometry()!!,
                                        Constants.PRECISION_6
                                    )
                                )
                            )
                        )
                    } else {
                        Toast.makeText(
                            this@LocationToRouteActivity,
                            "Cannot find Route!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }

        })
    }

    fun timeConverter(biggy: Double): String {
        val hours = biggy.toInt() / 3600
        var remainder = biggy.toInt() - hours * 3600
        val mins = remainder / 60
        remainder = remainder - mins * 60
        val secs = remainder

        val builder = StringBuilder()
        builder.append(hours)
        builder.append("h ")
        builder.append(mins)
        builder.append("m ")
        builder.append(secs)
        builder.append("s")
        return builder.toString()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        binding.mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        binding.mapView.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        binding.mapView.onLowMemory()
        super.onLowMemory()
    }


    override fun onDestroy() {
        binding.mapView.onDestroy()
        super.onDestroy()
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
}