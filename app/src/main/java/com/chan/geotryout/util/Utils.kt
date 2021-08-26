package com.chan.geotryout.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.preference.PreferenceManager
import android.util.Log
import com.chan.geotryout.MainActivity
import com.chan.geotryout.R
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import android.app.NotificationChannel
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

import androidx.core.content.ContextCompat.getSystemService





/**
 * Created by chandra-1765$ on 24/08/21$.
 */
object Utils {
    private const val TAG = "ChanLog"
    const val KEY_LOCATION_UPDATES_RESULT = "location-update-result"
    var accuracy = 0f
    var addressFragments = ""
    var addresses: List<Address> = arrayListOf()
    const val UPDATE_INTERVAL = (5 * 1000).toLong()
    const val SMALLEST_DISPLACEMENT = 1.0f
    const val FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2
    const val MAX_WAIT_TIME = UPDATE_INTERVAL * 2
    fun setLocationUpdatesResult(context: Context?, value: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(KEY_LOCATION_UPDATES_RESULT, value)
            .apply()
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(context: Context, intent: Intent?, broadcastevent: String?) {
        val result = LocationResult.extractResult(intent)
        if (result != null) {
            val today: Date = Calendar.getInstance().getTime()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH.mm.ss")
            val nowDate: String = formatter.format(today)
            val locations: List<Location> = result.locations
            val firstLocation: Location = locations[0]
            getAddress(firstLocation, context)
            //firstLocation.getAccuracy();
            //firstLocation.getLatitude();
            //firstLocation.getLongitude();
            //firstLocation.getAccuracy();
            //firstLocation.getSpeed();
            //firstLocation.getBearing();
            LocationRequestHelper.getInstance(context)?.setValue(
                "locationTextInApp",
                "You are at " + getAddress(
                    firstLocation,
                    context
                ) + "(" + nowDate + ") with accuracy " + firstLocation.getAccuracy() + " Latitude:" + firstLocation.getLatitude() + " Longitude:" + firstLocation.getLongitude() + " Speed:" + firstLocation.getSpeed() + " Bearing:" + firstLocation.getBearing()
            )
            showNotificationOngoing(context, broadcastevent, "")
        }
    }

    fun getAddress(location: Location, context: Context?): String {
        val geocoder = Geocoder(context, Locale.getDefault())

        // Address found using the Geocoder.
        var address: Address? = null
        addressFragments = ""
        try {
            addresses = geocoder.getFromLocation(
                location.getLatitude(),
                location.getLongitude(),
                1
            )
            address = addresses.get(0)
        } catch (ioException: IOException) {
            Log.e(TAG, "error", ioException)
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(
                TAG, "Latitude = " + location.getLatitude().toString() +
                        ", Longitude = " + location.getLongitude(), illegalArgumentException
            )
        }
        if (addresses == null || addresses.size == 0) {
            Log.i(TAG, "ERORR")
            addressFragments = "NO ADDRESS FOUND"
        } else {
            for (i in 0..(address?.getMaxAddressLineIndex()?:0)) {
                addressFragments =
                    addressFragments + java.lang.String.valueOf(address?.getAddressLine(i))
            }
        }
        LocationRequestHelper.getInstance(context!!)?.setValue("addressFragments", addressFragments)
        return addressFragments
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun showNotificationOngoing(context: Context, content: String) {

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = notificationManager.getNotificationChannel(channelId)
        if(channel == null) {
            createNotificationChannel(context)
        }

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationBuilder: Notification.Builder = Notification.Builder(context, channelId)
            .setContentTitle(
                "LocationInfoFromNotification"
            )
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentIntent)
            .setStyle(Notification.BigTextStyle().bigText(content))
            .setAutoCancel(true)
        //.setOngoing(true)
        notificationManager.notify(3, notificationBuilder.build())
    }

    val channelId = "chanGeoNotification2"
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        val channelName: CharSequence = "GeoLocation"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(channelId, channelName, importance)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(false)
        notificationManager?.createNotificationChannel(notificationChannel)
    }

    fun showNotificationOngoing(context: Context, broadcastevent: String?, title: String) {
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationBuilder: Notification.Builder = Notification.Builder(context)
            .setContentTitle(
                title + DateFormat.getDateTimeInstance().format(Date()).toString() + ":" + accuracy
            )
            .setContentText(addressFragments)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setStyle(Notification.BigTextStyle().bigText(addressFragments))
            .setAutoCancel(true)
        notificationManager.notify(3, notificationBuilder.build())
    }

    fun removeNotification(context: Context) {
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}