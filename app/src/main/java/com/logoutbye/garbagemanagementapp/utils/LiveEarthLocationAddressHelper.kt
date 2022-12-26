package com.live.earth.map.gps.navigation.directions.streetview.speedometer.satellite.webcam.AppUtils

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

class LiveEarthLocationAddressHelper {


    fun isLocationOn(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }
        return if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder(context)
                .setMessage("Location Service is Required")
                .setPositiveButton("Open Setting") { paramDialogInterface, paramInt ->
                    context.startActivity(
                        Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS
                        )
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
            false
        } else {
            true
        }
    }


}


