package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    // provide testing to the SaveReminderView and its live data objects
    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var dataSource: FakeDataSource

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        // We initialise the tasks to 3, with one active and two completed
        dataSource = FakeDataSource()
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun invalidReminderTitle_onSaveReminder_showSnackBarInt() {
        // Given by default invalid title value which is null or empty
        // When call onSaveReminder()
        saveReminderViewModel.onSaveReminder()

        // Then snackBarInt event is triggered with String Res [R.string.err_enter_title]
        val snackBarInt = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        MatcherAssert.assertThat(snackBarInt, Matchers.`is`(R.string.err_enter_title))
    }

    @Test
    fun invalidReminderLocation_onSaveReminder_showSnackBarInt() {
        // Given invalid location value which is null by default or empty.
        // and given title invalidReminderLocation
        saveReminderViewModel.reminderTitle.value = "invalidReminderLocation"

        // When call onSaveReminder()
        saveReminderViewModel.onSaveReminder()

        // Then snackBarInt event is triggered with String Res [R.string.err_select_location]
        val snackBarInt = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        MatcherAssert.assertThat(snackBarInt, Matchers.`is`(R.string.err_select_location))
    }

}