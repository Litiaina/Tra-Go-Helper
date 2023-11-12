package com.teamlitiaina.tragohelper.utility

import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LocationUtils {
    companion object {

        private const val EARTH_RADIUS = 6371e3

        fun calculateBearing(startPoint: LatLng, endPoint: LatLng): Float {
            val startLat = Math.toRadians(startPoint.latitude)
            val startLong = Math.toRadians(startPoint.longitude)
            val endLat = Math.toRadians(endPoint.latitude)
            val endLong = Math.toRadians(endPoint.longitude)

            val deltaLong = endLong - startLong

            val x = sin(deltaLong) * cos(endLat)
            val y = cos(startLat) * sin(endLat) - (sin(startLat) * cos(endLat) * cos(deltaLong))

            var bearing = atan2(x, y)
            bearing = Math.toDegrees(bearing)
            bearing = (bearing + 360) % 360

            return bearing.toFloat()
        }

        fun getDistanceHaversine(startLatitude: Double, startLongitude: Double, endLatitude: Double, endLongitude: Double, callback: (Double?) -> Unit) {
            val phi1 = Math.toRadians(startLatitude)
            val phi2 = Math.toRadians(endLatitude)
            val deltaPhi = Math.toRadians(endLatitude - startLatitude)
            val deltaLambda = Math.toRadians(endLongitude - startLongitude)

            val a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
                    cos(phi1) * cos(phi2) *
                    sin(deltaLambda / 2) * sin(deltaLambda / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            callback((EARTH_RADIUS * c))
        }

        fun formatDistance(distance: Double): String {
            var value = distance
            var unit = " m"
            if (distance < 1) {
                value *= 1000
                unit = " mm"
            } else if (distance > 1000) {
                value /= 1000
                unit = " km"
            }
            return String.format("%.1f%s", value, unit)
        }

    }
}