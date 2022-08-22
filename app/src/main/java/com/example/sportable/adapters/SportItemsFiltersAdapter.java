package com.example.sportable.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sportable.R;
import com.example.sportable.models.Sport;

import java.util.List;

public class SportItemsFiltersAdapter extends BaseAdapter {
    private Context context;
    private List<Sport> sportList;

    public SportItemsFiltersAdapter(Context context, List<Sport> sportList){
        this.context = context;
        this.sportList = sportList;
    }

    @Override
    public int getCount() {
        return sportList != null ? sportList.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rootView = LayoutInflater.from(context)
                .inflate(R.layout.item_sport, viewGroup, false);

        TextView txtName = rootView.findViewById(R.id.tv_name);
        ImageView image = rootView.findViewById(R.id.iv_sport_image);

        txtName.setText(sportList.get(i).getName());
        Glide
                .with(context)
                .load(sportList.get(i).getImage())
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(image);

        return rootView;
    }
}
