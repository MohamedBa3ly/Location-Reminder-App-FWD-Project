package com.udacity.project4.locationreminders.savereminder


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest{

    //Get main coroutineRule:
    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    //Making late init var for save reminder view model and fake data source :
    private lateinit var savingViewModel : SaveReminderViewModel
    private val fakeDataResource = FakeDataSource()

    //get instant task executor rule :
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel(){
        stopKoin()
        savingViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),fakeDataResource)
    }

    //First we will test when loading :
    @Test
    fun showLoadingTest(){
        //Make reminder data item that i will check on it : (put any data)
        val locationData = ReminderDataItem("Portofino","The Italian Riviera","Italy",44.303890,9.207778,"6")
        //I will pause dispatcher until finished:
        mainCoroutineRule.pauseDispatcher()

        //And i will save data by save reminder :
        savingViewModel.saveReminder(locationData)
        assertThat(savingViewModel.showLoading.getOrAwaitValue(),`is`(true))

        //Then resume dispatcher again :
        mainCoroutineRule.resumeDispatcher()
        //And assert that :
        assertThat(savingViewModel.showLoading.getOrAwaitValue(),`is`(false))
    }


    //Testing for Should Return Error in two cases (that found in Fun validate enter data in save reminder view model ) : 1- when null location :
    @Test
    fun shouldReturnErrorWhenNullLocation(){
        //Make reminder data item that i will check on it : (put any data)
        val locationData = ReminderDataItem("Portofino","The Italian Riviera",null,44.303890,9.207778,"6")
        // enter data in validate enter data :
        val enterLocationDataInValidateFun = savingViewModel.validateEnteredData(locationData)
        assertThat(enterLocationDataInValidateFun,`is`(false))
        //So snackBar will appear with message :
        assertThat(savingViewModel.showSnackBarInt.getOrAwaitValue(),`is`(R.string.err_select_location))
    }

    //2- when null Title :
    @Test
    fun shouldReturnErrorWhenNullTitle(){
        //Make reminder data item that i will check on it : (put any data)
        val locationData = ReminderDataItem(null,"The Italian Riviera","Italy",44.303890,9.207778,"6")
        // enter data in validate enter data :
        val enterLocationDataInValidateFun = savingViewModel.validateEnteredData(locationData)
        assertThat(enterLocationDataInValidateFun,`is`(false))
        //So snackBar will appear with message :
        assertThat(savingViewModel.showSnackBarInt.getOrAwaitValue(),`is`(R.string.err_enter_title))
    }





}