package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // testing to the RemindersListViewModel and its live data objects
    // Subject under test
    private lateinit var listViewModel: RemindersListViewModel

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
        // We initialise listViewModel with the fakeDataSource
        dataSource = FakeDataSource()
        listViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    // provide testing to the RemindersListViewModel and its live data objects

    @Test
    fun loadRemainders_showLoading() {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()
        // When loading remainders
        listViewModel.loadReminders()
        // Then loading event is triggered
        val loading = listViewModel.showLoading.getOrAwaitValue()
        MatcherAssert.assertThat(loading, Matchers.`is`(true))
        mainCoroutineRule.resumeDispatcher()
    }

    @Test
    fun loadRemainders_shouldReturnError() {
        // Make the repository return errors
        dataSource.setReturnError(true)
        // When loading remainders
        listViewModel.loadReminders()

        // Then an error message is shown
        MatcherAssert.assertThat(
            listViewModel.showSnackBar.getOrAwaitValue(), Matchers.equalTo("Test exception")
        )
        MatcherAssert.assertThat(listViewModel.showNoData.getOrAwaitValue(), Matchers.`is`(true))

    }

    @Test
    fun loadRemainders_withEmptyList_showNoData() {
        // Given empty remainders list
        // When loading remainders
        listViewModel.loadReminders()

        // Then the showNoData event is triggered
        val showNoData = listViewModel.showNoData.getOrAwaitValue()

        MatcherAssert.assertThat(showNoData, Matchers.`is`(true))
    }

    @Test
    fun loadRemainders_withOneRemainders_appears() = mainCoroutineRule.runBlockingTest {
        // With a dataSource that has a 1 remainder
        val remainder = ReminderDTO("Title1", "Description1", "location1", 0.0, 0.0)
        // save a remainder to list
        dataSource.saveReminder(remainder)
        // When loading remainders
        listViewModel.loadReminders()
        val reminderList = listViewModel.remindersList.getOrAwaitValue()

        // Then reminderList must contains Reminder info
        MatcherAssert.assertThat(reminderList, Matchers.`is`(Matchers.notNullValue()))
        val reminderFromList = reminderList.firstOrNull { it.id == remainder.id }
        MatcherAssert.assertThat(reminderFromList, Matchers.`is`(Matchers.notNullValue()))
    }
}