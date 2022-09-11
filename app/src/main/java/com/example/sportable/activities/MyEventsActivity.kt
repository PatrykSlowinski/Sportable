package com.example.sportable.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportable.R
import com.example.sportable.adapters.EventItemsAdapter
import com.example.sportable.adapters.MyEventItemsAdapter
import com.example.sportable.firebase.FirestoreClass
import com.example.sportable.models.Event
import com.example.sportable.models.Sport
import com.example.sportable.utils.Constants
import kotlinx.android.synthetic.main.app_bar_my_events.*
import kotlinx.android.synthetic.main.my_events_content.*

class MyEventsActivity : BaseActivity() {


    private lateinit var mUserLogin: String
    companion object{
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_events)
        setupActionBar()

        if (intent.hasExtra(Constants.LOGIN)) {
            mUserLogin= intent.getStringExtra(Constants.LOGIN)!!
        }

        FirestoreClass().getSportsList(this)

        updateEventsList()

    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_my_events)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString((R.string.my_events))
        }

        toolbar_my_events.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MainActivity.MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getEventsList(this, mUserLogin)
        }
        else{
            Log.e("Cancelled", "Cancelled")
        }
    }
    fun populateEventsListToUI(eventsCreatedByList: ArrayList<Event>, eventsJoinedList: ArrayList<Event>){
        hideProgressDialog()



        if(eventsCreatedByList.size > 0){

            eventsCreatedByList.sortBy { it.date }

            cv_my_events_created.visibility = View.VISIBLE

            var layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL

            rv_my_events_created_by_me.layoutManager = layoutManager
            rv_my_events_created_by_me.setHasFixedSize(true)


            val adapter = MyEventItemsAdapter(this, eventsCreatedByList)
            rv_my_events_created_by_me.adapter = adapter

            adapter.setOnClickListener(object : MyEventItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Event) {
                    val intent = Intent(this@MyEventsActivity, EventDetailsActivity::class.java)
                    intent.putExtra(
                        Constants.DOCUMENT_ID, model.documentId)
                    startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
                }
            })
        }else{
            cv_my_events_created.visibility = View.INVISIBLE
            val params = cv_my_events_joined.layoutParams as ConstraintLayout.LayoutParams
            params.topToTop= cl_my_events.id
            cv_my_events_joined.requestLayout()
        }

        if(eventsJoinedList.size > 0){
            eventsJoinedList.sortBy { it.date }
            cv_my_events_joined.visibility = View.VISIBLE

            var layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL

            rv_my_events_joined.layoutManager = layoutManager
            rv_my_events_joined.setHasFixedSize(true)

            val adapter = MyEventItemsAdapter(this, eventsJoinedList)
            rv_my_events_joined.adapter = adapter



            adapter.setOnClickListener(object : MyEventItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Event) {
                    val intent = Intent(this@MyEventsActivity, EventDetailsActivity::class.java)
                    intent.putExtra(
                        Constants.DOCUMENT_ID, model.documentId)
                   startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
                }
            })
        }else{
            cv_my_events_joined.visibility = View.INVISIBLE
        }

        if(eventsJoinedList.size <= 0 && eventsCreatedByList.size <= 0) tv_no_events.visibility = View.VISIBLE

    }

    fun updateEventsList(){
        showProgressDialog("Please Wait...")
        FirestoreClass().getEventsList(this, mUserLogin)
    }



}