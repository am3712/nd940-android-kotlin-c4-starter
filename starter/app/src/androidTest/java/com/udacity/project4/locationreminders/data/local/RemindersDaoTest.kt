package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    // Add testing implementation to the RemindersDao.kt
    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN - insert a reminder
        val reminder = ReminderDTO("title", "description", "location", 0.0, 0.0)
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values
        assertThat(loaded as ReminderDTO, Matchers.notNullValue())
        assertThat(loaded.id, Matchers.`is`(reminder.id))
        assertThat(loaded.title, Matchers.`is`(reminder.title))
        assertThat(loaded.description, Matchers.`is`(reminder.description))
        assertThat(loaded.location, Matchers.`is`(reminder.location))
        assertThat(loaded.latitude, Matchers.`is`(reminder.latitude))
        assertThat(loaded.longitude, Matchers.`is`(reminder.longitude))
    }

    @Test
    fun updateTaskAndGetById() = runBlockingTest {
        // When inserting a reminder
        val originalReminder = ReminderDTO("title", "description", "location", 0.0, 0.0)
        database.reminderDao().saveReminder(originalReminder)

        // When the task is updated
        val updatedTask = ReminderDTO(
            "new title", "new description", "new location", 1.0, 1.0, id = originalReminder.id
        )
        database.reminderDao().saveReminder(updatedTask)

        // THEN - The loaded data contains the expected values
        val loaded = database.reminderDao().getReminderById(originalReminder.id)
        assertThat(loaded?.id, Matchers.`is`(originalReminder.id))
        assertThat(loaded?.title, Matchers.`is`("new title"))
        assertThat(loaded?.description, Matchers.`is`("new description"))
        assertThat(loaded?.location, Matchers.`is`("new location"))
        assertThat(loaded?.latitude, Matchers.`is`(1.0))
        assertThat(loaded?.longitude, Matchers.`is`(1.0))
    }
}