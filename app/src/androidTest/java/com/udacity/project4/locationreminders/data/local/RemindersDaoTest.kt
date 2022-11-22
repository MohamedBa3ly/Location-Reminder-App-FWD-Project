package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest{

    //Make lateinit var for db :
    private lateinit var db : RemindersDatabase
    //variable for instant executor rule :
    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //Before :
    @Before
    fun openDataBase(){
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),RemindersDatabase::class.java).build()
    }

    //After : ( when we open data base in java ,we must close it ) ...i think i was make this in java :)
    @After
    fun endDataBase(){
        db.close()
    }

    // I want to test saveReminder fun , getReminder fun and deleteAllReminders fun :
    // So i will save data in database then i will delete all data from db , finally i will get the data
    //so getReminder will be Empty
    @Test
    fun saveDeleteGetReminders() = runBlockingTest {
        val locationReminderData = ReminderDTO("Portofino","The Italian Riviera","Italy",44.303890,9.207778,"6")
        db.reminderDao().saveReminder(locationReminderData)
        db.reminderDao().deleteAllReminders()
        assertThat(db.reminderDao().getReminders().isEmpty(),`is`(true))
    }

    //Want to test getReminderByID fun :
    //so i will save all data first then i will get what i want  by id :
    @Test
    fun getReminderById() = runBlockingTest{
        val locationReminderData = ReminderDTO("Portofino","The Italian Riviera","Italy",44.303890,9.207778,"6")
        db.reminderDao().saveReminder(locationReminderData)
        val getReminderById = db.reminderDao().getReminderById(locationReminderData.id)

        //Then i will ensure get reminder by id not null : (Discuss in video7 - L5 P4 A07 Testing Room V3)
        assertThat<ReminderDTO>(getReminderById, notNullValue())
        //ID:
        assertThat(getReminderById?.id, `is`(locationReminderData.id))
        //Title :
        assertThat(getReminderById?.title, `is`(locationReminderData.title))
        //Description:
        assertThat(getReminderById?.description, `is`(locationReminderData.description))
        //location:
        assertThat(getReminderById?.location, `is`(locationReminderData.location))
        //latitude:
        assertThat(getReminderById?.latitude, `is`(locationReminderData.latitude))
        //longitude:
        assertThat(getReminderById?.longitude, `is`(locationReminderData.longitude))

    }



}