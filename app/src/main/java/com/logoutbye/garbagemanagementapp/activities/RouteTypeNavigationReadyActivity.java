package com.logoutbye.garbagemanagementapp.activities;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.live.earth.map.gps.navigation.directions.streetview.speedometer.satellite.webcam.AppUtils.LiveEarthLocationAddressHelper;
import com.live.earth.map.satellite.map.street.view.gps.navigation.utils.InternetAvailable;
import com.logoutbye.garbagemanagementapp.R;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouteTypeNavigationReadyActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener {

    private static final String TAG = "RouteTypeNavigationActivity";
    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String ICON_LAYER_ID = "icon-layer-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";
    private static final String RED_PIN_ICON_ID = "red-pin-icon-id";


    private MapView mapView;
    private MapboxMap mapboxMap;
    private DirectionsRoute drivingRoute;
    private DirectionsRoute walkingRoute;
    private DirectionsRoute cyclingRoute;
    private DirectionsRoute currentRoute;
    private MapboxDirections client;
    private Point origin = Point.fromLngLat(0.0, 0.0);
    private Point destination = Point.fromLngLat(0.0, 0.0);
    private String lastSelectedDirectionsProfile = DirectionsCriteria.PROFILE_DRIVING;
    private boolean isdriving = false;
    private boolean iswalking = false;
    private boolean iscycling = false;
    private ImageView btnBike, btnCar, btnWalk;
    private FloatingActionButton btnNavigate;
    private ImageView imgBackArrow;
    private TextView txtTime, txtDistance;
    private boolean firstRouteDrawn = false;
    private final String[] profiles = new String[]{
            DirectionsCriteria.PROFILE_CYCLING,
            DirectionsCriteria.PROFILE_WALKING,
            DirectionsCriteria.PROFILE_DRIVING};


    private String txtDriveTime = "";
    private String txtWalkTime = "";
    private String txtBikeTime = "";


    private String txtDriveDist = "";
    private String txtWalkDist = "";
    private String txtBikeDist = "";
    private int selectedRoute = 0;
    private double distance = 0;
    private long timeSec = 0;
    //    private String placeName = "Navigation";
    private LiveEarthLocationAddressHelper liveEarthLocationAddressHelper;

    private ProgressDialog pd;
//    private Button btnKm, btnMile;
//    private MaterialButtonToggleGroup toggleGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.MAPBOX_TOKEN));
        setContentView(R.layout.activity_route_navigation);


        btnBike = findViewById(R.id.btnBike);
        btnCar = findViewById(R.id.btnCar);
        btnWalk = findViewById(R.id.btnWalk);
        imgBackArrow = findViewById(R.id.imgBackArrow);
        txtTime = findViewById(R.id.txtTime);
        txtDistance = findViewById(R.id.txtDistance);
        btnNavigate = findViewById(R.id.btnNavigate);
//        btnKm = findViewById(R.id.btnKm);
//        btnMile = findViewById(R.id.btnMile);
//        toggleGroup = findViewById(R.id.toggleGroup);


        pd = new ProgressDialog(RouteTypeNavigationReadyActivity.this);
        pd.setMessage("Wait for a second");


        imgBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();

        Log.d("iiii", "onCreate intent: " + intent.getExtras().get("start"));

        origin = (Point) intent.getExtras().get("start");
        destination = (Point) intent.getExtras().get("end");
//        placeName =  (String) intent.getExtras().get("place");

        liveEarthLocationAddressHelper = new LiveEarthLocationAddressHelper();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        RouteTypeNavigationReadyActivity.this.mapboxMap = mapboxMap;

                        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                .include(new LatLng(origin.latitude(), origin.longitude()))
                                .include(new LatLng(destination.latitude(), destination.longitude()))
                                .build();

                        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 10), 2000);

                        zoomOutForMarginWithDelay();


                        mapboxMap.getUiSettings().setAttributionEnabled(false);



                        initSource(style);

                        initLayers(style);

                        getAllRoutes(false);

                        initButtonClickListeners();

                        mapboxMap.addOnMapClickListener(RouteTypeNavigationReadyActivity.this);




                        btnNavigate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startMapBoxNavigationActivity();
                            }
                        });

                        btnCar.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.yellow));

                    }
                });
            }
        });

    }


    private void getAllRoutes(boolean fromMapClick) {
        for (String profile : profiles) {
            getSingleRoute(profile, fromMapClick);
        }
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        return false;
    }




    /**
     * Add the source for the Directions API route line LineLayer.
     */
    private void initSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID));
        GeoJsonSource iconGeoJsonSource = new GeoJsonSource(ICON_SOURCE_ID,
                Feature.fromGeometry(Point.fromLngLat(destination.longitude(),
                        destination.latitude())));
        loadedMapStyle.addSource(iconGeoJsonSource);
    }

    /**
     * Set up the click listeners on the buttons for each Directions API profile.
     */
    private void initButtonClickListeners() {
        btnCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isdriving) {

                    btnCar.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.yellow));
                    btnWalk.clearColorFilter();
                    btnBike.clearColorFilter();

                    lastSelectedDirectionsProfile = DirectionsCriteria.PROFILE_DRIVING;
                    txtTime.setText(txtDriveTime);
                    txtDistance.setText(txtDriveDist);
                    showRouteLine();
                } else {
                    Toast.makeText(RouteTypeNavigationReadyActivity.this, "Not Available", Toast.LENGTH_SHORT).show();
//                    txtTime.setText("Time:00 min");
//                    txtDistance.setText("Dist:00 km");
                }
            }
        });
        btnWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iswalking) {

                    btnCar.clearColorFilter();
                    btnWalk.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.yellow));
                    btnBike.clearColorFilter();


                    lastSelectedDirectionsProfile = DirectionsCriteria.PROFILE_WALKING;
                    txtTime.setText(txtWalkTime);
                    txtDistance.setText(txtWalkDist);
                    showRouteLine();
                } else {
                    Toast.makeText(RouteTypeNavigationReadyActivity.this, "Not Available", Toast.LENGTH_SHORT).show();
//                    txtTime.setText("Time:00 min");
//                    txtDistance.setText("Dist:00 km");
                }
            }
        });
        btnBike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iscycling) {


                    btnCar.clearColorFilter();
                    btnWalk.clearColorFilter();
                    btnBike.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.yellow));

                    lastSelectedDirectionsProfile = DirectionsCriteria.PROFILE_CYCLING;
                    txtTime.setText(txtBikeTime);
                    txtDistance.setText(txtBikeDist);
                    showRouteLine();
                } else {
                    Toast.makeText(RouteTypeNavigationReadyActivity.this, "Not Available", Toast.LENGTH_SHORT).show();
//                    txtTime.setText("Time:00 min");
//                    txtDistance.setText("Dist:00 km");
                }
            }
        });
    }

    /**
     * Display the Directions API route line depending on which profile was last
     * selected.
     */
    private void showRouteLine() {
        if (mapboxMap != null) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {

                    // Retrieve and update the source designated for showing the directions route
                    GeoJsonSource routeLineSource = style.getSourceAs(ROUTE_SOURCE_ID);

                    // Create a LineString with the directions route's geometry and
                    // reset the GeoJSON source for the route LineLayer source
                    if (routeLineSource != null) {
                        switch (lastSelectedDirectionsProfile) {

                            case DirectionsCriteria.PROFILE_DRIVING:
                                if (drivingRoute != null) {
                                    routeLineSource.setGeoJson(LineString.fromPolyline(drivingRoute.geometry(), PRECISION_6));
                                    selectedRoute = 1;
                                } else {

                                }

                                break;
                            case DirectionsCriteria.PROFILE_WALKING:

                                if (walkingRoute != null) {
                                    routeLineSource.setGeoJson(LineString.fromPolyline(walkingRoute.geometry(), PRECISION_6));
                                    selectedRoute = 2;
                                } else {

                                }

                                break;
                            case DirectionsCriteria.PROFILE_CYCLING:

                                if (cyclingRoute != null) {
                                    routeLineSource.setGeoJson(LineString.fromPolyline(cyclingRoute.geometry(), PRECISION_6));
                                    selectedRoute = 3;
                                } else {

                                }

                                break;
                            default:
                                break;
                        }
                    }
                }
            });
        }
    }

    /**
     * Add the route and icon layers to the map
     */
    private void initLayers(@NonNull Style loadedMapStyle) {
        LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);

// Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineColor(Color.parseColor("#006eff"))
        );
        loadedMapStyle.addLayer(routeLayer);

// Add the red marker icon image to the map
        loadedMapStyle.addImage(RED_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable
                (getResources().getDrawable(R.drawable.ic_bin)));

// Add the red marker icon SymbolLayer to the map
        loadedMapStyle.addLayer(new SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
                iconImage(RED_PIN_ICON_ID),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[]{0f, -9f})));
    }

    /**
     * Make a request to the Mapbox Directions API. Once successful, pass the route to the
     * route layer.
     *
     * @param profile the directions profile to use in the Directions API request
     */

    //todo route
    private void getSingleRoute(String profile, boolean fromMapClick) {
        client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .bannerInstructions(true)
                .voiceInstructions(true)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(profile)
                .accessToken(getString(R.string.MAPBOX_TOKEN))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(@NotNull Call<DirectionsResponse> call, @NotNull Response<DirectionsResponse> response) {

                if (response != null && response.isSuccessful()) {

                    if (response.body() == null) {
                        return;
                    } else if (response.body().routes().size() < 1) {
                        return;
                    }

                    switch (profile) {
                        case DirectionsCriteria.PROFILE_DRIVING:
                            drivingRoute = response.body().routes().get(0);

                            if (drivingRoute != null) {
                                try {
                                    selectedRoute = 1;
                                    isdriving = true;

                                    timeSec = drivingRoute.duration().longValue();
                                    distance = drivingRoute.distance();
                                    txtDriveTime = String.format("Time: %s" + " min", TimeUnit.SECONDS.toMinutes(drivingRoute.duration().longValue()));
                                    txtDriveDist = String.format("Dist: %s" + "Km", new DecimalFormat("###.##").format(drivingRoute.distance() / 1000.0));

                                    txtTime.setText(txtDriveTime);
                                    txtDistance.setText(txtDriveDist);

                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }
                            }

                            if (!firstRouteDrawn) {
                                showRouteLine();
                                firstRouteDrawn = true;
                            }

                            break;
                        case DirectionsCriteria.PROFILE_WALKING:

                            walkingRoute = response.body().routes().get(0);

                            if (walkingRoute != null) {
                                try {

                                    iswalking = true;
                                    timeSec = drivingRoute.duration().longValue();
                                    distance = drivingRoute.distance() / 1000.0;
                                    Log.d("nnnn", "walkingRoute: ready");

                                    txtWalkTime = String.format("Time: %s" + " min", String.valueOf(TimeUnit.SECONDS.toMinutes(walkingRoute.duration().longValue())));
                                    txtWalkDist = String.format("Dist: %s" + "Km", new DecimalFormat("###.##").format(walkingRoute.distance() / 1000.0));

                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }
                            }

                            break;
                        case DirectionsCriteria.PROFILE_CYCLING:
                            cyclingRoute = response.body().routes().get(0);

                            if (cyclingRoute != null) {

                                try {
                                    iscycling = true;
                                    timeSec = drivingRoute.duration().longValue();
                                    distance = drivingRoute.distance() / 1000.0;
                                    Log.d("nnnn", "cyclingRoute: ready");
                                    txtBikeTime = String.format("Time: %s" + " min", String.valueOf(TimeUnit.SECONDS.toMinutes(cyclingRoute.duration().longValue())));
                                    txtBikeDist = String.format("Dist: %s" + "Km", new DecimalFormat("###.##").format(cyclingRoute.distance() / 1000.0));
                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }

                            }

                            break;

                        default:
                            break;
                    }
                    if (fromMapClick) {
                        showRouteLine();
                    }

                } else {
//                    Toast.makeText(RouteTypeNavigationActivity.this, "Some Error Try Again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<DirectionsResponse> call, @NotNull Throwable throwable) {
                Log.e("Error", throwable.getMessage());
                Toast.makeText(RouteTypeNavigationReadyActivity.this,
                        "Error: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
// Cancel the Directions API request
        if (client != null) {
            client.cancelCall();
        }
        if (mapboxMap != null) {
            mapboxMap.removeOnMapClickListener(this);
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private int getCurrentMapZoom() {

        try {
            CameraPosition camPos = null;
            camPos = mapboxMap.getCameraPosition();
            return (int) camPos.zoom;
        } catch (Exception e) {
            e.printStackTrace();
            return 5;
        }
    }

    private void zoomOut() {
        int zoomValCurrent = getCurrentMapZoom();
        if (zoomValCurrent - 1 >= 0) {
            try {
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .zoom(zoomValCurrent - 0.06)
                        .build()), 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void zoomOutForMarginWithDelay() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                zoomOut();
            }
        }, 2100);
    }


    public void startMapBoxNavigationActivity() {


        if (InternetAvailable.Companion.isInternetAvailable(getApplicationContext())) {

            if (liveEarthLocationAddressHelper.isLocationOn(RouteTypeNavigationReadyActivity.this)) {

                if (origin != null && destination != null && isSelectedRouteAvailable(selectedRoute)) {


                    Log.d("ssss", "selected route num : " + selectedRoute);

//                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
//                                .directionsRoute(getSelectedRoute(selectedRoute))
//                                .shouldSimulateRoute(false)
//                                .build();
//                        NavigationLauncher.startNavigation(RouteTypeNavigationActivity.this, options);

                    //todo
//                    getThisRoute(origin, destination, lastSelectedDirectionsProfile);


                } else {
                    Toast.makeText(this, "Some Error: Try Again", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Turn On Location", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Internet is off", Toast.LENGTH_SHORT).show();
        }
    }


    private DirectionsRoute getSelectedRoute(int routeNum) {

        if (routeNum == 1) {
            Log.d("ssss", "getSelectedRoute: " + routeNum);
            return drivingRoute;

        } else if (routeNum == 2) {
            Log.d("ssss", "getSelectedRoute: " + routeNum);
            return walkingRoute;

        } else {
            Log.d("ssss", "getSelectedRoute else: " + routeNum);
            return cyclingRoute;
        }

    }


    private boolean isSelectedRouteAvailable(int routeNum) {

        if (routeNum == 1 && drivingRoute != null) {
            Log.d("ssss", "isSelectedRouteAvailable: drivingRoute" + routeNum);
            return true;

        } else if (routeNum == 2 && walkingRoute != null) {
            Log.d("ssss", "isSelectedRouteAvailable: walkingRoute" + routeNum);
            return true;

        } else if (routeNum == 3 && cyclingRoute != null) {
            Log.d("ssss", "isSelectedRouteAvailable: cyclingRoute" + routeNum);
            return true;
        } else {
            Log.d("ssss", "isSelectedRouteAvailable: NOOO");
            return false;
        }

    }





//    private void getThisRoute(Point origin, Point destination, String profile) {
//
//        try {
//            pd.show();
//            currentRoute = null;
//
//            NavigationRoute.builder(this)
//                    .accessToken(getString(R.string.MAPBOX_TOKEN))
//                    .origin(origin)
//                    .destination(destination)
//                    .profile(profile)
//                    .build()
//                    .getRoute(new Callback<DirectionsResponse>() {
//                        @Override
//                        public void onResponse(@NotNull Call<DirectionsResponse> call, @NotNull Response<DirectionsResponse> response) {
//
//                            if (response.body() == null) {
//
//                                pd.hide();
//                                return;
//                            } else if (response.body().routes().size() < 1) {
//                                pd.hide();
//                                return;
//                            }
//                            currentRoute = response.body().routes().get(0);
//
//                            if (liveEarthLocationAddressHelper.isLocationOn(RouteTypeNavigationReadyActivity.this)) {
//
//                                try {
//
//                                    if (currentRoute != null) {
//
//                                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
//                                                .directionsRoute(currentRoute)
//                                                .build();
//                                        NavigationLauncher.startNavigation(RouteTypeNavigationReadyActivity.this, options);
//                                        pd.hide();
//
//                                    } else {
//
//                                        pd.hide();
//                                        Toast.makeText(RouteTypeNavigationReadyActivity.this, "An error occurred, please try again.", Toast.LENGTH_SHORT).show();
//                                    }
//
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            } else {
//                                pd.hide();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(@NotNull Call<DirectionsResponse> call, @NotNull Throwable throwable) {
//                            Toast.makeText(getApplicationContext(), "Some error to find route. Try again", Toast.LENGTH_SHORT).show();
//                            pd.hide();
//                        }
//                    });
//        } catch (Exception e) {
//            e.printStackTrace();
//            pd.hide();
//        }
//    }

}