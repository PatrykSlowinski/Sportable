package com.example.sportable.adapters

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sportable.R
import com.example.sportable.activities.BaseActivity
import com.example.sportable.activities.BaseActivity.Companion.mSportsList
import com.example.sportable.activities.MyEventsActivity
import com.example.sportable.firebase.FirestoreClass
import com.example.sportable.models.Event
import com.example.sportable.models.Sport
import kotlinx.android.synthetic.main.activity_add_sport.*
import kotlinx.android.synthetic.main.activity_create_event.view.*
import kotlinx.android.synthetic.main.activity_event_details.view.*
import kotlinx.android.synthetic.main.item_event.view.*
import kotlinx.android.synthetic.main.item_member.view.*
import kotlinx.android.synthetic.main.item_sport.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

open class EventItemsAdapter(
    private val context: Context,
                             private var list: ArrayList<Event>
):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_event, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var imageURL = ""
        var eventSportName = ""
        val model = list[position]
        for(sport in mSportsList){
            if( model.sportId == sport.documentId){
                imageURL = sport.image
                eventSportName = sport.name
            }
        }

        if(holder is MyViewHolder){
                Glide
                    .with(context)
                    .load(imageURL)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(holder.itemView.iv_event_image)


            //if(position == list.size - 1){
                //for myEventsActivity
                holder.itemView.tv_sport_name.text = eventSportName
                holder.itemView.tv_created_by.text = "Created by: ${model.createdBy}"
                holder.itemView.tv_max_people.text = model.maxPeople.toString()

            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(model.date))
            holder.itemView.tv_event_date.text = selectedDate

                //for EventDetailsActivity

                holder.itemView.setOnClickListener{
                    if(onClickListener != null){
                        onClickListener!!.onClick(position, model)
                    }
                //}
            }
        }

        }

    override fun getItemCount(): Int {
        return list.size
    }
    private fun Int.toDp(): Int =
        (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()



    interface OnClickListener{
        fun onClick(position: Int, model: Event)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)



}