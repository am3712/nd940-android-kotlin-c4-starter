package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.dto.ReminderDTO

// Use FakeDao that acts as a test double to the RemindersDao
class FakeRemindersDao(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : RemindersDao {
    override suspend fun getReminders(): List<ReminderDTO> = reminders?.toList() ?: emptyList()

    override suspend fun getReminderById(reminderId: String): ReminderDTO? =
        reminders?.firstOrNull { it.id == reminderId }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

}