package com.example.sportable.activities

import android.app.Activity
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.example.sportable.R
import com.example.sportable.firebase.FirestoreClass
import com.example.sportable.models.User
import com.example.sportable.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object{
        const val MY_PROFILE_REQUEST_CODE : Int = 11
    }
    private lateinit var mUserLogin: String

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirestoreClass().isUserAdmin(this)
        FirestoreClass().getAllEventsList(this)
        setupActionBar()
        FirestoreClass().getUserAddress(this)
        FirestoreClass().deleteOutdatedEvents(this)
        nav_view.setNavigationItemSelectedListener(this)
        FirestoreClass().loadUserData(this)
        FirestoreClass().getSportsList(this)

        showErrorSnackBar("This app requires internet connection to work properly. Always use it online.")

        fab_create_event.setOnClickListener{
            val intent = Intent(this, CreateEventActivity::class.java)
            intent.putExtra(Constants.LOGIN, mUserLogin)
            startActivity(intent)

        }

        layoutEvents.setOnClickListener {
            val intent = Intent(this, MyEventsActivity::class.java)
            intent.putExtra(Constants.LOGIN, mUserLogin)
            startActivity(intent)

        }

        layoutSports.setOnClickListener {
            startActivity(Intent(this, SportsActivity::class.java))
        }
        layoutQuickSearch.setOnClickListener {
            startActivity(Intent(this, AllEventsActivity::class.java))
        }
        layoutSearchOnMap.setOnClickListener {
            startActivity(Intent(this, SearchOnMapActivity::class.java))
        }

    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer(){
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
            }else{
                    drawer_layout.openDrawer(GravityCompat.START)
            if(!isUserAdmin){
                tv_admin.visibility = View.INVISIBLE
            }
            }
        }

    override fun onBackPressed() {
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }

    fun updateNavigationUserDetails(user: User){

        mUserLogin = user.login

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(nav_user_image);

        tv_username.text = user.login
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)

        return true
    }
}