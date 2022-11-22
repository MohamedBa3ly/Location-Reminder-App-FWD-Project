package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest{
    //First we will test Reminders local repository so i should make late init var for it :
    lateinit var repo :RemindersLocalRepository

    //Repository is here so also i will make late init var for data base :
    lateinit var db : RemindersDatabase

    //Instance from InstantTaskExecutorRule :
    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //Now i should open data base :
    @Before
    fun openDataBaseAndSetupRepo(){
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),RemindersDatabase::class.java).allowMainThreadQueries().build()

        //Reminders local repository take a two variable (Reminder Dao and Dispatcher:
        //here our test in main so our dispatcher will be in Main :
        repo = RemindersLocalRepository(db.reminderDao(),Dispatchers.Main)
    }

    // i open db so i should close it i say this before in Reminder Dao Test :)
    @After
    fun endDataBase(){
        db.close()
    }

    //So go and test , i want to test Fun that found in Repo , so i will begin with fun that i love it (Delete all reminders)  :)
    @Test
    fun deleteAllReminderAndGetRemindersTest() = runBlocking {
        //i want to test something make a deleting like our Fun ,so before delete i will save first then delete ,
        //so let us make something to delete :)
        val locationReminderData = ReminderDTO("Portofino","The Italian Riviera","Italy",44.303890,9.207778,"6")
        //save data in repo first :
        repo.saveReminder(locationReminderData)
        //Then delete all data :
        repo.deleteAllReminders()
        //To check if data is deleted successfully , i should get data and see if already repo is null now  :
        //fun getReminders  return Result so i should make a casting as a success result :)
        val repoResult = repo.getReminders() as Result.Success
        //so data was removed ,yea... test will pass :)
        assertThat(repoResult.data.isEmpty(),`is`(true))
    }

    @Test
    fun saveReminderAndGetReminderById() = runBlocking {
        // i will save any data :
        val locationReminderData = ReminderDTO("Portofino","The Italian Riviera","Italy",44.303890,9.207778,"6")
        repo.saveReminder(locationReminderData)
        //Then i will get this data by id , so i will use fun get reminder:
        repo.getReminder(locationReminderData.id)
        //Fun get Reminder will return Result so first make sure its success:
        assertThat(repo.getReminder(locationReminderData.id) is Result.Success,`is`(true))
        //So if return Success we will complete same things that happened in the previous test but not null data :
        val repoResult = repo.getReminder(locationReminderData.id) as Result.Success
        //Id :
        assertThat(repoResult.data.id,`is`(locationReminderData.id))
        //Title:
        assertThat(repoResult.data.title,`is`(locationReminderData.title))
        //Description:
        assertThat(repoResult.data.description,`is`(locationReminderData.description))
        //Location:
        assertThat(repoResult.data.location,`is`(locationReminderData.location))
        //Latitude:
        assertThat(repoResult.data.latitude,`is`(locationReminderData.latitude))
        //Longitude:
        assertThat(repoResult.data.longitude,`is`(locationReminderData.longitude))
    }

    //Data not found error Test :
    @Test
    fun reminderNotFoundError() = runBlocking {

        //Save any data :
        val locationReminderData = ReminderDTO("Portofino","The Italian Riviera","Italy",44.303890,9.207778,"6")
        repo.saveReminder(locationReminderData)

        //Delete it to make it null :
        repo.deleteAllReminders()

        //And i will get the data by id :
        val repoResult = repo.getReminder(locationReminderData.id)
        //Then assert that :
        assertThat(repoResult is Result.Error ,`is`(true))
        repoResult as Result.Error
        //then message will be :
        assertThat(repoResult.message,`is`("Reminder not found!"))

    }



}