package com.example.sportable.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportable.R
import com.example.sportable.adapters.SportItemsAdapter
import com.example.sportable.firebase.FirestoreClass
import com.example.sportable.models.Sport
import com.example.sportable.utils.Constants
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.activity_sports.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.app_bar_main.view.*
import kotlinx.android.synthetic.main.app_bar_sports.*
import kotlinx.android.synthetic.main.sports_content.*

class SportsActivity : BaseActivity() {

    companion object{
        const val ADD_SPORT_REQUEST_CODE = 12
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sports)

        setupActionBar()

        updateSportsList()

        fab_add_sport.setOnClickListener{
            val intent = Intent(this, AddSportActivity::class.java)
            startActivityForResult(intent, ADD_SPORT_REQUEST_CODE)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MainActivity.MY_PROFILE_REQUEST_CODE) {
            FirestoreClass().loadUserData(this)
        }else if(resultCode == Activity.RESULT_OK && requestCode == ADD_SPORT_REQUEST_CODE){
            FirestoreClass().getSportsList(this)
        }else{
            Log.e("Cancelled", "Cancelled")
        }
    }
    fun populateSportsListToUI(sportsList: ArrayList<Sport>){
        hideProgressDialog()
        sportsList.sortBy { it.name }

        if(sportsList.size > 0){
            rv_sports_list.visibility = View.VISIBLE
            tv_no_sports.visibility = View.GONE

            rv_sports_list.layoutManager = LinearLayoutManager(this)
            rv_sports_list.setHasFixedSize(true)

            val adapter = SportItemsAdapter(this, sportsList)
            rv_sports_list.adapter = adapter

        }else{
            rv_sports_list.visibility = View.GONE
            tv_no_sports.visibility = View.VISIBLE
        }

    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_sports_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString((R.string.sports))
        }

        toolbar_sports_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun updateSportsList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getSportsList(this)

    }
}