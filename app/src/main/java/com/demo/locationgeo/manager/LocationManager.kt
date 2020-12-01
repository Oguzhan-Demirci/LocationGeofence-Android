package com.demo.locationgeo.manager

import android.content.Context
import android.content.IntentSender.SendIntentException
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.demo.locationgeo.data.listener.ResultListener
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*


class LocationManager(context: Context) {

    companion object {
        private const val TAG = "GEO_LocationManager"
    }

    private val mContext = context.applicationContext
    private val mFusedLocationProviderClient = FusedLocationProviderClient(mContext)
    private val mLocationRequest = LocationRequest().also {
        it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private val mSettingsClient = LocationServices.getSettingsClient(mContext)
    private var mLocationListener: ResultListener<Location>? = null

    fun getLocation(activity: FragmentActivity, listener: ResultListener<Location>) {
        checkLocationSettings(activity, listener)
    }

    /**
     *  Checks the location settings of the device. Defines its callbacks.
     *  Initiates location requests when location settings meet requirements.
     *  Else calls startResolutionForResult to display a pop-up asking the user to enable related
     *  permission.
     */
    private fun checkLocationSettings(activity: FragmentActivity, listener: ResultListener<Location>) {
        Log.d(TAG, "checkLocationSettings()")

        val locationSettingsRequestBuilder = LocationSettingsRequest.Builder()
        locationSettingsRequestBuilder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = locationSettingsRequestBuilder.build()

        mSettingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                Log.d(TAG, "checkLocationSettings --> Device location settings are OK.")
                mFusedLocationProviderClient
                    .requestLocationUpdates(
                        mLocationRequest,
                        mLocationCallback,
                        Looper.getMainLooper()
                    )
                    .addOnSuccessListener {
                        Log.d(TAG, "checkLocationSettings --> Requested location updates successfully.")
                        mLocationListener = listener
                    }.addOnFailureListener {
                        Log.w(TAG, "checkLocationSettings --> A problem occurred while requesting location updates: ", it)
                        listener.onFailure("A problem occurred while requesting location updates", it)
                    }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "checkLocationSettings --> Checking device location settings. Failure: ", e)
                val statusCode = (e as ApiException).statusCode
                listener.onFailure("A problem occurred while checking device location settings.", e)
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val rae = e as ResolvableApiException
                        rae.startResolutionForResult(activity, 0)
                    } catch (sie: SendIntentException) {
                        sie.printStackTrace()
                    }
                }
            }
    }

    private val mLocationCallback = object : LocationCallback() {

        override fun onLocationAvailability(availability: LocationAvailability?) {
            super.onLocationAvailability(availability)
            Log.d(TAG, "onLocationAvailability --> LocationAvailability: $availability")
        }

        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            Log.d(TAG, "onLocationResult --> LocationResult: $result. Removing updates...")
            result?.lastLocation?.also {
                mLocationListener?.onSuccess(it)
                mLocationListener = null
                mFusedLocationProviderClient.removeLocationUpdates(this)
            }
        }
    }
}