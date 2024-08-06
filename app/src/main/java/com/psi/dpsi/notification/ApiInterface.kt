package com.psi.dpsi.notification


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {

    @Headers(
        "Content-Type: application/json",
        "Authorization: key=AAAAb-8Cry4:APA91bFkITkHoJtl0FeIc0RQ0fBc4w3Wqo6oAC23Te9jt2pNVE-bOxbVetiAPMQoDPDa9IbRX8PbFJb6w55O0GVsk1V5XytGm_3grEEoB0VoqTABzq2szZNMkBsIMmVgCuwk00UShUpf"
    )
    @POST("fcm/send")
    fun sendNotification(@Body notification: PushNotification): Call<PushNotification>


}


