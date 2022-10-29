package com.udacity.project4

import android.app.Application
import androidx.annotation.StringRes
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest : AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel
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
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        // Get our real repository
        repository = get()

        // Get our real saveReminderViewModel
        saveReminderViewModel = get()

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

    @Test
    fun saveReminderScreen_addReminder_verifyDisplayed() {
        // Start up reminders screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // WHEN - Click on the "+" button to navigate to save reminder screen
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // fill title & description & location info
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle)).perform(ViewActions.replaceText("NEW TITLE"))
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription)).perform(ViewActions.replaceText("NEW DESCRIPTION"))
        saveReminderViewModel.selectedPOI.postValue(
            PointOfInterest(LatLng(37.8, -122.2), "", "NEW LOCATION")
        )
        // WHEN - Click on save button
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        // Then added reminder info appears in reminders list
        // make sure that the title/description/location are shown and correct
        Espresso.onView(ViewMatchers.withText("NEW TITLE"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("NEW DESCRIPTION"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("NEW LOCATION"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // using ActivityScenario.launch, always call close()
        activityScenario.close()
    }

    @Test
    fun saveReminderScreen_addReminderWithInvalidData_verifySnackBarMessageAppears() {
        // Start up reminders screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // WHEN - Click on the "+" button to navigate to save reminder screen
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // fill description only
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription)).perform(ViewActions.replaceText("NEW DESCRIPTION"))
        saveReminderViewModel.selectedPOI.postValue(
            PointOfInterest(LatLng(37.8, -122.2), "", "NEW LOCATION")
        )
        // WHEN - Click on save button
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        // Then snackBar appear with error Res R.string.err_enter_title
        // make sure that the title/description/location are shown and correct
        checkSnackBarDisplayedByMessage(R.string.err_enter_title)
        // using ActivityScenario.launch, always call close()
        activityScenario.close()
    }

    @Test
    fun reminderListScreen_doubleBackButton() = runBlocking {
        val reminder = ReminderDTO("title 3", "description 3", "location 3", 37.5, -23.1)
        repository.saveReminder(reminder)

        // Start up reminder list screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on the "+" button to navigate to save reminder screen
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // Click on select location text to navigate to select location screen
        Espresso.onView(ViewMatchers.withId(R.id.selectLocation)).perform(ViewActions.click())

        // Confirm that if we click back once, we end up back at the save reminder page
        Espresso.pressBack()
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // Confirm that if we click back a second time, we end up back at the home screen ( reminder list screen )
        Espresso.pressBack()
        Espresso.onView(ViewMatchers.withId(R.id.reminderssRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // When using ActivityScenario.launch, always call close()
        activityScenario.close()
    }

}

private fun checkSnackBarDisplayedByMessage(@StringRes message: Int) {
    Espresso.onView(ViewMatchers.withText(message)).check(
        ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
    )
}