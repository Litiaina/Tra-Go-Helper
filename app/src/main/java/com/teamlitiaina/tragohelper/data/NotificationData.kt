package com.teamlitiaina.tragohelper.data

data class NotificationData(
    val receiverEmail: String? = null,
    val title: String? = null,
    val information: String? = null,
    val datetime: String? = null,
    val isSeen: Boolean = false
)
