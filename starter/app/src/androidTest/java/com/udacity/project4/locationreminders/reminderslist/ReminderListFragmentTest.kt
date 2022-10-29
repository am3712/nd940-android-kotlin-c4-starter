package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel { RemindersListViewModel(appContext, get() as ReminderDataSource) }
            single { SaveReminderViewModel(appContext, get() as ReminderDataSource) }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        // declare a new koin module
        startKoin { modules(listOf(myModule)) }

        // Get our real repository
        repository = GlobalContext.get().get()

        //clear the data to start fresh
        runBlocking { repository.deleteAllReminders() }
    }

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    // test the navigation of the fragments.

    @Test
    fun clickAddReminderButton_navigateToSaveReminderFragment() {
        // GIVEN - On the home screen
        val fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        val navController = mock(NavController::class.java)
        fragmentScenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        // WHEN - Click on the "+" button
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // THEN - Verify that we navigate to the add screen
        Mockito.verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    // test the displayed data on the UI.
    @Test
    fun openRemindersScreen_twoRemindersInDB_DisplayedInUi() {
        // GIVEN - Add two reminders to the DB
        val reminder1 = ReminderDTO("TITLE1", "DESCRIPTION1", "LOCATION NAME 1", 1.0, 1.0)
        val reminder2 = ReminderDTO("TITLE2", "DESCRIPTION2", "LOCATION NAME 2", 2.0, 2.0)
        runBlocking {
            repository.saveReminder(reminder1)
            repository.saveReminder(reminder2)
        }
        // WHEN - Reminder list fragment launched to display reminders
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        // THEN - reminders fragment list are displayed on the screen
        // make sure that the items count is 2
        onView(withId(R.id.reminderssRecyclerView)).check(
            ViewAssertions.matches((ViewMatchers.hasChildCount(2)))
        )

        // and then make sure that the title/description/location are shown and correct
        onView(ViewMatchers.withText("TITLE1")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText("DESCRIPTION1")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText("LOCATION NAME 1")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText("TITLE2")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText("DESCRIPTION2")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText("LOCATION NAME 2")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    // add testing for the error messages.
    @Test
    fun noReminders_verifyNoDataShow() {
        // GIVEN - no reminders in the DB
        // WHEN - reminders fragment list launched no data text shown
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        // make sure that the noDataTextView textView shown.
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}