package com.example.sportable.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportable.R
import com.example.sportable.adapters.EventItemsAdapter
import com.example.sportable.adapters.SportItemsAdapter
import com.example.sportable.adapters.SportItemsFiltersAdapter
import com.example.sportable.firebase.FirestoreClass
import com.example.sportable.models.Event
import com.example.sportable.models.LastFilters
import com.example.sportable.models.Sport
import com.example.sportable.utils.Constants
import com.example.sportable.utils.GetAddressFromLatLng
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_all_events.*
import kotlinx.android.synthetic.main.activity_create_event.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.all_events_content.*
import kotlinx.android.synthetic.main.app_bar_all_events.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.app_bar_my_events.*
import kotlinx.android.synthetic.main.filters_drawer_content.*
import kotlinx.android.synthetic.main.my_events_content.*
import kotlinx.android.synthetic.main.my_events_content.cv_my_events_created
import kotlinx.android.synthetic.main.my_events_content.rv_my_events_created_by_me
import java.util.*
import kotlin.collections.ArrayList
import kotlin.text.StringBuilder

class AllEventsActivity : BaseActivity() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var sportsToString = mSportsList.map { it.name }.toTypedArray()
    private var sportList= ArrayList<Int>(sportsToString.size)
    private var selectedSport= BooleanArray(sportsToString.size)
    private lateinit var mUserLogin: String
    private var mLatitude = userAddress.latitude
    private var mLongitude = userAddress.longitude
    companion object{
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_events)

        for(j in selectedSport.indices){
            selectedSport[j] = true
        }

        et_filters_location.setText(userAddress.location.toString())



        if (intent.hasExtra(Constants.LOGIN)) {
            mUserLogin= intent.getStringExtra(Constants.LOGIN)!!
        }

        setupActionBar()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        updateEventsList()

        if (!Places.isInitialized()) {
            Places.initialize(
                this,
                resources.getString(R.string.google_maps_api_key)
            )
        }

        tv_select_current_location_filters.setOnClickListener {
            setCurrentLocation()
        }

        iv_manual_search.setOnClickListener {
            toggleDrawer()
        }

        tv_sport_filter.setOnClickListener {
            dropDownListSportsFilters()
        }

        /*spinner_sports = findViewById(R.id.spinner_sports)
        val adapter = SportItemsFiltersAdapter(this, mSportsList)
        spinner_sports.adapter = adapter*/
        et_filters_location.setOnClickListener { locationHandler()
            //val intent = Intent(this, MapCreatingEventActivity::class.java)
            //startActivity(intent)
            //Log.e("sadasdsada","asdasdsad")

        }

        btn_confirm_filters.setOnClickListener {
            reloadFilters()
            toggleDrawer()
        }
    }

    private fun setCurrentLocation() {
        if (!isLocationEnabled()) {
            Toast.makeText(
                this,
                "Your location provider is turned off. Please turn it on.",
                Toast.LENGTH_SHORT
            ).show()

            // This will redirect you to settings from where you need to turn on the location provider.
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            requestNewLocationData()
                        }else{
                            requestNewLocationData()

                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread()
                .check()
        }
    }
    private fun showRationalDialogForPermissions() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Toast.makeText(this@AllEventsActivity, "klik", Toast.LENGTH_SHORT).show()
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            mLatitude = mLastLocation.latitude
            //Toast.makeText(this@AllEventsActivity, mLatitude.toString(), Toast.LENGTH_SHORT).show()
            Log.e("Current Latitude", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")

            val addressTask =
                GetAddressFromLatLng(this@AllEventsActivity, mLatitude, mLongitude)

            addressTask.setAddressListener(object :
                GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    Log.e("Address ::", "" + address)
                    et_filters_location.setText(address) // Address is set to the edittext
                }

                override fun onError() {
                    Log.e("Get Address ::", "Something is wrong...")
                }
            })

            addressTask.getAddress()
            // END
        }

    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_all_events)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "Available Events"
        }
        toolbar_all_events.setNavigationOnClickListener { onBackPressed()
            //saveFilters()
        }
    }
    private fun toggleDrawer(){
        if(drawer_layout_filters.isDrawerOpen(GravityCompat.END)){
            drawer_layout_filters.closeDrawer(GravityCompat.END)
        }else{
            drawer_layout_filters.openDrawer(GravityCompat.END)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MainActivity.MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getAllEventsList(this)
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE) {

            val place: Place = Autocomplete.getPlaceFromIntent(data!!)

            et_filters_location.setText(place.address)
            mLatitude = place.latLng!!.latitude
            mLongitude = place.latLng!!.longitude
        }

        else{
            Log.e("Cancelled", "Cancelled")
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun populateEventsListToUI(eventsList: ArrayList<Event>){
        hideProgressDialog()

        eventsList.removeIf{e: Event -> e.currentNumberOfPeople>=e.maxPeople}
        eventsList.removeIf{e: Event ->
            val float = floatArrayOf(.1f)
            val distanceBetween = Location.distanceBetween(e.latitude,e.longitude, mLatitude, mLongitude,float)
            Toast.makeText(this, float[0].toString(), Toast.LENGTH_SHORT).show()
            var str = km.text.toString()
            float[0] > str.toFloat()*1000
        }

        if(eventsList.size > 0){

            eventsList.sortBy { it.date }

            cv_all_events.visibility = View.VISIBLE
            tv_no_events_all_events.visibility = View.INVISIBLE
            //tv_no_e.visibility = View.GONE


            rv_all_events.layoutManager = LinearLayoutManager(this)
            rv_all_events.setHasFixedSize(true)

            val adapter = EventItemsAdapter(this, eventsList)
            rv_all_events.adapter = adapter

            adapter.setOnClickListener(object : EventItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Event) {
                    val intent = Intent(this@AllEventsActivity, EventDetailsActivity::class.java)
                    intent.putExtra(
                        Constants.DOCUMENT_ID, model.documentId)
                    startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
                }
            })
        }else{
            cv_all_events.visibility = View.INVISIBLE
            tv_no_events_all_events.visibility = View.VISIBLE
        }





    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun updateEventsList(){
        showProgressDialog("Please Wait...")
        FirestoreClass().getAllEventsList(this)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun dropDownListSportsFilters(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Sport")
        builder.setCancelable(false)
        builder.setMultiChoiceItems(sportsToString, selectedSport, DialogInterface.OnMultiChoiceClickListener(){
            dialogInterface, i, b ->

            if(b){
                sportList.add(i)
                sportList.sort()
            }else{
                sportList.remove(i)
            }


        })

        builder.setPositiveButton("OK", DialogInterface.OnClickListener(){
            dialogInterface, i ->
        })

        builder.setNegativeButton("Select All", DialogInterface.OnClickListener(){
            //dialogInterface, i ->
            //dialogInterface.dismiss()
                dialogInterface, i ->
            for(j in selectedSport.indices){
                selectedSport[j] = true
            }
        })
        builder.setNeutralButton("Select All", DialogInterface.OnClickListener(){
                dialogInterface, i ->
            for(j in selectedSport.indices){
                selectedSport[j] = true
            }
        })

        builder.setNeutralButton("Clear All", DialogInterface.OnClickListener(){
            dialogInterface, i ->
            for(j in selectedSport.indices){
                selectedSport[j] = false
                sportList.clear()
            }
        })

        builder.show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun reloadFilters() {

        var map: Map<String, Boolean> = mSportsList.map { it.documentId }.toTypedArray().zip(selectedSport.map { it }.toTypedArray()).toMap()
        //var map: Map<Sport, Boolean> = mSportsList.map { it }.toTypedArray().zip(selectedSport.map{it}.toTypedArray()).toMap()
        var eventsAfterFiltration= ArrayList<Event>()
        for(e in allEvents) {
            for (m in map) {
                if (e.sportId == m.key && m.value){
                    eventsAfterFiltration.add(e)
                }
            }
        }
        populateEventsListToUI(eventsAfterFiltration)
        showToast()
    }

    fun lastFiltersAddedSuccessfully() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }

    private fun saveFilters(){
        var filtersToSave = ArrayList<String>()
        for(s in selectedSport)
        {
            if(s) filtersToSave.add(mSportsList[selectedSport.indexOf(s)].name)
        }
        val lastFilters= LastFilters(FirestoreClass().getCurrentUserID(), filtersToSave)

        FirestoreClass().saveFilters(this, lastFilters)
    }


    private fun locationHandler(){
        try {
            val fields = listOf(
                Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )

            val intent =
                Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this)
            startActivityForResult(intent, Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}