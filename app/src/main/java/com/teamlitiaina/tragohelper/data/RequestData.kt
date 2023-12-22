package com.teamlitiaina.tragohelper.data

data class RequestData(
    val requestUID: String? = null,
    val vehicleOwnerEmail: String? = null,
    val serviceProviderEmail: String? = null,
    val vehicleType: String? = null,
    val otherNotes: String? = null,
    val progress: String? = null
)