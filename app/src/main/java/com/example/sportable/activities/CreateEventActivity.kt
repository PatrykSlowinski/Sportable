package com.example.sportable.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.sportable.R
import com.example.sportable.dialogs.SportsListDialog
import com.example.sportable.firebase.FirestoreClass
import com.example.sportable.models.Event
import com.example.sportable.models.Sport
import com.example.sportable.utils.Constants
import com.example.sportable.utils.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE
import com.example.sportable.utils.GetAddressFromLatLng
import com.google.android.gms.location.*
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteFragment
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.grpc.InternalChannelz.id
import kotlinx.android.synthetic.main.activity_create_event.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.io.IOException
import java.nio.channels.CancelledKeyException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import java.time.LocalDateTime

class CreateEventActivity : BaseActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private lateinit var mUserLogin: String
    private lateinit var mSelectedSportId: String
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var mSelectedDateMilliSeconds: Long = 0
    private var hour = 0
    private var minute = 0
    private var year = 0
    private var month = 0
    private var day = 0
    private var savedDay = 0
    private var savedHour = 0
    private var savedMinute = 0
    private var savedYear = 0
    private var savedMonth = 0



    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        setupActionBar()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!Places.isInitialized()) {
            Places.initialize(
                this,
                resources.getString(R.string.google_maps_api_key)
            )
        }


        if (intent.hasExtra(Constants.LOGIN)) {
            mUserLogin = intent.getStringExtra(Constants.LOGIN)!!
        }


        btn_create.setOnClickListener {
            createEvent()
        }

        tv_select_sport_create_event.setOnClickListener {
            FirestoreClass().getSportsList(this)
        }

        et_location.setOnClickListener {
            locationHandler()
        }

        tv_select_current_location.setOnClickListener {
            selectCurrentLocation()
        }

        tv_select_time.setOnClickListener{
            getDateTimeCalendar()
            val dpd= DatePickerDialog(this, this, year, month, day)
            dpd.datePicker.minDate = System.currentTimeMillis()
            dpd.show()
        }

        btn_min_info.setOnClickListener{
            showErrorSnackBar("Minimum number of people determines number of people that is needed to start the event. If there are fewer members 2 hours before the start of the event, the event is canceled.")
        }

        btn_max_info.setOnClickListener{
            showErrorSnackBar("If the number of members of the event reaches the maximum number of people, no one else will be able to join.")
        }

        btn_time_info.setOnClickListener{
            showErrorSnackBar("Set the date and time for the event. Remember that the event should start at least 2 hours from the current time, otherwise it will be canceled due to the lack of the minimum number of people.")
        }

        btn_duration_info.setOnClickListener{
            showErrorSnackBar("Set the duration of event in minutes.")
        }

        btn_location_info.setOnClickListener{
            showErrorSnackBar("Set the event location manually or set your current location as the event location by clicking the SELECT CURRENT LOCATION button.")
        }

    }

    private fun getDateTimeCalendar(){
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year =  cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR_OF_DAY)
        minute = cal.get(Calendar.MINUTE)

    }



    private fun setupActionBar() {

        setSupportActionBar(toolbar_create_event_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString((R.string.create_new_event))
        }

        toolbar_create_event_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun createEvent(){
        var minimum : Int = 0
        var maximum : Int = 0
        var duration: Int = 0

        var numericsAreOk = true
        try{
            minimum = Integer.parseInt(et_minimum.text.toString())
            maximum = Integer.parseInt(et_maximum.text.toString())
            duration = Integer.parseInt(et_duration.text.toString())}
        catch(e: NumberFormatException){
            numericsAreOk = false
            showErrorSnackBar("Minimum, maximum number of people and duration of event have to be numeric! Minimum and maximum numbers of people have to be less or equal 50.")
        }

        if(numericsAreOk){
            if(minimum<=1) showErrorSnackBar("Minimum number of people has to be greater than 1.")
            else if(maximum<=1) showErrorSnackBar("Maximum number of people has to be greater than 1.")
            else if(maximum>50 || minimum >50) showErrorSnackBar("Minimum and maximum number of people have to be less or equal 50.")
            else if(duration<15 || duration>300) showErrorSnackBar("Duration of the event has to be greater than 15 minutes and less than 300 minutes.")
            else if(maximum < minimum) showErrorSnackBar("Maximum number of people has to be greater or equal to minimum.")
            else if(tv_select_sport_create_event.text == "") showErrorSnackBar("You have to choose any sport.")
            else if(tv_select_time.text.toString() == "") showErrorSnackBar("You have to choose event time.")
            else if(et_location.text.toString() == "") showErrorSnackBar("You have to choose event location.")
            else{
                showProgressDialog(resources.getString(R.string.please_wait))
                val assignedUsersArrayList: ArrayList<String> = ArrayList()
                assignedUsersArrayList.add((getCurrentUserID()))

                var event = Event(
                    mSelectedSportId.toString(),
                    mUserLogin,
                    assignedUsersArrayList,
                    et_location.text.toString(),
                    minimum,
                    maximum,
                    1,
                    "",
                    mSelectedDateMilliSeconds,
                    mLatitude,
                    mLongitude,
                    duration
                )

                FirestoreClass().createEvent(this, event)
            }
        }
    }


    fun eventCreatedSuccessfully() {

        hideProgressDialog()

        finish()
    }

    fun sportsListDialog(sportsList: ArrayList<Sport>){
        val mSportList = sportsList

        val listDialog = object : SportsListDialog(
            this,
            mSportList,
            "Select Sport"
        ){
            override fun onItemSelected(sport: Sport) {
                setSport(sport)
            }

        }
        listDialog.show()
    }

    private fun setSport(sport: Sport) {
        mSelectedSportId = sport.documentId
        tv_select_sport_create_event.text = sport.name
        try {
                    Glide
                        .with(this@CreateEventActivity)
                        .load(sport.image)
                        .centerCrop()
                        .placeholder(R.drawable.sports)
                        .into(iv_sport_image)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
    }

    private fun locationHandler(){
        try {
            val fields = listOf(
                Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )
            val intent = Intent(this, MapCreatingEventActivity::class.java)
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
                if (data != null) {
                    et_location.setText(data.getStringExtra("placeAddress"))
                    mLatitude = data.getDoubleExtra("placeLat", 0.0)
                    mLongitude = data.getDoubleExtra("placeLng", 0.0)
                }
            }else{
                Log.e("Activity", "Cancelled or Back Pressed")
            }

    }

    private fun showDataPicker() {
        val c = Calendar.getInstance()
        val year =
            c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"

                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                val theDate = sdf.parse(selectedDate)

                mSelectedDateMilliSeconds = theDate!!.time
                Toast.makeText(this, mSelectedDateMilliSeconds.toString(),Toast.LENGTH_LONG ).show()
            },
            year,
            month,
            day
        )
        dpd.show()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun selectCurrentLocation(){
        if (!isLocationEnabled()) {
            Toast.makeText(
                this,
                "Your location provider is turned off. Please turn it on.",
                Toast.LENGTH_SHORT
            ).show()

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
                        }
                        else{
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
        AlertDialog.Builder(this)
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

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            mLatitude = mLastLocation.latitude
            Log.e("Current Latitude", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")

            val addressTask =
                GetAddressFromLatLng(this@CreateEventActivity, mLatitude, mLongitude)

            addressTask.setAddressListener(object :
                GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    Log.e("Address ::", "" + address)
                    et_location.setText(address) // Address is set to the edittext
                }

                override fun onError() {
                    Log.e("Get Address ::", "Something is wrong...")
                }
            })

            addressTask.getAddress()
        }
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month
        savedYear = year

        getDateTimeCalendar()

        val tpd = TimePickerDialog(this, this, hour, minute, true)
        tpd.show()
    }

    override fun onTimeSet(p0: TimePicker?, hourOfDay: Int, minute: Int) {
        var todayDate = Date()
        val currentHours = Calendar.getInstance().time.hours
        val currentMinutes = Calendar.getInstance().time.minutes
        val selectedDate = "$savedDay/${savedMonth+1}/$savedYear"
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val theDate = sdf.parse(selectedDate)
        var areDatesSame = false
        if(todayDate.day.equals(theDate.day) && todayDate.month.equals(theDate.month) && todayDate.year.equals(theDate.year)) areDatesSame = true
        if(areDatesSame && (hourOfDay<currentHours || (currentHours==hourOfDay && minute < currentMinutes))){
            Toast.makeText(this, "Start time cannot be earlier than current time!", Toast.LENGTH_SHORT).show()
            val tpd = TimePickerDialog(this, this, hour, minute, true)
            tpd.show()
        }else {
            savedHour = hourOfDay
            savedMinute = minute

            val selectedData = "$savedDay/${savedMonth + 1}/$savedYear $savedHour:$savedMinute"
            tv_select_time.text = "$savedDay/${savedMonth + 1}/$savedYear $savedHour:$savedMinute"
            val sdf2 = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH)
            val theSavedDate = sdf2.parse(selectedData)
            mSelectedDateMilliSeconds = theSavedDate!!.time

            val date = Date(mSelectedDateMilliSeconds)
            val cal = Calendar.getInstance()
            cal.timeInMillis = mSelectedDateMilliSeconds
        }
    }


}
