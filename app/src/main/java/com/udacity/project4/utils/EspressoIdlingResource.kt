package com.udacity.project4.utils

import androidx.test.espresso.idling.CountingIdlingResource

//I am using EspressoIdlingResource to facilitate long tasks in background, or basically anything that takes long time :
object EspressoIdlingResource {
    //I can name it anything but i named it global like in google sample :)
    private const val RESOURCE = "GLOBAL"

    @JvmField val countingIdlingResource = CountingIdlingResource(RESOURCE)

    //Fun increment :
    fun increment(){
        countingIdlingResource.increment()
    }

    //Fun decrement:
    fun decrement(){
        if(!countingIdlingResource.isIdleNow){
            countingIdlingResource.decrement()
        }
    }

    //Make an inline Fun called Wrap espresso idling resource :
    //Discuss in video 9 - L5 P4 A09 CountingIdlingResource V3 in nano degree program :
    inline fun <T> wrapEspressoIdlingResource(function: () -> T): T {
        EspressoIdlingResource.increment() // Set app as busy.
        return try {
            function()
        } finally {
            EspressoIdlingResource.decrement() // Set app as idle.
        }
    }


}