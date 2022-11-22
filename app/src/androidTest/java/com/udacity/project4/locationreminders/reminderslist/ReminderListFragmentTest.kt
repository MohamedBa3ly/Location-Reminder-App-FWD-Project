package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import com.udacity.project4.R
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.google.android.material.internal.ContextUtils
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest(){
    //Make lateinit var fro repository, appContext and list fragment view model :
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var listFragmentViewModel: RemindersListViewModel

    //take an instance from DataBindingIdlingResource:
    private val idleResource = DataBindingIdlingResource()

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
        //Get our real repository and list fragment viewModel :
        repository = get()
        listFragmentViewModel = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    //Using idling resource ,Discussed in video 11 - L5 P4 A11 Using Idling Resources V2 in nano degree :
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(idleResource)
    }

    //And after :
    @After
    fun unregisterIdlingResource(){
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(idleResource)
    }

    //    TODO: test the navigation of the fragments:
    //video number 15 in nanodegree program (Using Mockito to write navigation test) :

    @Test
    fun fabToNavigateToSaveFragment() = runBlockingTest {

        //Given on screen, First i will make a variable for a scenario :
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.Theme_LocationApp)
        idleResource.monitorFragment(scenario)

        //Using Mockito to make a navigation test :
        val navController = mock(NavController::class.java)
        scenario.onFragment{
            Navigation.setViewNavController(it.view!!,navController)
        }

        //When click on fab :
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Then verify that we will navigate to save reminder fragment :)
        verify(navController).navigate(ReminderListFragmentDirections.actionReminderListFragmentToSaveReminderFragment())
    }

    //    TODO: test the displayed data on the UI :
    @Test
    fun displayedDataOnUiTest() = runBlocking {

        //UI in list fragment contains title, description and location so lets try on data
        //i will enter same data that i use it :)
        val locationReminderData = ReminderDTO("Portofino","The Italian Riviera","Italy",44.303890,9.207778,"6")

        //And i will save this data :
        repository.saveReminder(locationReminderData)

        //Given : And i will make variable for scenario like previous test for fab :
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.Theme_LocationApp)
        idleResource.monitorFragment(scenario)

        //When : check on screen is matches to data :
        //1-Title :
        onView(withText(locationReminderData.title)).check(matches(isDisplayed()))
        //2-Description :
        onView(withText(locationReminderData.description)).check(matches(isDisplayed()))
        //3-Location :
        onView(withText(locationReminderData.location)).check(matches(isDisplayed()))

        //After this then delete data :)
        repository.deleteAllReminders()
    }

    //    TODO: add testing for the error messages :

    // if there is no data so Toast will appear no data :
    @Test
    fun noDataFound() = runBlocking {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.Theme_LocationApp)
        idleResource.monitorFragment(scenario)
        //When i Swipe down refresh layout is refreshed :
        onView(withId(R.id.refreshLayout)).perform(ViewActions.swipeDown())
        //and i will compare text that will appear when refresh in the text in view model if list not found ,
        //so text will be the same no data .
        onView(withText("no data")).inRoot(RootMatchers.withDecorView(Matchers.not(ContextUtils.getActivity(appContext)?.window?.decorView))).check(matches(isDisplayed()))
        //then delete all reminders in repository .
        repository.deleteAllReminders()

    }












}