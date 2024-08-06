package com.psi.dpsi.notification


data class PushNotification(
    val to: String? =null,
    val data: NotificationData
)