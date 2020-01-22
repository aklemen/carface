package com.example.carface;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Switch;

public class MyLogSwitch extends Switch implements View.OnClickListener {
    public MyLogSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }



    //Send log to server when pressed

    @Override
    public void onClick(View v) {
        MainActivity.sendLogToServer(String.valueOf(getResources().getResourceEntryName(v.getId())), "", "false", "");
    }
}
