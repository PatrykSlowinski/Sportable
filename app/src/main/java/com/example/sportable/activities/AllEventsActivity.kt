package com.example.sportable.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Spinner
import android.widget.Toast
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
import kotlinx.android.synthetic.main.activity_all_events.*
import kotlinx.android.synthetic.main.activity_main.*
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
    private var sportsToString = mSportsList.map { it.name }.toTypedArray()
    private var sportList= ArrayList<Int>(sportsToString.size)
    private var selectedSport= BooleanArray(sportsToString.size)
    private lateinit var mUserLogin: String
    companion object{
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_events)



        if (intent.hasExtra(Constants.LOGIN)) {
            mUserLogin= intent.getStringExtra(Constants.LOGIN)!!
        }

        setupActionBar()

        updateEventsList()


        iv_manual_search.setOnClickListener {
            toggleDrawer()
        }

        tv_sport_filter.setOnClickListener {
            dropDownListSportsFilters()
        }

        /*spinner_sports = findViewById(R.id.spinner_sports)
        val adapter = SportItemsFiltersAdapter(this, mSportsList)
        spinner_sports.adapter = adapter*/



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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MainActivity.MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getAllEventsList(this)
        }
        else{
            Log.e("Cancelled", "Cancelled")
        }
    }
    fun populateEventsListToUI(eventsList: ArrayList<Event>){
        hideProgressDialog()



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
    fun updateEventsList(){
        showProgressDialog("Please Wait...")
        FirestoreClass().getAllEventsList(this)
    }

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
            reloadFilters()
        })

        builder.setNegativeButton("Select All", DialogInterface.OnClickListener(){
            //dialogInterface, i ->
            //dialogInterface.dismiss()
                dialogInterface, i ->
            for(j in selectedSport.indices){
                selectedSport[j] = true
            }
            reloadFilters()
        })
        builder.setNeutralButton("Select All", DialogInterface.OnClickListener(){
                dialogInterface, i ->
            for(j in selectedSport.indices){
                selectedSport[j] = true
            }
            reloadFilters()
        })

        builder.setNeutralButton("Clear All", DialogInterface.OnClickListener(){
            dialogInterface, i ->
            for(j in selectedSport.indices){
                selectedSport[j] = false
                sportList.clear()
            }

            reloadFilters()
        })

        builder.show()
    }

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


}