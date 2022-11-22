package com.udacity.project4.locationreminders.reminderslist


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest{

    //Get main coroutineRule
    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    //Making late init var for reminder list view model and fake data resource :
    private lateinit var reminderListViewModel : RemindersListViewModel
    private val fakeDataResource = FakeDataSource()

    //get instant task executor rule :
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel(){
        stopKoin()
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataResource)
    }

    //At the end of load reminder Fun ... there is result error .so i will test this :)
    @Test
    fun shouldReturnError() = runBlockingTest {
        //Discuss in video 6 - L5 P4 A06 Testing Error Handling V3, nano degree program :
        //so i will return true from fake data resource first :
        fakeDataResource.setReturnError(true)
        //Then i will load and assert that :
        reminderListViewModel.loadReminders()
        assertThat(reminderListViewModel.showSnackBar.getOrAwaitValue(), `is`("Error occurred"))

    }

    //I will check loading :
    @Test
    fun check_loading(){
        //i will pause dispatcher first : ( in video 5 Testing coroutine timing ):
        mainCoroutineRule.pauseDispatcher()
        reminderListViewModel.loadReminders()
        assertThat(reminderListViewModel.showLoading.getOrAwaitValue(),`is`(true))
        //After finished i will resume a Dispatcher again :
        mainCoroutineRule.resumeDispatcher()
        assertThat(reminderListViewModel.showLoading.getOrAwaitValue(),`is`(false))
    }

    //Check if there is no data :
    @Test
    fun check_noData() = runBlockingTest{
        //So i will delete all first :
        fakeDataResource.deleteAllReminders()
        //Then load and assert :
        reminderListViewModel.loadReminders()
        assertThat(reminderListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }





}