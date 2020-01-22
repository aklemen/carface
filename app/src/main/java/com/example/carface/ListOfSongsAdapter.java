package com.example.carface;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class ListOfSongsAdapter extends RecyclerView.Adapter<ListOfSongsAdapter.MyViewHolder> {


    private List<Song> listOfSongs;
    private USBRadioFragment fragment;
    private Context context;


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView itemName;
        TextView itemArtist;
        LinearLayout itemLayout;

        public MyViewHolder(View v) {
            super(v);
            itemName = v.findViewById(R.id.item_song_name);
            itemArtist = v.findViewById(R.id.item_song_artist);
            itemLayout = v.findViewById(R.id.item_song_layout);
        }
    }

    public ListOfSongsAdapter(List<Song> listOfSongs, USBRadioFragment fragment) {
        this.listOfSongs = listOfSongs;
        this.fragment = fragment;
    }


    @Override
    public ListOfSongsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Song s = listOfSongs.get(position);

        holder.itemName.setText(s.getSongName());
        holder.itemArtist.setText(s.getSongArtist());

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.changeSong(s.getId());
                fragment.slideUpList.hide();
            }
        });

    }


    @Override
    public int getItemCount() {
        return listOfSongs.size();
    }

    public void filterList (List<Song> filteredList){
        listOfSongs = filteredList;
        notifyDataSetChanged();
    }

    public List<Song> getListOfSongs(){
        return listOfSongs;
    }
}