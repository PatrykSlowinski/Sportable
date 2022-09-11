package com.example.sportable.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.sportable.R
import com.example.sportable.firebase.FirestoreClass
import com.example.sportable.models.Address
import com.example.sportable.models.User
import com.example.sportable.utils.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {
    var userId : String = ""
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setupActionBar()

        if (!Places.isInitialized()) {
            Places.initialize(
                this,
                resources.getString(R.string.google_maps_api_key)
            )
        }

        et_select_address.setOnClickListener {
            setAddress()
        }

        btn_sign_up.setOnClickListener {
            FirestoreClass().checkIfLoginIsAlreadyInUse(this, et_login.text.toString().trim{ it<= ' '})
        }
    }

    private fun setAddress() {
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

    fun userRegistered(){
        val address = Address()
        address.userId = userId
        address.location = et_select_address.text.toString()
        address.latitude = mLatitude
        address.longitude = mLongitude

        FirestoreClass().addAddress(this, address )
    }

    fun userRegisteredSuccess(){
        Toast.makeText(
            this, "You have" +
                    " succesfully registered", Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_sign_up_activity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_sign_up_activity.setNavigationOnClickListener { onBackPressed() }

    }

    fun registerUser(){
        val name: String = et_name.text.toString().trim{ it<= ' '}
        val email: String = et_email.text.toString().trim{ it<= ' '}
        val login: String = et_login.text.toString().trim{ it<= ' '}
        val password: String = et_password.text.toString().trim{ it<= ' '}
        val address: String = et_select_address.text.toString().trim{ it<= ' '}
        if(validateForm(name, email, login, password, address)){

            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showProgressDialog(resources.getString(R.string.please_wait))
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail,login)
                        userId = firebaseUser.uid
                        FirestoreClass().registerUser(this, user)
                    } else {
                        showErrorSnackBar(task.exception?.message.toString())
                    }
                }
        }

    }

    private fun validateForm(name: String, email: String, login: String, password: String, address:String): Boolean {
        return when{

            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter an email adress")
                false
            }
            TextUtils.isEmpty(login)->{
                showErrorSnackBar("Please enter a login")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a password")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a password")
                false
            }
            TextUtils.isEmpty(address)->{
                showErrorSnackBar("Please enter an address")
                false
            }
            else -> {
                true
            }
        }
    }



    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE) {

                try {
                    val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                    et_select_address.setText(place.address)
                    mLatitude = place.latLng!!.latitude
                    mLongitude = place.latLng!!.longitude
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Cancelled", "Cancelled")
        }
    }
}