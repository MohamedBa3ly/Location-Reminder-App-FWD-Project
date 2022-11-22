package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.ApiException
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    private lateinit var binding: FragmentSaveReminderBinding
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    //Define a broadcast receiver for geofence transitions(define a PendingIntent that starts a BroadcastReceiver):
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    //Make variable for geofencingClient :
    private lateinit var geofencingClient: GeofencingClient

    //For Back Ground Location :
    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if(it){

            createLocationRequest()
        }else{
            //Toast if the permission is denied:
            Toast.makeText(requireContext(),"Permission is denied",Toast.LENGTH_SHORT).show()
        }

    }

    //For Fine Location :
    private val locationPermissionFine = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it){

            createLocationRequest()
        }else{
            //Toast if the permission is denied:
            Toast.makeText(requireContext(),"Permission is denied",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_save_reminder,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Initializing:
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        //SnackBar:
        showSnackbar()

        //First we should take an instance from geofencing client:
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        //Navigate to Location Fragment :
        navigateToDetermineLocation()

        //Save Reminder and take permission first:
        toSaveReminder()

    }

    //Fun to navigate to Location fragment to determine user location :)
    private fun navigateToDetermineLocation(){
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.postValue(NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()))

        }
    }

    //Fun to save reminder and ask for permission first :
    private fun toSaveReminder(){
        binding.saveReminder.setOnClickListener {

            //Permission for backGround First :
            locationAppPermission()

        }
    }

    //Fun for data and add geofence:
    private fun dataOfReminder(){

        val id = _viewModel.selectedPOI.value?.placeId ?:""
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value?:0.0
        val longitude = _viewModel.longitude.value?:0.0

        //Data and make validate for it :
        val data = _viewModel.validateEnteredData(ReminderDataItem(title,description, location, latitude, longitude,
            id!!
        ))

        //Add a geofencing request :
        if (data) {
            val geofence = Geofence.Builder().setRequestId(id)
                .setCircularRegion(latitude!!, longitude!!, 70f)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

            val geofenceRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence).build()

            //Add geofence (First put permission) :
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    requireContext(),
                    "Permission is denieddd",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent)
                    .run {
                        addOnSuccessListener {
                            _viewModel.validateAndSaveReminder(
                                ReminderDataItem(
                                    title,
                                    description,
                                    location,
                                    latitude,
                                    longitude,
                                    geofence.requestId
                                )
                            )
                            Toast.makeText(
                                requireContext(),
                                "geofence success",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "geofence failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    //Override fun to Clear all details when destroy :
    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }

    //Companion object for action geofence event :
    companion object{
        const val ACTION_GEOFENCE_EVENT = "400"
        const val LOCATION_SETTING_REQUEST = 999
    }


    private fun showSnackbar(){
        _viewModel.showSnackBarInt.observe(viewLifecycleOwner, Observer {
            Snackbar.make(
                binding.root,
                it, Snackbar.LENGTH_LONG
            ).setAction(android.R.string.ok) {
                //Do nothing?
            }.show()
        })
    }

    //Fun to make permission for foreground and background and callback above :)
    private fun locationAppPermission(){
        //On Android 10 (API level 29) and higher, you must declare the ACCESS_BACKGROUND_LOCATION permission in your app's manifest in order to request background location access at runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val locationPermissionApp = Manifest.permission.ACCESS_BACKGROUND_LOCATION
            locationPermission.launch(locationPermissionApp)
        } else {
        // On earlier versions of Android, when your app receives foreground location access, it automatically receives background location access as well.
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
                createLocationRequest()
            }else{
                // i will ask user for permission again if it denied before :)
                val locationPermissionAppFine = Manifest.permission.ACCESS_FINE_LOCATION
                locationPermissionFine.launch(locationPermissionAppFine)
            }
        }
    }


    //Create a location request :
    private fun createLocationRequest(){

        //Make a Location request :
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client : SettingsClient = LocationServices.getSettingsClient(requireContext())
        val task : Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnCompleteListener {

            try {
                //Here we will make a Dialog to ask me to open the location (when get result) :
                it.getResult(ApiException::class.java)
                dataOfReminder()

            }catch (e:ApiException){
                when(e.statusCode){

                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        startIntentSenderForResult(e.status.resolution?.intentSender,
                            LOCATION_SETTING_REQUEST,null,0,0,0,null)

                    }catch (e:IntentSender.SendIntentException){ }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        }
    }


    //Make onActivityResult : when press on Ok to activate location -- and cancel to keep it disable :
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            LOCATION_SETTING_REQUEST -> {
                when(resultCode){
                    Activity.RESULT_OK ->{
                        Toast.makeText(requireContext(), "Location is opened", Toast.LENGTH_SHORT).show()
                        dataOfReminder()
                    }

                    Activity.RESULT_CANCELED -> {
                        Toast.makeText(requireContext(), "Location still off", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

}