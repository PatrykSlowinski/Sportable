package com.example.sportable.adapters

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sportable.R
import com.example.sportable.activities.BaseActivity
import com.example.sportable.models.Event
import kotlinx.android.synthetic.main.item_event.view.*
import kotlinx.android.synthetic.main.item_event.view.iv_event_image
import kotlinx.android.synthetic.main.item_event.view.tv_created_by
import kotlinx.android.synthetic.main.item_event.view.tv_max_people
import kotlinx.android.synthetic.main.item_event.view.tv_sport_name
import kotlinx.android.synthetic.main.item_my_event.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

open class MyEventItemsAdapter (private val context: Context,
                                private var list: ArrayList<Event>
):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_my_event, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var imageURL = ""
        var eventSportName = ""
        val model = list[position]
        for(sport in BaseActivity.mSportsList){
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
            holder.itemView.tv_my_event_date.text = selectedDate

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