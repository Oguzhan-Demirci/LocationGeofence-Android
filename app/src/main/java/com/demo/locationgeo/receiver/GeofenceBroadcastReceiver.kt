package com.demo.locationgeo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.demo.locationgeo.manager.NotificationManager
import com.huawei.hms.location.GeofenceData
import com.huawei.hms.location.GeofenceRequest


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GEO_GeofenceReceiver"
        const val ACTION_PROCESS_LOCATION =
            "com.demo.locationgeo.receiver.GeofenceBroadcastReceiver.ACTION_PROCESS_LOCATION"
    }

    private lateinit var mNotificationManager: NotificationManager

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent != null) {
            val sb = StringBuilder()

            if (ACTION_PROCESS_LOCATION == intent.action) {

                val geofenceData = GeofenceData.getDataFromIntent(intent)

                if (geofenceData != null) {

                    val mLocation = geofenceData.convertingLocation

                    if (geofenceData.errorCode != -1) {
                        sb.append("ErrorCode: ${geofenceData.errorCode}\n")
                    }

                    when (geofenceData.conversion) {
                        GeofenceRequest.ENTER_INIT_CONVERSION -> {
                            sb.append("Conversion: ENTER_INIT_CONVERSION\n")
                        }
                        GeofenceRequest.EXIT_INIT_CONVERSION -> {
                            sb.append("Conversion: EXIT_INIT_CONVERSION\n")
                        }
                        GeofenceRequest.DWELL_INIT_CONVERSION -> {
                            sb.append("Conversion: DWELL_INIT_CONVERSION\n")
                        }
                        else -> {
                            sb.append("Conversion: ${geofenceData.conversion}\n")
                        }
                    }

                    geofenceData.convertingGeofenceList.forEach { sb.append("${it.uniqueId}\n") }
                    sb.append("Location: ${mLocation.longitude}, ${mLocation.longitude}\n")
                    sb.append("Success Status: ${geofenceData.isSuccess}\n")

                    context?.let {
                        mNotificationManager = NotificationManager(it)
                        mNotificationManager.notify("Geofence Event", sb.toString())
                    }

                    Log.d(TAG, "onReceive --> $sb")
                } else {
                    Log.w(TAG, "onReceive --> geofenceData is null")
                }

            }

            Log.d(TAG, "onReceive --> ${intent.action}")

        } else {
            Log.w(TAG, "onReceive --> Incoming intent is null.")
        }
    }
}