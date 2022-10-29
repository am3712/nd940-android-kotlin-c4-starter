# Location Reminder

A Todo list app with location reminders that remind the user to do something when he reaches a specific location. The app will require the user to create an account and login to set and access reminders.

## Getting Started

1. Clone the project to your local machine.
2. Open the project using Android Studio.

### Dependencies

```
1. A created project on Firebase console.
2. A create a project on Google console.
```

### Installation

Step by step explanation of how to get a dev environment running.

```
1. To enable Firebase Authentication:
        a. Go to the authentication tab at the Firebase console and enable Email/Password and Google Sign-in methods.
        b. download `google-services.json` and add it to the app.
2. To enable Google Maps:
    a. Go to APIs & Services at the Google console.
    b. Select your project and go to APIs & Credentials.
    c. Create a new api key and restrict it for android apps.
    d. Add your package name and SHA-1 signing-certificate fingerprint.
    c. Enable Maps SDK for Android from API restrictions and Save.
    d. Copy the api key to the `google_maps_api.xml`
3. Run the app on your mobile phone or emulator with Google Play Services in it.
```

## Testing

Right click on the `test` or `androidTest` packages and select Run Tests

### Break Down Tests

Explain what each test does and why


1. androidTest
   - `RemindersDaoTest` Unit test the DAO
     - Testing uses `Room.inMemoryDatabaseBuilder` to create a Room DB instance.
     - Testing uses `@Before` to initDb using `Room.inMemoryDatabaseBuilder` & `@After` to closeDb.
     - @Test `insertReminderAndGetById` insert a reminder & when Get the reminder by id from the database make sure The loaded data contains the expected values.
     - @Test `updateTaskAndGetById` insert a reminder & when the task is updated  make sure The loaded data contains the expected values.
   - `FakeRemindersDao` Use FakeDao that acts as a test double to the RemindersDao.
     - Implementation of a RemindersDao with static access to the data for easy testing using param `reminders` that passed through class constructor.
   - `RemindersLocalRepositoryTest` Medium Test to test the repository.
     - set the main coroutines dispatcher for unit testing using `MainCoroutineRule`.
     -  Testing uses @Before to init `FakeRemindersDao`  which Class under test depend on it.
     -  @Test `getReminders_requestsAllRemindersFromDao` reminders are requested from the reminders repository then make sure result from type Success & The loaded data contains the fake Dao data.
     -  @Test `getReminderById_usingExistingItemId_successResult` get reminder by id requested from the reminders repository which already exist on fake doa then result from type Success & The loaded data contains the expected values.
     -  @Test `getReminderById_ofNoneExistItem_callErrorToDisplay` When get reminder by id requested from the reminders repository of non exist item then the result of type Error & error message is "Reminder not found!".
   - `ReminderListFragmentTest` UI Testing
    - initialize Koin related code to be able to use it in out testing to get our real repository.
    - use `DataBindingIdlingResource` that waits for Data Binding to have no pending bindings.
    - `@before` registerIdlingResource Idling resources tell Espresso that the app is idle or busy. This is needed when operations are not scheduled in the main Looper (for example when executed on a different thread).
    - `@After` Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
    - test navigation using Mockito.mock(NavController::class.java)
    - @Test `clickAddReminderButton_navigateToSaveReminderFragment` On the home screen when Click on the "+" button Verify that we navigate to the add screen.
    - @Test `noReminders_verifyNoDataShow` no reminders in the DB WHEN reminders fragment list launched no data text shown.
    - @Test `openRemindersScreen_twoRemindersInDB_DisplayedInUi` repository contains two items when Reminder list fragment launched to display reminders then make sure that the items count is 2 & the title/description location are shown and correct.
  - `RemindersActivityTest` END TO END test to black box test the app
    - `@Before` init initialize Koin related code to be able to use it in out testing to get our real repository & our real saveReminderViewModel
    - `@After` reset clear the data
    - use `DataBindingIdlingResource` that waits for Data Binding to have no pending bindings.
    - `@before` registerIdlingResource Idling resources tell Espresso that the app is idle or busy. This is needed when operations are not scheduled in the main Looper (for example when executed on a different thread).
    - `@After` Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
    - @Test `saveReminderScreen_addReminder_verifyDisplayed` Start up reminders Activity screen & Click on the "+" button to navigate to save reminder screen & fill title & description & location & POI info Then Click on save button and Then the added reminder info appears in reminders list.
    - @Test `saveReminderScreen_addReminderWithInvalidData_verifySnackBarMessageAppears` Start up reminders Activity screen & fill description only & Click on save button Then snackBar appear with error message "Please enter title"
    - @Test `reminderListScreen_doubleBackButton` test navigation reminders list -> save reminder -> select location & double pressBack then reminders screen displayed
