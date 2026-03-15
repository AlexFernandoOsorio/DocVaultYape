package com.example.docvaultyape.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class LocationData(val latitude: Double, val longitude: Double, val address: String?)

@Singleton
class LocationManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationData? = suspendCancellableCoroutine { cont ->
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc != null) cont.resume(
                    LocationData(
                        loc.latitude,
                        loc.longitude,
                        getAddress(loc.latitude, loc.longitude)
                    )
                )
                else cont.resume(null)
            }
            .addOnFailureListener { cont.resume(null) }
    }

    private fun getAddress(lat: Double, lng: Double): String? = try {
        val geocoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            var result: String? = null
            geocoder.getFromLocation(lat, lng, 1) { addresses ->
                result = formatAddress(addresses.firstOrNull())
            }
            result
        } else {
            @Suppress("DEPRECATION")
            formatAddress(geocoder.getFromLocation(lat, lng, 1)?.firstOrNull())
        }
    } catch (e: Exception) {
        null
    }

    private fun formatAddress(address: Address?): String? {
        if (address == null) return null
        return buildList {
            address.thoroughfare?.let { add(it) }
            address.subThoroughfare?.let { add(it) }
            address.locality?.let { add(it) }
            address.adminArea?.let { add(it) }
            address.countryName?.let { add(it) }
        }.joinToString(", ").ifEmpty { null }
    }
}
