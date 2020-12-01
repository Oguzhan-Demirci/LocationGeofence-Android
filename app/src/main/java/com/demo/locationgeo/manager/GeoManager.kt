package com.demo.locationgeo.manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.demo.locationgeo.data.Location
import com.demo.locationgeo.receiver.GeofenceBroadcastReceiver
import com.huawei.hms.location.Geofence
import com.huawei.hms.location.GeofenceRequest
import com.huawei.hms.location.GeofenceService
import com.huawei.hms.location.LocationServices


class GeoManager(private val context: Context) {

    companion object {
        private const val TAG = "GEO_GeoManager"

        private const val GEOFENCE_ID = "com.demo.locationgeo.GEOFENCE"
        private const val GEOFENCE_VALID_TIME = 10000L
    }

    private val mContext = context.applicationContext
    private var mGeofenceService: GeofenceService? = null
    private val mIdList = ArrayList<String>()
    private val mGeofenceList = ArrayList<Geofence>()
    private var mPendingIntent: PendingIntent? = null

    init {
        init()
    }

    private fun init() {
        Log.d(TAG, "init()")
        mGeofenceService = LocationServices.getGeofenceService(context)
        mPendingIntent = getPendingIntent()
    }

    private fun getPendingIntent(): PendingIntent? {
        val intent = Intent(mContext, GeofenceBroadcastReceiver::class.java)
        intent.action = GeofenceBroadcastReceiver.ACTION_PROCESS_LOCATION
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     *  Creates a geofence instance and adds to the list.
     */
    fun addGeofenceToList(latitude: Double, longitude: Double, radius: Float) {
        if (mGeofenceList.isEmpty()) {
            val geofence = Geofence.Builder().let {
                it.setUniqueId(GEOFENCE_ID)
                it.setValidContinueTime(GEOFENCE_VALID_TIME)
                it.setRoundArea(latitude, longitude, radius)
                it.setConversions(Geofence.ENTER_GEOFENCE_CONVERSION.or(Geofence.EXIT_GEOFENCE_CONVERSION))
                it.build()
            }
            mGeofenceList.add(geofence)
            mIdList.add(GEOFENCE_ID)

            requestGeofenceWithNewIntent()
        } else {
            Log.d(TAG, "addGeofenceToList --> Geofence already registered.")
        }
    }

    /**
     *  Sends the request to add a geofence.
     */
    private fun getGeofenceAddingRequest(): GeofenceRequest = GeofenceRequest.Builder().let {
        it.setInitConversions(Geofence.ENTER_GEOFENCE_CONVERSION)
        it.createGeofenceList(mGeofenceList)
        it.build()
    }

    /**
     *  Creates a request for adding a geofence.
     */
    private fun requestGeofenceWithNewIntent() {
        mGeofenceService?.createGeofenceList(getGeofenceAddingRequest(), getPendingIntent())
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "requestGeofenceWithNewIntent --> Geofence added successfully.")
                } else {
                    Log.w(
                        TAG,
                        "requestGeofenceWithNewIntent --> Couldn't add geofence. Error: ",
                        it.exception
                    )
                }
            }
    }

    /**
     *  Removes a geofence based on its ID, and processes the response to the geofence deletion
     *  request.
     */
    fun removeGeofenceWithID() {

        mGeofenceService?.deleteGeofenceList(mIdList)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "removeGeofenceWithID --> Geofence deleted with ID successfully.")
                    mGeofenceList.removeLast()
                    mIdList.removeLast()
                } else {
                    Log.w(TAG, "removeGeofenceWithID -- > Deleting geofence with ID failed. ")
                }
            }
    }

}