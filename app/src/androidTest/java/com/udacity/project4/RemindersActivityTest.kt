package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
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
class RemindersActivityTest : AutoCloseKoinTest() {
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
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
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    //Using idling resource ,Discussed in video 11 - L5 P4 A11 Using Idling Resources V2 in nano degree :
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    //And after :
    @After
    fun unregisterIdlingResource(){
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

//    TODO: add End to End testing to the app :



    //I will take you in a quick journey in my app :
    @Test
    fun locationReminder() = runBlocking {

        //i will make a variable for activity scenario and use data binding idling resource to
        //use monitor activity
        //Discuss in video 11 using Idling resource in nano degree :
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //from start there is no data so (no data and (fab to add reminder) is found )  :
        //so u will check if no data is displayed
        //and fab is clicked
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())

        //then u will navigate to add title and description and location , then fab to save reminder :
        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("Location Title"),ViewActions.closeSoftKeyboard())
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("Location Description"),ViewActions.closeSoftKeyboard())
        onView(withId(R.id.selectLocation)).perform(click())

        //after u press on select location u will navigate to select location fragment,
        //select the location that u want to make it reminder, then press on save button :
        //Note : i make check null entry if user forget to enter title or description or select location , it will show toast (Enter empty field) to avoid crash of app:
        onView(withId(R.id.location_map)).perform(click())
        onView(withId(R.id.btn_save)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())

        //then check it title and description is matching with that u typed :
        onView(withText("Location Title")).check(matches(isDisplayed()))
        onView(withText("Location Description")).check(matches(isDisplayed()))

        //then close the activity Scenario :
        activityScenario.close()
    }

    //Test snack bar when no title found :
    @Test
    fun noTitleFound(){
        //i will make a variable for activity scenario and use data binding idling resource to
        //use monitor activity
        //Discuss in video 11 using Idling resource in nano degree :
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //First press on FAB to add location reminder :
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Save reminder fragment will open :
        //Select location on map:
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.location_map)).perform(click())
        onView(withId(R.id.btn_save)).perform(click())

        //Write description :
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("My descriptions"),ViewActions.closeSoftKeyboard())

        //Press on save reminder FAB :
        onView(withId(R.id.saveReminder)).perform(click())

        //Message from snack bar will show on screen :)
        onView(withText("Please enter title")).check(matches(isDisplayed()))

        //then close activity scenario :
        activityScenario.close()

    }

    //Test snack bar when no location found :
    @Test
    fun noLocationFound(){
        //i will make a variable for activity scenario and use data binding idling resource to
        //use monitor activity
        //Discuss in video 11 using Idling resource in nano degree :
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //First press on FAB to add location reminder :
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Save reminder fragment will open :
        //Write Title:
        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("My title"),ViewActions.closeSoftKeyboard())

        //Write description :
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("My descriptions"),ViewActions.closeSoftKeyboard())

        //Press on save reminder FAB :
        onView(withId(R.id.saveReminder)).perform(click())

        //Message from snack bar will show on screen :)
        onView(withText("Please select location")).check(matches(isDisplayed()))

        //then close activity scenario :
        activityScenario.close()

    }


}







