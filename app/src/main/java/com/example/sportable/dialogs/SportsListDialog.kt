package com.example.sportable.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportable.R
import com.example.sportable.adapters.SportItemsAdapter
import com.example.sportable.models.Sport
import kotlinx.android.synthetic.main.dialog_list.view.*

abstract class SportsListDialog(
    context: Context,
    private var list: ArrayList<Sport>,
    private var title: String = ""
) : Dialog(context) {

    private var adapter: SportItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {
        view.tvTitle.text = title

        if (list.size > 0) {

            view.rvList.layoutManager = LinearLayoutManager(context)
            adapter = SportItemsAdapter(context, list)
            view.rvList.adapter = adapter

            adapter!!.setOnClickListener(object :
                SportItemsAdapter.OnClickListener {
                override fun onClick(position: Int, sport: Sport) {
                    dismiss()
                    onItemSelected(sport)

                }
            })
        }
    }

    protected abstract fun onItemSelected(sport: Sport)
}