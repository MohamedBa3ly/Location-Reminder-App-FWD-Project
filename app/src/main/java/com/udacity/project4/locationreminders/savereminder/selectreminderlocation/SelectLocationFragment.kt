package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentSelectLocationBinding
    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    //Make (Map) variable :
    private lateinit var map : GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    //make variables for Marker and POI :
    private lateinit var poi : PointOfInterest
    private lateinit var markerx : Marker

    //Variable for location permissions:
    val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION]==true|| permissions[Manifest.permission.ACCESS_COARSE_LOCATION]==true){
            setupMap()
        }else{
            //Toast if the permission is denied:
            Toast.makeText(requireContext(),"Permission is denied",Toast.LENGTH_SHORT).show()
            setupMap()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Initializing:
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        //First show permission :
        locationAppPermission()

        //Add Menu :
        addMenu()

        //When user select the location and press on save button :
        onLocationSelected()

    }


    //Fun to add Menu :
    private fun addMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.map_options, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.normal_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_NORMAL
                        return true
                    }
                    R.id.hybrid_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_HYBRID
                        return true
                    }
                    R.id.satellite_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                        return true
                    }
                    R.id.terrain_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                        return true
                    }
                    else -> {false}
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

    }

    //Fun to setup map in fragment :
    private fun setupMap(){
        val mFragment = childFragmentManager.findFragmentById(R.id.location_map)as SupportMapFragment
        mFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    //Here i will make all things that will happen when user select the location :
    private fun onLocationSelected() {
        binding.btnSave.setOnClickListener {
            if (this::poi.isInitialized){
                //Set value for reminder selected location str :
                _viewModel.reminderSelectedLocationStr.value= poi.name
                //set value for selected poi :
                _viewModel.selectedPOI.value= poi
                //set value for latitude :
                _viewModel.latitude.value = poi.latLng.latitude
                //set value for longitude :
                _viewModel.longitude.value = poi.latLng.longitude
            }
            findNavController().popBackStack()
        }
    }

    //Override fun that implemented from class :
    override fun onMapReady(mapX: GoogleMap) {
        map = mapX
        style(map)
        poiX(map)
        mapLongClick(map)
        myLocation(map)
    }

    //Fun Style to change style of map :
    private fun style(mapX: GoogleMap){
        try {
            //Here i will set style (json file) that i made in raw directory from (https://mapstyle.withgoogle.com/)  :)
            val style = mapX.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),R.raw.style_map))
            if (!style){
                // here i can use Log.d also :)
                Toast.makeText(requireContext(),"style doesn't appear",Toast.LENGTH_SHORT).show()
            }
        }catch (e:Resources.NotFoundException){
            Toast.makeText(requireContext(),"There is no style: $e",Toast.LENGTH_SHORT).show()
        }
    }

    //Fun for POI :
    private fun poiX(mapX: GoogleMap){
        mapX.setOnPoiClickListener {
            //First if marker is initialized remove it :
            if (this::markerx.isInitialized){
                markerx.remove()
            }
            //Add Marker with its position and title :
            markerx = mapX.addMarker(MarkerOptions().position(it.latLng).title(it.name))!!

            //Here in fun poi   (it = poi) :
            poi = it

            //Show information :
            markerx.showInfoWindow()

        }
    }

    //Fun for Map Long Click :
    private fun mapLongClick(mapX: GoogleMap){
        mapX.setOnMapLongClickListener {
            //First if marker is initialized remove it :
            if (this::markerx.isInitialized){
                markerx.remove()
            }

            //make snippet:
            val snippet = String.format(Locale.getDefault(),getString(R.string.lat_long_snippet),it.latitude,it.longitude)

            //Poi now is :
            poi = PointOfInterest(it,snippet,snippet)

            //Marker :
            markerx = mapX.addMarker(MarkerOptions().position(it)
                .title(getString(R.string.reminder_location)).snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )!!

            //Show info Window:
            markerx.showInfoWindow()

        }
    }

    //Fun for My Location :
    private fun myLocation(mapX: GoogleMap){

        //To go to my location , First check permission:
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            map.isMyLocationEnabled = true
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {

                //First check if location is open in your device to determine yor location on the map :

                if(it != null){

                    val snippet = String.format(
                        Locale.getDefault(),
                        getString(R.string.lat_long_snippet),
                        it.latitude,
                        it.longitude
                    )

                    val latLngX = LatLng(it.latitude, it.longitude)
                    poi = PointOfInterest(latLngX,snippet,"My location now")

                    //Marker :
                    markerx = mapX.addMarker(MarkerOptions().position(latLngX)
                        .title(getString(R.string.reminder_location)).snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )!!

                    //Zoom and move camera :
                    val zoomLevel = 20f
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngX, zoomLevel))
                    //Show info Window
                    markerx.showInfoWindow()

                }else{
                    //Message to open your location on device when location is turned off :
                    createLocationRequest()
                }
            }

        }else{
            //Toast if the permission is denied:
            Toast.makeText(requireContext(),"Permission is denied",Toast.LENGTH_SHORT).show()
        }
    }

    //Fun to open location device from setting ( another option to open your location ) i have two option from setting and directly from dialog:
    private fun openLocationDevice(){
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setTitle("Location")
            setMessage("Your GPS disabled, do you want to enable it?")
            setPositiveButton("Yes") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS )
                startActivity(intent)
            }
            setNegativeButton("No") { _, _ ->
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    //Fun to make permission for foreground callback above :)
    private fun locationAppPermission(){
        val locationPermissionApp = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)

        locationPermission.launch(locationPermissionApp)
    }

    //Fun to show dialog to open location of device directly when press ok :
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
            }catch (e: ApiException){
                when(e.statusCode){

                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        startIntentSenderForResult(e.status.resolution?.intentSender,
                            SaveReminderFragment.LOCATION_SETTING_REQUEST,null,0,0,0,null)

                    }catch (e: IntentSender.SendIntentException){ }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        }
    }


    //Make onActivityResult : when press on Ok to activate location -- and cancel to keep it disable :
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            SaveReminderFragment.LOCATION_SETTING_REQUEST -> {
                when(resultCode){
                    Activity.RESULT_OK ->{
                        Toast.makeText(requireContext(), "Location is opened", Toast.LENGTH_SHORT).show()
                    }

                    Activity.RESULT_CANCELED -> {
                        Toast.makeText(requireContext(), "Location still off", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }




}