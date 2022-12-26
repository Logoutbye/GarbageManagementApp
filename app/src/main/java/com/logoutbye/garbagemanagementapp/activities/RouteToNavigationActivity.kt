package com.logoutbye.garbagemanagementapp.activities

//import com.liveearth.map.webcams.navigation.weather.R
//import com.liveearth.map.webcams.navigation.weather.ads.LiveEarthLoadAds
//import com.liveearth.map.webcams.navigation.weather.databinding.ActivityRouteToNavigationBinding
//import com.liveearth.map.webcams.navigation.weather.helper.Helper
//import com.liveearth.map.webcams.navigation.weather.model.RoutPointClass
//import com.liveearth.map.webcams.navigation.weather.preference.MySharedPreference
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.logoutbye.garbagemanagementapp.R
import com.logoutbye.garbagemanagementapp.databinding.ActivityRouteToNavigationBinding
import com.logoutbye.garbagemanagementapp.utils.RoutPointClass
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.navigation.base.internal.route.RouteUrl
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback
import com.mapbox.navigation.ui.NavigationViewOptions
import com.mapbox.navigation.ui.OnNavigationReadyCallback
import com.mapbox.navigation.ui.listeners.NavigationListener
import com.mapbox.navigation.ui.map.NavigationMapboxMap

class RouteToNavigationActivity : AppCompatActivity(), OnNavigationReadyCallback,
    NavigationListener {
    lateinit var binding: ActivityRouteToNavigationBinding
    val TAG = "RouteToNavigation"
    var isRouteDrawn = false

    private lateinit var navigationMapboxMap: NavigationMapboxMap
    private lateinit var mapboxNavigation: MapboxNavigation
    private var origin: Point? = null
    private var destination: Point? = null
    private val INITIAL_ZOOM = 18

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.MAPBOX_TOKEN))
        binding = ActivityRouteToNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setCusTheme()


//        if (intent.hasExtra("model")) {


            val model = intent.getSerializableExtra("model") as RoutPointClass
            origin = Point.fromLngLat(model.originLong, model.originLat)
            destination = Point.fromLngLat(model.destinationLong, model.destinationLat)

            val initialPosition =
                CameraPosition.Builder()
                    .target(
                        LatLng(
                            origin!!.latitude(),
                            origin!!.longitude()
                        )
                    )
                    .zoom(INITIAL_ZOOM.toDouble())
                    .build()
            binding.navigationView.onCreate(savedInstanceState)
            binding.navigationView.initialize(this, initialPosition)
            val feedbackFab = findViewById<View>(R.id.feedbackFab)
            feedbackFab.visibility = View.GONE
//        }

    }



    override fun onNavigationReady(isRunning: Boolean) {
        Log.d(TAG, "onNavigationReady: $isRunning")
        if (!isRunning && !::navigationMapboxMap.isInitialized
            && !::mapboxNavigation.isInitialized
        ) {
            if (binding.navigationView.retrieveNavigationMapboxMap() != null
            ) {
                Log.d(TAG, "onNavigationReady: Both are not null")
                navigationMapboxMap = binding.navigationView.retrieveNavigationMapboxMap()!!
                val navigationOptions = MapboxNavigation
                    .defaultNavigationOptionsBuilder(this, getString(R.string.MAPBOX_TOKEN))
                    .build()

                    mapboxNavigation = MapboxNavigationProvider.create(navigationOptions)
                    fetchRoute()

            } else {
                Log.d(TAG, "onNavigationReady:  Both are null")
            }
        }
    }

    private fun fetchRoute() {
        Log.d(TAG, "fetchRoute: ")
        if (origin != null && destination != null) {
            mapboxNavigation.requestRoutes(
                RouteOptions.builder()
                    .accessToken(getString(R.string.MAPBOX_TOKEN))
                    .coordinates(listOf(origin, destination))
                    .geometries(RouteUrl.GEOMETRY_POLYLINE6)
                    .profile(RouteUrl.PROFILE_DRIVING)
                    .user(RouteUrl.PROFILE_DEFAULT_USER)
                    .baseUrl(RouteUrl.BASE_URL)
                    .requestUuid("1")
                    .build(), object : RoutesRequestCallback {
                    override fun onRoutesReady(routes: List<DirectionsRoute>) {
                        Log.d(TAG, "onRoutesReady: ")
                        if (routes.isNotEmpty()) {
                            val directionsRoute = routes[0]
                            startNavigation(directionsRoute)
                            isRouteDrawn = true
                        } else {
                            isRouteDrawn = true
                        }

                    }

                    override fun onRoutesRequestCanceled(routeOptions: RouteOptions) {
                        Log.d(TAG, "onRoutesRequestCanceled: ")
                        isRouteDrawn = true
                    }

                    override fun onRoutesRequestFailure(
                        throwable: Throwable,
                        routeOptions: RouteOptions,
                    ) {
                        Log.d(TAG, "onRoutesRequestFailure: ")
                        isRouteDrawn = true
                    }
                }
            )
        }
    }

    private fun startNavigation(directionsRoute: DirectionsRoute) {
        Log.d(TAG, "startNavigation: ")
        val optionsBuilder = NavigationViewOptions.builder(this)
        optionsBuilder.navigationListener(this)
        optionsBuilder.directionsRoute(directionsRoute)
        optionsBuilder.shouldSimulateRoute(false)
        binding.navigationView.startNavigation(optionsBuilder.build())

    }

    override fun onCancelNavigation() {
        binding.navigationView.stopNavigation()
        finish()
    }

    override fun onNavigationFinished() {
        finish()
    }

    override fun onNavigationRunning() {
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.navigationView.onLowMemory()
    }

    override fun onStart() {
        super.onStart()
        binding.navigationView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.navigationView.onResume()
    }

    override fun onStop() {
        binding.navigationView.onStop()
        super.onStop()
    }

    override fun onPause() {
        binding.navigationView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.navigationView.onDestroy()
        try {
            if (::mapboxNavigation.isInitialized) {
                mapboxNavigation.onDestroy()
            }
        } catch (e: Exception) {
        }
        super.onDestroy()
    }



    override fun onBackPressed() {
        if (isRouteDrawn) {
            finish()
        } else {
            Toast.makeText(
                this@RouteToNavigationActivity,
                "Route is loading wait...",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}