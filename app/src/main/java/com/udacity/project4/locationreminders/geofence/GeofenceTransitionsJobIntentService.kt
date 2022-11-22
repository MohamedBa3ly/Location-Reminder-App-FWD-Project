package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.google.android.gms.location.Geofence
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.utils.sendNotification
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent


class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573
        const val TAG = "Error Message"


        //        TODO: call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        // Handle the geofencing transition events :
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent!!.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        // send a notification to the user when he enters the geofence area :
        if (geofencingEvent!!.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            sendNotification(geofencingEvent.triggeringGeofences!!)
//            if (geofencingEvent?.triggeringGeofences!!.isNotEmpty()){
//
//            }else{
//                Log.e(TAG, "No Geofence Found")
//            }
        }
    }

    //Get the request id of the current geofence :
    private fun sendNotification(triggeringGeofences: List<Geofence>) {

        //using for loop :
        for(geofence in triggeringGeofences){

            //request Id :
            val requestId = triggeringGeofences[0].requestId

            //Get the local repository instance
            val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                //get the reminder with the request id
                val result = remindersLocalRepository.getReminder(requestId)
                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )
                }
            }

        }

    }

}