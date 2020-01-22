package com.example.carface;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MyLogRadioButton extends android.support.v7.widget.AppCompatRadioButton implements View.OnTouchListener {

    public MyLogRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
    }



    //Send log to server when pressed

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            MainActivity.sendLogToServer(String.valueOf(getResources().getResourceEntryName(v.getId())), "", "false", "");
        }
        return false;
    }

}
