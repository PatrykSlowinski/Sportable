package com.example.sportable.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.sportable.R
import com.example.sportable.adapters.EventItemsAdapter
import com.example.sportable.adapters.MemberItemsAdapter
import com.example.sportable.firebase.FirestoreClass
import com.example.sportable.models.Event
import com.example.sportable.models.Sport
import com.example.sportable.models.User
import com.example.sportable.utils.Constants
import kotlinx.android.synthetic.main.activity_event_details.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.item_event.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.Duration.Companion.hours

class EventDetailsActivity : BaseActivity() {

    private lateinit var menu: Menu

    private lateinit var mMembersList: ArrayList<String>
    private lateinit var mEvent: Event
    var eventDocumentId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        if(intent.hasExtra(Constants.DOCUMENT_ID)){
            eventDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
            //Toast.makeText(this, eventDocumentId, Toast.LENGTH_SHORT).show()
            //mEvent.documentId = eventDocumentId

        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getEventDetails(this, eventDocumentId)

        tv_location.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra(Constants.LOCATION, mEvent)
            startActivity(intent)
        }


    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_event_details_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "Event details"
        }

        toolbar_event_details_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun eventDetails(event: Event){
        hideProgressDialog()
        setupActionBar()

        //val adapter = EventItemsAdapter(this, event )

        //val userList = User()
        //tv_event_details_sport_name.setText(event.sportId)
        tv_details_created_by.setText("${event.createdBy}")
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val selectedDate = simpleDateFormat.format(Date(event.date))
        tv_date.text = selectedDate
        val simpleTimeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        var selectedTime = simpleTimeFormat.format(Date(event.date))
        tv_time.text = selectedTime
        tv_location.setText(event.location)
        tv_members.setText("Members: " +event.currentNumberOfPeople.toString()+"/"+event.maxPeople +" (min: "+event.minPeople+")")

        mMembersList = event.assignedTo
        mEvent = event
        //Toast.makeText(this, mEvent.documentId, Toast.LENGTH_SHORT).show()
        //Toast.makeText(this, isUserMember().toString(), Toast.LENGTH_SHORT).show()



        FirestoreClass().getMembersList(this, event)


    }

    fun eventSportDetails(sport: Sport){
        Glide
            .with(this)
            .load(sport.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_sport_image)

        tv_event_details_sport_name.text = sport.name
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            this.menu = menu
        }
        menuInflater.inflate(R.menu.menu_leave_event, menu)
        if(isUserMember()==false) if (menu != null) {
            menu.getItem(0).setIcon(R.drawable.ic_vector_add_24dp)
        }
        return super.onCreateOptionsMenu(menu)
    }

    fun populateMembersListToUI(membersList: ArrayList<User>){


        hideProgressDialog()

        rv_members_list.layoutManager = LinearLayoutManager(this)
        rv_members_list.setHasFixedSize(true)

        val adapter = MemberItemsAdapter(this, membersList)
        rv_members_list.adapter = adapter


    }


    fun memberLeavingSuccess(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        //finish()
        FirestoreClass().getEventDetails(this, eventDocumentId)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.action_leave_event -> {
                alertDialogForLeavingEvent()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun alertDialogForLeavingEvent() {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Alert")
        //set message for alert dialog
        if(isUserMember()){
        builder.setMessage( "Are you sure that you want to leave the event?")}
        else builder.setMessage( "Are you sure that you want to join the event?")

        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
            if(isUserMember()) leaveEvent()
            else joinEvent()
        }
        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }

    private fun joinEvent() {
        mMembersList.add(getCurrentUserID())
        mEvent.assignedTo = mMembersList
        mEvent.currentNumberOfPeople += 1
        //showProgressDialog("Please Wait...")
        FirestoreClass().updateMemberList(this, mEvent)
    }
    fun leaveEvent(){
        if(mMembersList.size ==0) Toast.makeText(this, "puste", Toast.LENGTH_LONG).show()
        Toast.makeText(this, mMembersList.size.toString(), Toast.LENGTH_LONG).show()
        mMembersList.remove(getCurrentUserID())



        mEvent.assignedTo = mMembersList
        mEvent.currentNumberOfPeople -= 1

        //Toast.makeText(this, mEvent.assignedTo.size.toString(), Toast.LENGTH_SHORT).show()

        /*for(s in mEvent.assignedTo){
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
        }*/
        //showProgressDialog("Please Wait...")
        FirestoreClass().updateMemberList(this, mEvent)
    }

    private fun isUserMember(): Boolean{
        return FirestoreClass().getCurrentUserID() in mMembersList
        }

}