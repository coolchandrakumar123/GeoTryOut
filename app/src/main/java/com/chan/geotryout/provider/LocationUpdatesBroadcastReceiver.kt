package com.chan.geotryout.provider

import android.content.Intent

import android.content.BroadcastReceiver
import android.content.Context
import com.chan.geotryout.util.Utils
import java.text.DateFormat
import java.util.*


/**
 * Created by chandra-1765$ on 24/08/21$.
 */
class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_PROCESS_UPDATES == action) {
                Utils.setLocationUpdatesResult(
                    context,
                    DateFormat.getDateTimeInstance().format(Date())
                )
                context?.let {
                    Utils.getLocationUpdates(context, intent, "PROCESS_UPDATES")
                }
            }
        }
    }

    companion object {
        private const val TAG = "LUBroadcastReceiver"
        const val ACTION_PROCESS_UPDATES =
            "com.freakyjolly.demobackgroundlocation.action" + ".PROCESS_UPDATES"
    }
}