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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
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
            showProgressDialog(resources.getString(R.string.please_wait))
            createEvent()
        }

        tv_select_sport_create_event.setOnClickListener {
            FirestoreClass().getSportsList(this)
        }

        et_location.setOnClickListener { locationHandler()
            //val intent = Intent(this, MapCreatingEventActivity::class.java)
            //startActivity(intent)
            //Log.e("sadasdsada","asdasdsad")

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

    }

    private fun getDateTimeCalendar(){
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year =  cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR)
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
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add((getCurrentUserID()))

        var event = Event(
            mSelectedSportId.toString(),
            mUserLogin,
            assignedUsersArrayList,
            et_location.text.toString(),
            Integer.parseInt(et_minimum.text.toString()),
            Integer.parseInt(et_maximum.text.toString()),
            "",
            mSelectedDateMilliSeconds,
            mLatitude,
            mLongitude
        )

        FirestoreClass().createEvent(this, event)
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
               // Toast.makeText(this@CreateEventActivity, sport.name, Toast.LENGTH_LONG).show()
                //tv_select_sport_create_event.text = "asddsa"//sport.name.toString()
                /*try {
                    // Load the user image in the ImageView.
                    Glide
                        .with(this@CreateEventActivity)
                        .load(sport.image) // URI of the image
                        .centerCrop() // Scale type of the image.
                        .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                        .into(iv_sport_image) // the view in which the image will be loaded.
                } catch (e: IOException) {
                    e.printStackTrace()
                }*/
            }

        }
        listDialog.show()

    }

    private fun setSport(sport: Sport) {
        mSelectedSportId = sport.documentId
        tv_select_sport_create_event.text = sport.name
        try {
                    // Load the user image in the ImageView.
                    Glide
                        .with(this@CreateEventActivity)
                        .load(sport.image) // URI of the image
                        .centerCrop() // Scale type of the image.
                        .placeholder(R.drawable.sports) // A default place holder
                        .into(iv_sport_image) // the view in which the image will be loaded.
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
            /*val autocompleteFragment =
                supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
            autocompleteFragment?.setPlaceFields(fields)*/

            val intent = Intent(this, MapCreatingEventActivity::class.java)
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)





            //val intent = Intent(this, MapActivity::class.java)
            //intent.putExtra(Constants.LOCATION)
            //startActivity(intent)
            // Start the autocomplete intent with a unique request code.
            //val intent =
                //Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                  //  .build(this)
            //startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("sadasdsad","asdasdsad")
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            Log.e("dziala","asdasdsad")
                //val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                //val place: Place? = data?.getParcelableExtra<Place>("place")
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
            c.get(Calendar.YEAR) // Returns the value of the given calendar field. This indicates YEAR
        val month = c.get(Calendar.MONTH) // This indicates the Month
        val day = c.get(Calendar.DAY_OF_MONTH) // This indicates the Day

        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"

                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"

                tv_select_date.text = selectedDate

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                val theDate = sdf.parse(selectedDate)

                mSelectedDateMilliSeconds = theDate!!.time
                Toast.makeText(this, mSelectedDateMilliSeconds.toString(),Toast.LENGTH_LONG ).show()
            },
            year,
            month,
            day
        )
        dpd.show() // It is used to show the datePicker Dialog.
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
            // END
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
            Toast.makeText(this, "Current date:"+savedYear.toString(),Toast.LENGTH_SHORT).show()
            savedHour = hourOfDay
            savedMinute = minute

            val selectedData = "$savedDay/${savedMonth + 1}/$savedYear $savedHour:$savedMinute"
            tv_select_time.text = "$savedDay/${savedMonth + 1}/$savedYear $savedHour:$savedMinute"
            val sdf2 = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH)
            val theSavedDate = sdf2.parse(selectedData)
            mSelectedDateMilliSeconds = theSavedDate!!.time

            //val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            //val selectedDate = simpleDateFormat.format(Date(mSelectedDateMilliSeconds))
            val date = Date(mSelectedDateMilliSeconds)
            val cal = Calendar.getInstance()
            cal.timeInMillis = mSelectedDateMilliSeconds


            Toast.makeText(this, currentHours.toString(), Toast.LENGTH_LONG).show()
        }
    }
}
