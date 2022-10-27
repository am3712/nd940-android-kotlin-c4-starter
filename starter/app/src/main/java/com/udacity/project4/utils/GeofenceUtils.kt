package com.udacity.project4.utils

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.GeofenceStatusCodes
import com.udacity.project4.R
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.utils.GeofencingConstants.ACTION_GEOFENCE_EVENT

/**
 * Returns the error string for a geofencing error code.
 */
fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
            R.string.geofence_not_available
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
            R.string.geofence_too_many_geofences
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
            R.string.geofence_too_many_pending_intents
        )
        else -> resources.getString(R.string.geofence_unknown_error)
    }
}

internal object GeofencingConstants {
    const val GEOFENCE_RADIUS_IN_METERS = 100f
    const val ACTION_GEOFENCE_EVENT =
        "com.udacity.project4.action.ACTION_GEOFENCE_EVENT"
}

fun Context.geofencePendingIntent(reminderId: String): PendingIntent {
    val intent = Intent(applicationContext, GeofenceBroadcastReceiver::class.java)
    intent.action = ACTION_GEOFENCE_EVENT
    // need to set component name in case of FLAG_MUTABLE
    // https://medium.com/androiddevelopers/all-about-pendingintents-748c8eb8619
    intent.component = ComponentName(
        "com.udacity.project4",
        "com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver"
    )
    return PendingIntent.getBroadcast(
        applicationContext, reminderId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT
    )
}