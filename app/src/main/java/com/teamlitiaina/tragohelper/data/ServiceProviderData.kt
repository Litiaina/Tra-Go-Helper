package com.teamlitiaina.tragohelper.data

import java.io.Serializable

class ServiceProviderData(
    val userUID: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val profilePicture: String? = null,
    val token: String? = null,
    val type: String? = null,
    val address: String? = null,
    val businessOperatingHours: OperatingHours? = null,
    val businessPermitFileURL: String? = null,
    val identificationPhotoFileURL: String? = null,
    val businessType: String? = null,
    val description: String? = null,
    val establishmentDate: String? = null,
    val ownerName: String? = null,
    val registrationNumber: String? = null,
    val serviceOffers: List<String>? = null,
    val status: String? = null,
) : Serializable