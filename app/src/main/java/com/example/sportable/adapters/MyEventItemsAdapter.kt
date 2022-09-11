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
        val calendar = Calendar.getInstance()
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

            if(model.date<(calendar.timeInMillis +120*60*1000) && model.currentNumberOfPeople<model.minPeople){
                holder.itemView.tv_cancelled_event.visibility = View.VISIBLE
            }

            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val simpleTimeFormat = SimpleDateFormat("HH:mm")
            val selectedDate = simpleDateFormat.format(Date(model.date))
            val selectedTime = simpleTimeFormat.format(Date(model.date))
            holder.itemView.tv_my_event_date.text = selectedDate
            holder.itemView.tv_Time.text = selectedTime
            holder.itemView.tv_myEvent_duration.text = "Duration(minutes): " + model.duration.toString()
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