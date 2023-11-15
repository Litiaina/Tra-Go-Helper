package com.teamlitiaina.tragohelper.constants

class LocationUpdateConstants {
    companion object {
        const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 2000L
        const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000L
        const val MIN_UPDATE_DISTANCE_IN_METERS: Float = 1.0f
        const val WILL_WAIT_FOR_ACCURATE_LOCATION: Boolean = false
    }
}