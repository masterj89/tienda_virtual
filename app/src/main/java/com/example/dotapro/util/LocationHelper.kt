package com.example.dotapro.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.core.content.ContextCompat
import java.util.Locale

/** Geolocalización: obtiene la última ubicación conocida y la convierte en dirección. */
object LocationHelper {

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** Llama a [onResult] con la dirección (o coordenadas), o null si no hay ubicación. */
    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun fetchAddress(activity: Activity, onResult: (String?) -> Unit) {
        val manager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .mapNotNull { provider -> try { manager.getLastKnownLocation(provider) } catch (e: Exception) { null } }
            .maxByOrNull { it.time }

        if (location == null) {
            onResult(null)
            return
        }
        Thread {
            val address = try {
                Geocoder(activity, Locale("es", "CO"))
                    .getFromLocation(location.latitude, location.longitude, 1)
                    ?.firstOrNull()?.getAddressLine(0)
            } catch (e: Exception) {
                null
            }
            activity.runOnUiThread {
                onResult(address ?: "%.5f, %.5f".format(location.latitude, location.longitude))
            }
        }.start()
    }
}
