package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.utils.SingleLiveEvent
import com.udacity.project4.utils.geofencePendingIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class RemindersListViewModel(
    private val app: Application, private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    // list that holds the reminder data to be displayed on the UI
    val remindersList = MutableLiveData<List<ReminderDataItem>>()

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {
        showLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminders()
            showLoading.postValue(false)
            when (result) {
                is Result.Success<*> -> {
                    val dataList = ArrayList<ReminderDataItem>()
                    dataList.addAll((result.data as List<ReminderDTO>).map { reminder ->
                        //map the reminder data from the DB to the be ready to be displayed on the UI
                        ReminderDataItem(
                            reminder.title,
                            reminder.description,
                            reminder.location,
                            reminder.latitude,
                            reminder.longitude,
                            reminder.id
                        )
                    })
                    remindersList.value = dataList
                }
                is Result.Error -> showSnackBar.value = result.message
            }

            //check if no data has to be shown
            invalidateShowNoData()
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    }


    val logoutEvent = SingleLiveEvent<Boolean>()

    // logout logic:
    // 1) unregister geofences
    // 2) remove reminders data
    // 3) trigger logout event to use FirebaseAuth logout
    fun unregisterAndClearRemindersDataAndProcessLogout() {
        val geofencingClient = LocationServices.getGeofencingClient(app)
        viewModelScope.launch(Dispatchers.IO) {
            remindersList.value?.forEach { reminder ->
                try {
                    geofencingClient.removeGeofences(app.geofencePendingIntent(reminder.id)).await()
                } catch (ex: Exception) {
                    Timber.e("Failed to remove geofence of reminder: $reminder", ex)
                }
            }
            dataSource.deleteAllReminders()
            logoutEvent.postValue(true)
        }
    }
}