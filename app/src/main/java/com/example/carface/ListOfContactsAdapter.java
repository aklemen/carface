package com.example.carface;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class ListOfContactsAdapter extends RecyclerView.Adapter<ListOfContactsAdapter.MyViewHolder> {


    private List<Contact> listOfContacts;
    private PhoneFragment fragment;
    private Context context;


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView itemName;
        TextView itemNumber;
        LinearLayout itemLayout;

        public MyViewHolder(View v) {
            super(v);
            itemName = v.findViewById(R.id.item_contact_name);
            itemNumber = v.findViewById(R.id.item_contact_number);
            itemLayout = v.findViewById(R.id.item_contact_layout);
        }
    }

    public ListOfContactsAdapter(List<Contact> listOfContacts, PhoneFragment fragment) {
        this.listOfContacts = listOfContacts;
        this.fragment = fragment;
    }


    @Override
    public ListOfContactsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Contact c = listOfContacts.get(position);

        holder.itemName.setText(c.getContactName());
        holder.itemNumber.setText(c.getContactNumber());

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.fillNumber(c.getId());
                Log.d("INDEX", String.valueOf(c.getId()));
                Log.d("KONTAKT", String.valueOf(c.getContactName()));
                Log.d("CIFRA", String.valueOf(c.getContactNumber()));
                fragment.slideUpList.hide();
            }
        });

    }


    @Override
    public int getItemCount() {
        return listOfContacts.size();
    }

    public void filterList (List<Contact> filteredList){
        listOfContacts = filteredList;
        notifyDataSetChanged();
    }

    public List<Contact> getListOfContacts(){
        return listOfContacts;
    }
}