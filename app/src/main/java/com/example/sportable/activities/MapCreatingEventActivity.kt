package com.example.sportable.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.sportable.R
import com.example.sportable.firebase.FirestoreClass
import com.example.sportable.utils.Constants
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.android.synthetic.main.activity_event_details.*
import kotlinx.android.synthetic.main.activity_map_creating_event.*
import java.io.IOException

class MapCreatingEventActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener {

    private val pERMISSION_ID = 42
    lateinit var marker: Marker
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    var currentLocation: com.google.android.gms.maps.model.LatLng =
        com.google.android.gms.maps.model.LatLng(0.0, 0.0)
    var address: String = ""
    var lat: Double = 0.0
    var lng:Double = 0.0

    lateinit var mMap: GoogleMap
    lateinit var geocoder: Geocoder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_creating_event)
        setupActionBar()
        val ai: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        val apiKey = value.toString()

        if (!Places.isInitialized()) {
            Places.initialize(
                this,
                resources.getString(R.string.google_maps_api_key)
            )
        }

        val supportMapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.mapCreating) as SupportMapFragment
        supportMapFragment.getMapAsync(this@MapCreatingEventActivity)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        geocoder = Geocoder(this)
        val fields = listOf(
            Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
            Place.Field.ADDRESS
        )
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
        autocompleteFragment?.setPlaceFields(fields)
        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                setMarker(place)
                Toast.makeText(
                    this@MapCreatingEventActivity,
                    place.name.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(status: Status) {

            }
        })

        btn_confirm_adress.setOnClickListener {
            setLocation()
        }
        btn_my_cur_loc.setOnClickListener{
            getCurrentLocationMarker()
        }


    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationMarker() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        currentLocation = com.google.android.gms.maps.model.LatLng(
                            location.latitude,
                            location.longitude
                        )
                        // mMap.clear()
                        val marker2 = mMap.addMarker(MarkerOptions().position(currentLocation).title("Your current location").draggable(false).icon(BitmapDescriptorFactory.fromResource(R.drawable.iconsuser)))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16F))
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
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

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            pERMISSION_ID
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            currentLocation = com.google.android.gms.maps.model.LatLng(
                mLastLocation.latitude,
                mLastLocation.longitude
            )
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == pERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocationMarker()
            }
        }
    }

    private fun setLocation() {
        address = marker.title
        val data = Intent()
        data.putExtra("placeAddress", address)
        data.putExtra("placeLat", marker.position.latitude)
        data.putExtra("placeLng", marker.position.longitude)
        this.setResult(Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE,  data)
        finish()
    }

    private fun setMarker(place: Place) {
        mMap.clear()
        marker = mMap.addMarker(MarkerOptions().position(place.latLng).title(place.address).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.iconsstadium)))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 17F))
        /*address = place.address
        lat = place.latLng.latitude
        lng = place.latLng.longitude*/
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_creating_event_location)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "Set location of the event"
        }

        toolbar_creating_event_location.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val position =
            com.google.android.gms.maps.model.LatLng(userAddress.latitude, userAddress.longitude)
        val address: String = (geocoder.getFromLocation(userAddress.latitude, userAddress.longitude, 1))[0].getAddressLine(0)

        marker = googleMap.addMarker(
            MarkerOptions().position(position).title(address).icon(BitmapDescriptorFactory.fromResource(R.drawable.iconsstadium)).draggable(true))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 13f)
        googleMap.animateCamera(newLatLngZoom)

        mMap.setOnMarkerDragListener(this)
        mMap.setOnMapLongClickListener(this)
            }

    override fun onMarkerDragStart(p0: Marker) {
        Log.d(TAG, "onMarkerDragStart: ")

    }

    override fun onMarkerDrag(p0: Marker) {
        Log.d(TAG, "onMarkerDrag: ")
    }

    override fun onMarkerDragEnd(p0: Marker) {
        Log.d(TAG, "onMarkerDragEnd: ")
        val latLng = marker.position
        try{
            val addresses: List<Address> = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if(addresses.size>0){
                val address: Address = addresses[0]
                val streetAddress = address.getAddressLine(0)
                p0.title = streetAddress
                marker.title = streetAddress
            }
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    override fun onMapLongClick(latLng: LatLng) {
        Log.d(TAG, "onMarkerDragEnd: ")
        try{
            val addresses: List<Address> = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if(addresses.size>0){
                val address: Address = addresses[0]
                val streetAddress = address.getAddressLine(0)
                //marker = mMap.addMarker(MarkerOptions().position(latLng).title(streetAddress).draggable(true))
                marker.position = latLng
                marker.title = streetAddress
                marker.isDraggable = true
            }
        }catch(e: IOException){
            e.printStackTrace()
        }
    }


}