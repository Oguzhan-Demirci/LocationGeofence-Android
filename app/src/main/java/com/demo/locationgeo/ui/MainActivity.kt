package com.demo.locationgeo.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.demo.locationgeo.R
import com.demo.locationgeo.data.listener.ResultListener
import com.demo.locationgeo.manager.GeoManager
import com.demo.locationgeo.manager.LocationManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "GEO_MainActivity"

        private val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        private const val PERMISSION_REQUEST_CODE = 87
    }

    private val mGeoManager by lazy { GeoManager(this) }
    private val mLocationManager by lazy { LocationManager(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonAddGeofence.setOnClickListener {
            getLocation()
        }

        buttonRemoveGeofence.setOnClickListener {
            mGeoManager.removeGeofenceWithID()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty()) {
            getLocation()
        }
    }

    private fun getLocation() {
        if (hasPermissions(requiredPermissions)) {
            mLocationManager.getLocation(this, object : ResultListener<Location> {

                override fun onSuccess(result: Location?) {
                    result?.also {
                        val text = "Location: ${it.latitude}, ${it.longitude}"
                        tvResult.text = text
                        addGeofence(it.latitude, it.longitude, 200F)
                    }
                }

                override fun onFailure(message: String, exception: Exception?) {
                    tvResult.text = message
                }

            })
        } else {
            ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSION_REQUEST_CODE)
        }
    }

    private fun hasPermissions(permissions: Array<String>) = permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun addGeofence(latitude: Double, longitude: Double, radius: Float) {
        if (hasPermissions(requiredPermissions)) {
            mGeoManager.addGeofenceToList(latitude, longitude, radius)
        } else {
            ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSION_REQUEST_CODE)
        }
    }

}