package com.live.earth.map.satellite.map.street.view.gps.navigation.utils

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.logoutbye.garbagemanagementapp.R


class InternetAvailable {
    companion object{
        fun isInternetAvailable(mContext: Context): Boolean {
            val connectivityManager =
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            var activeNetworkInfo: NetworkInfo? = null
            try {
                activeNetworkInfo = connectivityManager.activeNetworkInfo
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }


        fun showInternetConnectionDialog(context: Context) {
            try {
                val alertDialog = AlertDialog.Builder(context)
                alertDialog.setTitle("No Internet !!!")
                alertDialog.setIcon(R.drawable.ic_launcher2)
                alertDialog.setMessage("Kindly enable your internet connection for better experience.")
                alertDialog.setPositiveButton("Ok") { _, _ ->
                }

                alertDialog.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                alertDialog.show()
            } catch (e: Exception) {
            }
        }
    }
}