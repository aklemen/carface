package com.example.carface;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class ListOfStationsAdapter extends RecyclerView.Adapter<ListOfStationsAdapter.MyViewHolder> {


    private List<RadioStation> listOfStations;
    private FMRadioFragment fragment;
    private Context context;


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView itemFrequency;
        TextView itemName;
        LinearLayout itemLayout;

        public MyViewHolder(View v) {
            super(v);
            itemFrequency = v.findViewById(R.id.item_station_frequency);
            itemName = v.findViewById(R.id.item_station_name);
            itemLayout = v.findViewById(R.id.item_station_layout);
        }
    }

    public ListOfStationsAdapter(List<RadioStation> listOfStations, FMRadioFragment fragment) {
        this.listOfStations = listOfStations;
        this.fragment = fragment;
    }


    @Override
    public ListOfStationsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_station, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final RadioStation r = listOfStations.get(position);

        holder.itemFrequency.setText(r.getStationFrequency());
        holder.itemName.setText(r.getStationName());

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.changeStation(r.getStationFrequency());
                fragment.slideUpList.hide();
            }
        });

    }


    @Override
    public int getItemCount() {
        return listOfStations.size();
    }

    public void filterList (List<RadioStation> filteredList){
        listOfStations = filteredList;
        notifyDataSetChanged();
    }

    public List<RadioStation> getListOfStations(){
        return listOfStations;
    }
}