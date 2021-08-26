package com.chan.geotryout.provider

import android.content.Intent

import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import com.chan.geotryout.util.Utils
import com.chan.geotryout.util.Utils.showNotificationOngoing
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import java.text.DateFormat
import java.util.*


/**
 * Created by chandra-1765$ on 24/08/21$.
 */
class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            /*if (ACTION_PROCESS_UPDATES == action) {
                Utils.setLocationUpdatesResult(
                    context,
                    DateFormat.getDateTimeInstance().format(Date())
                )
                context?.let {
                    Utils.getLocationUpdates(context, intent, "PROCESS_UPDATES")
                }
            }*/
            if (intent.action == ACTION_PROCESS_UPDATES) {

                // Checks for location availability changes.
                LocationAvailability.extractLocationAvailability(intent)?.let { locationAvailability ->
                    if (!locationAvailability.isLocationAvailable) {
                        Log.d("ChanLog", "Location services are no longer available!")
                    }
                }

                var address: String = "LocationInfo\n\n"
                LocationResult.extractResult(intent)?.let { locationResult ->
                    val locations = locationResult.locations.map { location ->
                        /*MyLocationEntity(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            foreground = isAppInForeground(context),
                            date = Date(location.time)
                        )*/
                        address += "${location.latitude}, ${location.longitude}\n\n"
                    }
                    /*if (locations.isNotEmpty()) {
                        LocationRepository.getInstance(context, Executors.newSingleThreadExecutor())
                            .addLocations(locations)
                    }*/
                }
                Log.d("ChanLog", "Location Receiver: ${address} ")
                context?.let {
                    showNotificationOngoing(it, address)
                }
            }
        }
    }

    companion object {
        const val ACTION_PROCESS_UPDATES =
            "com.chan.geotryout.provider.action" + ".PROCESS_UPDATES"
    }
}