package com.example.sportable.firebase

import android.app.Activity
import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.sportable.activities.*
import com.example.sportable.activities.BaseActivity.Companion.allEvents
import com.example.sportable.activities.BaseActivity.Companion.isUserAdmin
import com.example.sportable.activities.BaseActivity.Companion.mSportsList
import com.example.sportable.activities.BaseActivity.Companion.userAddress
import com.example.sportable.models.*
import com.example.sportable.utils.Constants
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FirestoreClass {


    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegistered()
            }
            .addOnFailureListener { e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }

    fun createEvent(activity: CreateEventActivity, event: Event) {
        mFireStore.collection(Constants.EVENTS)
            .document()
            .set(event, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Event created successfully.")

                Toast.makeText(activity, "Event created successfully.", Toast.LENGTH_SHORT).show()

                activity.eventCreatedSuccessfully()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating an event.",
                    e
                )
            }
    }

    fun getEventsList(activity: BaseActivity, login: String) {
        mFireStore.collection(Constants.EVENTS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val eventsJoined: ArrayList<Event> = ArrayList()
                val eventsCreatedList: ArrayList<Event> = ArrayList()
                val allEventsMembered: ArrayList<Event> = ArrayList()
                for (i in document.documents) {
                    val event = i.toObject(Event::class.java)!!
                    event.documentId = i.id
                    allEventsMembered.add(event)
                    //Toast.makeText(activity, /*event.createdBy.toString() +"     "+*/getCurrentUserLogin(), Toast.LENGTH_LONG).show()
                    if (event.createdBy.toString() == login) {
                        eventsCreatedList.add(event)
                    } else {
                        eventsJoined.add(event)
                    }

                }
                if (activity is MyEventsActivity) {
                    activity.populateEventsListToUI(eventsCreatedList, eventsJoined)
                }
                if (activity is MyProfileActivity) {
                    activity.deleteAllDeletedUserEventMemberships(allEventsMembered)
                }
            }.addOnFailureListener { e ->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while showing events! ")
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getAllEventsList(activity: BaseActivity) {
        val calendar = Calendar.getInstance()
        mFireStore.collection(Constants.EVENTS)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val eventsList: ArrayList<Event> = ArrayList()
                for (i in document.documents) {
                    val event = i.toObject(Event::class.java)!!
                    event.documentId = i.id
                    if((event.currentNumberOfPeople>=event.minPeople && event.date > calendar.timeInMillis) || ((event.currentNumberOfPeople<event.minPeople) &&(event.date > (calendar.timeInMillis + 120*60*1000)))) {
                        eventsList.add(event)
                    }
                }

                if(activity is MainActivity){
                    allEvents = eventsList
                }

                if (activity is AllEventsActivity) {
                    allEvents = eventsList
                    activity.populateEventsListToUI(eventsList)
                }
                if(activity is SearchOnMapActivity){
                    allEvents = eventsList
                    activity.setMarkers(eventsList)
                }
            }.addOnFailureListener { e ->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while showing events! ")
            }
    }


    fun addSport(activity: AddSportActivity, sport: Sport) {
        mFireStore.collection(Constants.SPORTS)
            .document()
            .set(sport, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Sport added successfully.")

                Toast.makeText(activity, "Sport added successfully.", Toast.LENGTH_SHORT).show()

                activity.sportAddedSuccessfully()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while adding a sport.",
                    e
                )
            }
    }

    fun getSportsList(activity: BaseActivity) {
        mFireStore.collection(Constants.SPORTS)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val sportList: ArrayList<Sport> = ArrayList()
                for (i in document.documents) {
                    val sport = i.toObject(Sport::class.java)!!
                    sport.documentId = i.id
                    sportList.add(sport)
                }
                if (activity is MyEventsActivity) {
                    mSportsList = sportList
                }
                if (activity is MainActivity) {
                    mSportsList = sportList
                }

                if(activity is SearchOnMapActivity){
                    mSportsList = sportList
                }
                if (activity is SportsActivity) {
                    activity.populateSportsListToUI(sportList)
                }
                if (activity is CreateEventActivity) {
                    activity.sportsListDialog(sportList)
                }

            }.addOnFailureListener { e ->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while showing sports! ")
            }
    }

    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Profile Data updated successfully!")

                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                activity.profileUpdateSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error when updating the profile!",
                    e
                )
                Toast.makeText(activity, "Error when updating the profile!", Toast.LENGTH_LONG)
                    .show()
            }
    }

    fun getCurrentUserID(): String {
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getCurrentUserLogin(): String {
        var currentUserLogin = ""
        mFireStore.collection(Constants.USERS)
            .whereEqualTo("id", getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                val user = document.documents[0].toObject(User::class.java)!!
                currentUserLogin = user.login
            }

        return currentUserLogin


    }

    fun loadUserData(activity: Activity) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!

                when (activity) {
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser)

                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser)
                    }
                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                }

            }
            .addOnFailureListener { e ->

                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()

                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting loggedIn user details",
                    e
                )
            }
    }

    fun getUserAddress(activity: BaseActivity){
        mFireStore.collection(Constants.ADDRESS)
            .whereEqualTo("userId", getCurrentUserID())
            .get()
            .addOnSuccessListener {
                document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                val address: Address = document.documents[0].toObject(Address::class.java)!!
                if(activity is MainActivity) {
                    userAddress = address
                }

            }.addOnFailureListener { e ->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while trying to get user address! ")
            }
    }

    fun getEventDetails(activity: EventDetailsActivity, documentId: String) {
        mFireStore.collection(Constants.EVENTS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                val event: Event = document.toObject(Event::class.java)!!
                //activity.eventDetails(document.toObject(Event::class.java)!!)
                event.documentId = documentId
                activity.eventDetails(event)
                getSportDetails(activity, event.sportId)

            }.addOnFailureListener { e ->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while showing events! ")
            }
    }

    fun getMembersList(activity: EventDetailsActivity, event: Event) {
        mFireStore.collection(Constants.USERS)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val members: ArrayList<User> = ArrayList()
                for (i in document.documents) {
                    val user = i.toObject(User::class.java)!!
                    if (event.assignedTo.contains(user.id)) {
                        members.add(user)
                    }
                }

                activity.populateMembersListToUI(members)
            }.addOnFailureListener { e ->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while showing events! ")
            }
    }

    fun updateMemberList(activity: BaseActivity, event: Event) {


        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = event.assignedTo
        assignedToHashMap["currentNumberOfPeople"] = event.currentNumberOfPeople



        mFireStore.collection(Constants.EVENTS)
            .document(event.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")
                if (activity is EventDetailsActivity) {
                    activity.memberLeavingSuccess()
                }

            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    fun getSportDetails(activity: BaseActivity, sportId: String) {
        mFireStore.collection(Constants.SPORTS)
            .document(sportId)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                val sport: Sport = document.toObject(Sport::class.java)!!
                //activity.eventDetails(document.toObject(Event::class.java)!!)
                if (activity is EventDetailsActivity) {
                    activity.eventSportDetails(sport)
                }


            }.addOnFailureListener { e ->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while showing events! ")
            }
    }

    fun saveFilters(activity: AllEventsActivity, lastFilters: LastFilters) {

        mFireStore.collection(Constants.LASTFILTERS)
            .document()
            .set(lastFilters, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Last filters added successfully.")

                Toast.makeText(activity, "Last filters added successfully.", Toast.LENGTH_SHORT)
                    .show()

                activity.lastFiltersAddedSuccessfully()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while adding a sport.",
                    e
                )
            }
    }

    fun deleteUserAccount(activity: MyProfileActivity) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .delete()
            .addOnSuccessListener {
                FirebaseAuth.getInstance().currentUser!!.delete()
                activity.userDeletedSuccessfully()
            }
            .addOnFailureListener { e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error deleting account",
                    e
                )
            }
    }

    fun addAddress(activity: BaseActivity, address: Address){
        mFireStore.collection(Constants.ADDRESS)
            .document()
            .set(address, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Address added successfully.")

                Toast.makeText(activity, "Address added successfully.", Toast.LENGTH_SHORT).show()

                if(activity is SignUpActivity) {
                    activity.userRegisteredSuccess()
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while adding a sport.",
                    e
                )
            }
    }

    fun addSportProposition(activity: AddSportActivity, sportProposition: SportProposition){
        mFireStore.collection(Constants.SPORTPROPOSITION)
            .document()
            .set(sportProposition , SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Sport proposition sent successfully.")

                Toast.makeText(activity, "Proposition sent. Thank you.", Toast.LENGTH_SHORT).show()

                if(activity is AddSportActivity) {
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while adding a sport.",
                    e
                )
            }
    }

    fun isUserAdmin(activity: MainActivity) {
        mFireStore.collection(Constants.USERS)
            .whereEqualTo("id", getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, "User status is set.")
                val User: User = document.documents[0].toObject(User::class.java)!!
                if(User.admin){
                    isUserAdmin = true
                }

            }
    }

    fun deleteOutdatedEvents(activity:MainActivity){
        val calendar = Calendar.getInstance()
        var itemsRef: CollectionReference = mFireStore.collection(Constants.EVENTS)
        var query : Query = itemsRef.whereLessThanOrEqualTo("date", calendar.timeInMillis )
        query.get().addOnCompleteListener(OnCompleteListener<QuerySnapshot>(){ task ->
            if(task.isSuccessful){
                for(document: DocumentSnapshot in task.result){
                    val event: Event = document.toObject(Event::class.java)!!
                    if((event.date + event.duration*60*1000)<=calendar.timeInMillis){
                        itemsRef.document(document.id).delete()
                    }
                }
            }else{
                Log.d(TAG, "Error getting documents: ", task.exception)
            }


        })
    }




}