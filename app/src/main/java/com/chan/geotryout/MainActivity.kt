package com.chan.geotryout

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import com.chan.geotryout.provider.LocationUpdatesBroadcastReceiver
import com.chan.geotryout.util.LocationRequestHelper
import com.chan.geotryout.util.Utils
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mSettingsClient: SettingsClient? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private val REQUEST_CHECK_SETTINGS = 0x1
    private var mActivityRecognitionClient: ActivityRecognitionClient? = null
    private var mLocationCallback: LocationCallback? = null
    //private val mRequestUpdatesButton: Button? = null
    //private val mRemoveUpdatesButton: Button? = null
    //private val mLocationUpdatesResultView: TextView? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setLocationServicesClient()
    }

    private fun setLocationServicesClient() {
        lastKnownLocation.setOnClickListener {
            showLastKnownLocation()
        }
        currentLocation.setOnClickListener {
            setLocationRequest()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                var data = "Current Known\n\n"
                for (location in locationResult.locations){
                    // Update UI with location data
                    data+="Latitude : ${location.latitude}, Longitude: ${location.longitude}\n\n"
                }
                locationLabel.text = data
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun showLastKnownLocation() {
        if (checkPermissions()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                location?.let {
                    locationLabel.text = "Latitude : ${location.latitude}, Longitude: ${location.longitude}"
                }
            }
        } else {
            requestPermissions()
        }
    }

    private fun setLocationRequest() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            //locationSettingsResponse.locationSettingsStates.
            //fusedLocationClient.requestLocationUpdates()
            startLocationUpdates(locationRequest)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(locationRequest: LocationRequest) {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onStart() {
        super.onStart()
        /*PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)*/
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            updateTextField(this)
        }
        /*updateTextField(this)
        updateButtonsState(
            LocationRequestHelper.getInstance(this)
                .getBoolanValue("RequestingLocationUpdates", false)
        )*/
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (shouldProvideRationale) {
            Log.i(Companion.TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar(
                "permission_rationale", "ok"
            ) { startLocationPermissionRequest() }
        } else {
            Log.i(Companion.TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }

    private fun showSnackbar(
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

    private fun startLocationPermissionRequest() {
        requestPermissions(
            this@MainActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                RESULT_OK -> {
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
                    changeStatusAfterGetLastLocation("1", "Manual")
                }
                RESULT_CANCELED -> {
                    if (!checkPermissions()) {
                        requestPermissions()
                    }
                    toast("GPS is required to Start Tracking")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                Log.i(TAG, "User interaction was cancelled.")
                requestPermissions()
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

    //region Check And Remove Code
    private fun config() {
        /*mRequestUpdatesButton = (Button) findViewById(R.id.request_updates_button);
        mRemoveUpdatesButton = (Button) findViewById(R.id.remove_updates_button);
        mLocationUpdatesResultView = (TextView) findViewById(R.id.location_updates_result);*/

        mSettingsClient = LocationServices.getSettingsClient(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        createLocationRequest();
        buildLocationSettingsRequest();

        mActivityRecognitionClient = ActivityRecognitionClient(this);
    }

    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        mLocationSettingsRequest = builder.build()
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = Utils.UPDATE_INTERVAL
        mLocationRequest!!.fastestInterval = Utils.FASTEST_UPDATE_INTERVAL

        //mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(Utils.SMALLEST_DISPLACEMENT);
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest!!.priority = LocationRequest.PRIORITY_LOW_POWER
        mLocationRequest!!.maxWaitTime = Utils.MAX_WAIT_TIME
    }

    fun requestLocationUpdates() {
        if (!checkPermissions()) {
            toast("Please Allow Location Permission!")
            requestPermissions()
            return
        }
        try {
            mSettingsClient!!.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this) { //toast("All location settings are satisfied.");
                    changeStatusAfterGetLastLocation("1", "Manual")
                }
                .addOnFailureListener(this) { e ->
                    val statusCode = (e as ApiException).statusCode
                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            Log.i(
                                Companion.TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings "
                            )
                            try {
                                val rae = e as ResolvableApiException
                                rae.startResolutionForResult(
                                    this@MainActivity,
                                    REQUEST_CHECK_SETTINGS
                                )
                            } catch (sie: SendIntentException) {
                                Log.i(Companion.TAG, "PendingIntent unable to execute request.")
                            }
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            val errorMessage = "Location settings are inadequate, and cannot be " +
                                    "fixed here. Fix in Settings."
                            Log.e(Companion.TAG, errorMessage)
                            Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG)
                                .show()
                            Log.e(Companion.TAG, "DEVELOPER_ERROR")
                        }
                        LocationSettingsStatusCodes.DEVELOPER_ERROR -> Log.e(Companion.TAG, "DEVELOPER_ERROR")
                    }
                }
        } catch (e: SecurityException) {
            LocationRequestHelper.getInstance(applicationContext)
                ?.setValue("RequestingLocationUpdates", false)
            e.printStackTrace()
        }
    }

    fun removeLocationUpdates(view: View?) {
        changeStatusAfterGetLastLocation("0", "Manual")
    }

    fun updateTextField(context: Context) {
        /*mLocationUpdatesResultView.setText(
            LocationRequestHelper.getInstance(context).getStringValue("locationTextInApp", "")
        )*/
        Log.d("ChanLog", "updateTextField: ${LocationRequestHelper.getInstance(context)?.getStringValue("locationTextInApp", "")}")
    }

    @SuppressLint("MissingPermission")
    private fun changeStatusAfterGetLastLocation(value: String, changeby: String) {
        if (value === "1") {
            toast("Location Updates Started!")
            mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, getPendingIntent())
            LocationRequestHelper.getInstance(applicationContext)
                ?.setValue("RequestingLocationUpdates", true)
            val task: Task<Void> = mActivityRecognitionClient!!.requestActivityUpdates(
                Utils.UPDATE_INTERVAL,
                getPendingIntent()
            )
            task.addOnSuccessListener(OnSuccessListener<Void?> { })
            task.addOnFailureListener(OnFailureListener { e ->
                Log.i(
                    Companion.TAG,
                    "addOnFailureListener mActivityRecognitionClient $e"
                )
            })
        } else if (value === "0") {
            LocationRequestHelper.getInstance(applicationContext)
                ?.setValue("RequestingLocationUpdates", false)
            mFusedLocationClient?.removeLocationUpdates(getPendingIntent())
            Utils.removeNotification(applicationContext)
            toast("Location Updates Stopped!")
            val task: Task<Void> = mActivityRecognitionClient!!.removeActivityUpdates(
                getPendingIntent()
            )
            task.addOnSuccessListener(OnSuccessListener<Void?> { })
            task.addOnFailureListener(OnFailureListener { e ->
                Log.i(
                    Companion.TAG,
                    "removeActivityUpdates addOnFailureListener $e"
                )
            })
        }
        updateButtonsState(
            LocationRequestHelper.getInstance(this)
                ?.getBoolanValue("RequestingLocationUpdates", false)
        )
    }

    private fun getPendingIntent(): PendingIntent? {
        val intent = Intent(this, LocationUpdatesBroadcastReceiver::class.java)
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun updateButtonsState(requestingLocationUpdates: Boolean?) {
        /*if (requestingLocationUpdates) {
            mRequestUpdatesButton.setVisibility(View.GONE)
            mRemoveUpdatesButton.setVisibility(View.VISIBLE)
        } else {
            mRequestUpdatesButton.setVisibility(View.VISIBLE)
            mRemoveUpdatesButton.setVisibility(View.GONE)
        }*/
    }

    private fun toast(message: String) {

    }
    //endregion

    companion object {
        private const val TAG = "ChanLog"
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }

}