2. test
   - `FakeDataSource` that acts as a test double to the ReminderDataSource.
     - Implementation of a data source with static access to the data for easy testing using param `reminders` mutable list of reminders that passed through class constructor.
     - `shouldReturnError` boolean variable for testing purpose to control the behavior of functions (getReminders(), getReminder(id: String)) to force return `Result.Error` with message "Test exception" & default value is false.
     - fun `getReminders()` just check if shouldReturnError to return `Result.Error` else return `reminders` list data.
     - fun `saveReminder()` accept reminder data as paramater and just add it to `reminders` list.
     - fun `getReminder(id:String)` overloaded fun to get reminder by id first check if shouldReturnError then return `Result.Error` else search in `reminders` list if item not found then return Result.Error with message "Reminder not found!" else return item.
     - fun `deleteAllReminders()` clear items that reminders list hold.
   - `RemindersListViewModelTest` 
     - `mainCoroutineRule` Set the main coroutines dispatcher for unit testing.
     - `instantExecutorRule` Executes each task synchronously using Architecture Components.
     - `Before` setupViewModel We initialise listViewModel with the fakeDataSource.
     - `After` tearDown stopKoin.
     - `loadRemainders_showLoading` Pause dispatcher so we can verify initial values & When loading remainders then loading event is triggered.
     - `loadRemainders_shouldReturnError` Make the repository return errors & When loading remainders Then an error message is shown.
     - `loadRemainders_withEmptyList_showNoData` Given empty remainders list & When loading remainders Then the showNoData event is triggered.
     - `loadRemainders_withOneRemainders_appears` With a dataSource that has a 1 remainder When loading remainders Then reminderList must contains added reminder info.
   - `SaveReminderViewModelTest` provide testing to the SaveReminderViewModel and its live data objects
     - `mainCoroutineRule` Set the main coroutines dispatcher for unit testing.
     - `instantExecutorRule` Executes each task synchronously using Architecture Components.
     - `Before` setupViewModel We initialise saveReminderViewModel with the fakeDataSource.
     - `After` tearDown stopKoin.
     - `invalidReminderTitle_onSaveReminder_showSnackBarInt` given invalid title value which is null or empty When call onSaveReminder() Then snackBarInt event is triggered with String Res [R.string.err_enter_title] "Please enter title"
     - `invalidReminderLocation_onSaveReminder_showSnackBarInt` Given invalid location value which is null by default or empty When call onSaveReminder() Then snackBarInt event is triggered with String Res [R.string.err_select_location] "Please select location"


## Project Instructions
    1. Create a Login screen to ask users to login using an email address or a Google account.  Upon successful login, navigate the user to the Reminders screen.   If there is no account, the app should navigate to a Register screen.
    2. Create a Register screen to allow a user to register using an email address or a Google account.
    3. Create a screen that displays the reminders retrieved from local storage. If there are no reminders, display a   "No Data"  indicator.  If there are any errors, display an error message.
    4. Create a screen that shows a map with the user's current location and asks the user to select a point of interest to create a reminder.
    5. Create a screen to add a reminder when a user reaches the selected location.  Each reminder should include
        a. title
        b. description
        c. selected location
    6. Reminder data should be saved to local storage.
    7. For each reminder, create a geofencing request in the background that fires up a notification when the user enters the geofencing area.
    8. Provide testing for the ViewModels, Coroutines and LiveData objects.
    9. Create a FakeDataSource to replace the Data Layer and test the app in isolation.
    10. Use Espresso and Mockito to test each screen of the app:
        a. Test DAO (Data Access Object) and Repository classes.
        b. Add testing for the error messages.
        c. Add End-To-End testing for the Fragments navigation.


## Student Deliverables:

1. APK file of the final project.
2. Git Repository with the code.

## Built With

* [Koin](https://github.com/InsertKoinIO/koin) - A pragmatic lightweight dependency injection framework for Kotlin.
* [FirebaseUI Authentication](https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md) - FirebaseUI provides a drop-in auth solution that handles the UI flows for signing
* [JobIntentService](https://developer.android.com/reference/androidx/core/app/JobIntentService) - Run background service from the background application, Compatible with >= Android O.

## License
