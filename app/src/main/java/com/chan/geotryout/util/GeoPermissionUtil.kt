package com.chan.geotryout.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.chan.geotryout.BuildConfig
import com.chan.geotryout.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.material.snackbar.Snackbar

/**
 * Created by chandra-1765$ on 26/08/21$.
 */


private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
val REQUEST_CHECK_SETTINGS = 0x1
fun AppCompatActivity.checkPermissions(): Boolean {
    val permissionState = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    return permissionState == PackageManager.PERMISSION_GRANTED
}

fun AppCompatActivity.requestLocationAccess() {
    Log.d("ChanLog", "requestLocationAccess: Please Allow")
    val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (shouldProvideRationale) {
        Log.i("ChanLog", "Displaying permission rationale to provide additional context.")
        showSnackbar(
            "permission_rationale", "ok"
        ) { startLocationPermissionRequest() }
    } else {
        Log.i("ChanLog", "Requesting permission")
        startLocationPermissionRequest()
    }
}

fun AppCompatActivity.showSnackbar(
    mainTextString: String, actionString: String,
    listener: View.OnClickListener
) {
    Snackbar.make(
        findViewById(R.id.lastKnownLocation),
        mainTextString,
        Snackbar.LENGTH_INDEFINITE
    )
        .setAction(actionString, listener).show()
}

private fun AppCompatActivity.startLocationPermissionRequest() {
    ActivityCompat.requestPermissions(
        this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
        REQUEST_PERMISSIONS_REQUEST_CODE
    )
}

fun AppCompatActivity.turnOnDeviceLocation(e: Exception) {
    when ((e as ApiException).statusCode) {
        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
            Log.i(
                "ChanLog", "Location settings are not satisfied. Attempting to upgrade " +
                        "location settings "
            )
            try {
                val rae = e as ResolvableApiException
                rae.startResolutionForResult(
                    this,
                    REQUEST_CHECK_SETTINGS
                )
            } catch (sie: IntentSender.SendIntentException) {
                Log.i("ChanLog", "PendingIntent unable to execute request.")
            }
        }
        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
            val errorMessage = "Location settings are inadequate, and cannot be " +
                    "fixed here. Fix in Settings."
            Log.e("ChanLog", errorMessage)
            toast(errorMessage)
        }
        LocationSettingsStatusCodes.DEVELOPER_ERROR -> {
            Log.e("ChanLog", "DEVELOPER_ERROR")
            toast("DEVELOPER_ERROR")
        }
    }
}

fun AppCompatActivity.onPermissionActivityResult(requestCode: Int, resultCode: Int) {
    when (requestCode) {
        REQUEST_CHECK_SETTINGS -> when (resultCode) {
            AppCompatActivity.RESULT_OK -> {
                toast("GPS turned on")
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    toast("Please Provide Location Permission.")
                    return
                }
                toast("Permission Granted")
            }
            AppCompatActivity.RESULT_CANCELED -> {
                if (!checkPermissions()) {
                    requestLocationAccess()
                }
                toast("GPS is required to Start Tracking")
            }
        }
    }
}

fun AppCompatActivity.onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
    Log.i("ChanLog", "onRequestPermissionResult")
    if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
        if (grantResults.size <= 0) {
            Log.i("ChanLog", "User interaction was cancelled.")
            requestLocationAccess()
        } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
            // Permission denied.
            showSnackbar("permission_denied_explanation", "settings") {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri: Uri = Uri.fromParts(
                    "package",
                    BuildConfig.APPLICATION_ID, null
                )
                intent.data = uri
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}