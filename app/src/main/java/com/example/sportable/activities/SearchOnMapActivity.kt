package com.example.sportable.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.sportable.R
import com.example.sportable.firebase.FirestoreClass
import com.example.sportable.models.Event
import com.example.sportable.utils.Constants
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_search_on_map.*


class SearchOnMapActivity : BaseActivity(), OnMapReadyCallback {
    val map = mutableMapOf<Marker,Event>()
    private var markers: MutableMap<Marker, Event> = map
    private val pERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var mMap: GoogleMap
    var currentLocation: com.google.android.gms.maps.model.LatLng =
        com.google.android.gms.maps.model.LatLng(0.0, 0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_on_map)

        setupActionBar()

        val ai: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        val apiKey = value.toString()

        val supportMapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.mapSearchOnMap) as SupportMapFragment
        supportMapFragment.getMapAsync(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        currentLocSearchOnMap.setOnClickListener {
            getCurrentLocationMarker()
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_search_on_map)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "Search On Map"
        }

        toolbar_search_on_map.setNavigationOnClickListener { onBackPressed() }


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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val position = com.google.android.gms.maps.model.LatLng(0.0, 0.0)
        FirestoreClass().getAllEventsList(this)

        googleMap.setOnInfoWindowClickListener(GoogleMap.OnInfoWindowClickListener()  { marker ->
            val event: Event = markers.getValue(marker)
            markerOnClick(event)
            false
        })
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

    private fun markerOnClick(event: Event) {
        val intent = Intent(this, EventDetailsActivity::class.java)
        intent.putExtra(Constants.DOCUMENT_ID, event.documentId)
        startActivity(intent)
    }

    fun setMarkers(eventsList: ArrayList<Event>) {

        for (event in eventsList) {
            var imagePath: String = ""

            for (sport in mSportsList) {
                if (sport.documentId == event.sportId) {
                    imagePath = sport.image
                }
                var bitmapFinal: Bitmap?

                Glide.with(this)
                    .asBitmap()
                    .load(imagePath)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                        ) {

                            bitmapFinal =
                            Bitmap.createScaledBitmap(resource, 150, 150, false) //here we will insert the bitmap we got with the link in a placehold with white border.

                            val marker = mMap.addMarker(
                                MarkerOptions().position(
                                    com.google.android.gms.maps.model.LatLng(
                                        event.latitude,
                                        event.longitude
                                    )
                                ).title("Check details").icon(
                                    //BitmapDescriptorFactory.fromBitmap(R.drawable.iconlocation
                                    BitmapDescriptorFactory.fromBitmap(bitmapFinal)))
                                markers.put(marker, event)



                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            mMap.clear()
                        }


                    })


            }
        }
    }


}