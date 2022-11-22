package com.udacity.project4.locationreminders.data


import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeDataSource(var reminderLocation : MutableList<ReminderDTO> = mutableListOf()) :ReminderDataSource {

    //Discuss in video 6 - L5 P4 A06 Testing Error Handling V3, nano degree program :
    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }


    //First make an implementation for methods , and override save reminder and add a reminder to a reminder location mutable list :
    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderLocation.add(reminder)
    }

    //Same in save reminder but here we will clear all reminder in delete :
    override suspend fun deleteAllReminders() {
        reminderLocation.clear()
    }

    //Here we have two option Happy and Bad , if success or in error :
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if(shouldReturnError) return Result.Error("Error occurred")
        return Result.Success(reminderLocation)
    }

    //Same in get reminders but here it depends on id :
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError){
            return Result.Error("failed to get reminder from DB")
        }
        val location = reminderLocation.firstOrNull{it.id==id}
        return if (location!=null){
            Result.Success(location)
        }else{
            Result.Error("Reminder Location not found")
        }
    }

}