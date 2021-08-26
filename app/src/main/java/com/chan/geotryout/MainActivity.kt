package com.chan.geotryout

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.chan.geotryout.provider.LocationUpdatesBroadcastReceiver
import com.chan.geotryout.util.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setLocationServicesClient()
        setGoogleMap()
    }

    //region Location related
    private fun setLocationServicesClient() {
        lastKnownLocation.setOnClickListener {
            showLastKnownLocation()
        }
        currentLocation.setOnClickListener {
            showCurrentLocation()
        }
        backgroundLocation.setOnClickListener {
            showBackgroundLocation()
        }
        stopUpdates.setOnClickListener {
            stopLocationUpdates()
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
        if (this.checkPermissions()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    location?.let {
                        locationLabel.text =
                            "Last Known\n\nLatitude : ${location.latitude}, Longitude: ${location.longitude}"
                        addMarkerToMap(location)
                    }
                }
                .addOnFailureListener { e ->
                    Log.d("ChanLog", "showLastKnownLocation:  Failed ${e.stackTraceToString()}")
                    turnOnDeviceLocation(e)
                }
        } else {
            this.requestLocationAccess()
        }
    }

    private fun setLocationRequest(onSuccess: (LocationRequest) -> Unit) {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (checkPermissions()) {
            val client: SettingsClient = LocationServices.getSettingsClient(this)
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
            client.checkLocationSettings(builder.build())
                .addOnSuccessListener { locationSettingsResponse ->
                    //locationSettingsResponse.locationSettingsStates.
                    //fusedLocationClient.requestLocationUpdates()
                    onSuccess(locationRequest)
                }
                .addOnFailureListener { e ->
                    Log.d("ChanLog", "showLastKnownLocation:  Failed ${e.stackTraceToString()}")
                    turnOnDeviceLocation(e)
                }
        } else {
            requestLocationAccess()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showCurrentLocation() {
        setLocationRequest { locationRequest ->
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun showBackgroundLocation() {
        setLocationRequest { locationRequest ->
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                getPendingIntent()
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        fusedLocationClient.removeLocationUpdates(getPendingIntent())
        Utils.removeNotification(applicationContext)
        toast("Location Updates Stopped!")
    }

    override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            requestLocationAccess()
        } else {
            Log.d("ChanLog", "Permission Granted Already")
        }
    }

    override fun onStop() {
        super.onStop()
        //stopLocationUpdates()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onPermissionActivityResult(requestCode, resultCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun getPendingIntent(): PendingIntent? {
        val intent = Intent(this, LocationUpdatesBroadcastReceiver::class.java)
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    //endregion

    //region Map related
    private fun setGoogleMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(mapReadyCallback)
    }

    private val mapReadyCallback = object: OnMapReadyCallback {
        override fun onMapReady(googleMap: GoogleMap) {
            mMap = googleMap

            // Add a marker in Sydney and move the camera
            addMarkerToMap(-34.0, 151.0, "Marker in Sydney")
        }
    }

    private fun addMarkerToMap(location: Location) {
        addMarkerToMap(location.latitude, location.longitude, getLocationAddress(location = location))
    }

    private val ZOOM_LEVEL = 13f
    private fun addMarkerToMap(latitude: Double, longitude: Double, title: String = "Location") {
        mMap?.clear()
        val latLng = LatLng(latitude, longitude)
        mMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
        )
        //mMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL))
    }

    private fun getLocationAddress(location: Location) = run { Geocoder(this).getFromLocation(location.latitude, location.longitude, 1)?.toString()?:"Location" }

    //endregion

}