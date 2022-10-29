package com.udacity.project4.locationreminders.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.core.IsEqual
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Add testing implementation to the RemindersLocalRepository.kt
    private val reminder1 = ReminderDTO("Title1", "Description1", "location1", 1.0, 1.0)
    private val reminder2 = ReminderDTO("Title2", "Description2", "location2", 2.0, 2.0)
    private val localReminders = listOf(reminder1, reminder2).sortedBy { it.id }
    private lateinit var fakeRemindersDao: FakeRemindersDao

    // Class under test
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun createRepository() {
        fakeRemindersDao = FakeRemindersDao(localReminders.toMutableList())
        // Get a reference to the class under test
        remindersLocalRepository = RemindersLocalRepository(fakeRemindersDao, Dispatchers.Main)
    }

    @Test
    fun getReminders_requestsAllRemindersFromDao() = mainCoroutineRule.runBlockingTest {
        // When reminders are requested from the reminders repository
        val reminders = remindersLocalRepository.getReminders() as Result.Success

        // Then reminders are loaded from the Dao
        Assert.assertThat(reminders.data, IsEqual(localReminders))
    }

    @Test
    fun getReminderById_usingExistingItemId_successResult() = mainCoroutineRule.runBlockingTest {
        // When get reminder by id requested from the reminders repository
        val reminder = remindersLocalRepository.getReminder(id = reminder1.id)

        // Then result from type Success
        Assert.assertThat(reminder, Matchers.instanceOf(Result.Success::class.java))
        MatcherAssert.assertThat((reminder as Result.Success).data, Matchers.notNullValue())
        MatcherAssert.assertThat(reminder.data, Matchers.equalTo(reminder1))
    }

    @Test
    fun getReminderById_ofNoneExistItem_callErrorToDisplay() = mainCoroutineRule.runBlockingTest {
        // When get reminder by id requested from the reminders repository
        val reminder = remindersLocalRepository.getReminder(id = "")

        // Then result from type Error
        Assert.assertThat(reminder, Matchers.instanceOf(Result.Error::class.java))
        MatcherAssert.assertThat((reminder as Result.Error).message, Matchers.notNullValue())
        MatcherAssert.assertThat(reminder.message, Matchers.equalTo("Reminder not found!"))
    }
}