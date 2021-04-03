package com.milen.bluetoothapp.services

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionSolverService {

    fun isLocationPermissionGranted(activity: Activity): Boolean {
        return when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            true -> checkLollipopPermissions(activity)
            else -> true
        }
    }


    fun requestPermissions(
        activity: Activity,
        permissions: Array<String>
    ) {
        ActivityCompat.requestPermissions(
            activity,
            permissions,
            1
        )
    }

    private fun checkLollipopPermissions(activity: Activity): Boolean {

        val permissionCoarseLocation = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val permissionFineLocation = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        return permissionCoarseLocation == PackageManager.PERMISSION_GRANTED &&
                permissionFineLocation == PackageManager.PERMISSION_GRANTED
    }

}