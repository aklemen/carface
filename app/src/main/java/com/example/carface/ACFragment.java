package com.example.carface;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

public class ACFragment extends Fragment {

//region Variables


    private MyLogImageButton imageButtonFan;
    private int fanLevel;
    private MyLogImageButton plusFan;
    private MyLogImageButton minusFan;

    private MyLogImageButton plusLeft;
    private MyLogImageButton minusLeft;
    private MyLogImageButton plusRight;
    private MyLogImageButton minusRight;
    private TextView leftTemperature;
    private TextView rightTemperature;

    private MyLogSwitch switchFront;
    private MyLogSwitch switchCirculation;
    private MyLogSwitch switchRear;

    private MyLogSwitch switchLeftSeat;
    private MyLogSwitch switchRightSeat;

    private MyLogSwitch switchWindow;
    private MyLogSwitch switchHead;
    private MyLogSwitch switchLegs;


//endregion


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ac, container, false);


        //Initialize views

        imageButtonFan = view.findViewById(R.id.button_fan);
        plusFan = view.findViewById(R.id.button_plusFan);
        minusFan = view.findViewById(R.id.button_minusFan);

        plusLeft = view.findViewById(R.id.button_plusLeft);
        minusLeft = view.findViewById(R.id.button_minusLeft);
        plusRight = view.findViewById(R.id.button_plusRight);
        minusRight = view.findViewById(R.id.button_minusRight);

        leftTemperature = view.findViewById(R.id.text_leftTemperature);
        rightTemperature = view.findViewById(R.id.text_rightTemperature);

        switchFront = view.findViewById(R.id.switch_front);
        switchCirculation = view.findViewById(R.id.switch_circulation);
        switchRear = view.findViewById(R.id.switch_rear);

        switchLeftSeat = view.findViewById(R.id.switch_leftSeat);
        switchRightSeat = view.findViewById(R.id.switch_rightSeat);

        switchWindow = view.findViewById(R.id.switch_window);
        switchHead = view.findViewById(R.id.switch_head);
        switchLegs = view.findViewById(R.id.switch_legs);

        //Set fan level to 0 as default

        fanLevel = 0;


        //Set listeners for buttons

        plusFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fanLevel++;

                if (fanLevel > 4){
                    fanLevel = 4;
                }
                else {
                    setFanImage(fanLevel);
                }
            }
        });

        minusFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fanLevel--;

                if (fanLevel < 0){
                    fanLevel = 0;
                }
                else {
                    setFanImage(fanLevel);
                }
            }
        });


        plusLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float temp = Float.valueOf(leftTemperature.getText().toString());

                if (temp < 25.0f ){
                    temp = temp + 0.5f;
                    leftTemperature.setText(String.valueOf(temp));
                }
            }
        });

        minusLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float temp = Float.valueOf(leftTemperature.getText().toString());

                if (temp > 16.0f ){
                    temp = temp - 0.5f;
                    leftTemperature.setText(String.valueOf(temp));
                }
            }
        });

        plusRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float temp = Float.valueOf(rightTemperature.getText().toString());

                if (temp < 25.0f ){
                    temp = temp + 0.5f;
                    rightTemperature.setText(String.valueOf(temp));
                }
            }
        });

        minusRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float temp = Float.valueOf(rightTemperature.getText().toString());

                if (temp > 16.0f ){
                    temp = temp - 0.5f;
                    rightTemperature.setText(String.valueOf(temp));
                }
            }
        });

        switchFront.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(isChecked),
                    "ACFragment",
                        "window_front",
                        "Front window heating was set.");

            }
        });

        switchCirculation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(isChecked),
                    "ACFragment",
                        "circulation",
                        "Circulation was set.");

            }
        });

        switchRear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(isChecked),
                    "ACFragment",
                        "window_rear",
                        "Rear window heating was set.");

            }
        });

        switchLeftSeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(isChecked),
                    "ACFragment",
                        "seat_left",
                        "Left seat heating was set.");

            }
        });

        switchRightSeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(isChecked),
                    "ACFragment",
                        "seat_right",
                        "Right seat heating was set.");
            }
        });

        switchWindow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(isChecked),
                    "ACFragment",
                        "blow_window",
                        "Blowing to the window was set.");

            }
        });

        switchHead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(isChecked),
                    "ACFragment",
                        "blow_head",
                        "Blowing to the head was set.");

            }
        });

        switchLegs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(isChecked),
                    "ACFragment",
                        "blow_legs",
                        "Blowing to the legs was set.");

            }
        });

        leftTemperature.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                ((MainActivity)getActivity()).isViewSetToTarget(s.toString(),"ACFragment",
                        "temperature_left", "Left " +
                        "temperature was set.");

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        rightTemperature.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                ((MainActivity)getActivity()).isViewSetToTarget(s.toString(),"ACFragment",
                        "temperature_right",
                        "Right" +
                        " " +
                        "temperature was set.");

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return view;

    }



    //Set image of the fan to desired level

    private void setFanImage(int level){
        switch (level){
            case 0:
                imageButtonFan.setImageResource(R.drawable.ic_fan_zero);
                break;
            case 1:
                imageButtonFan.setImageResource(R.drawable.ic_fan_one);
                break;
            case 2:
                imageButtonFan.setImageResource(R.drawable.ic_fan_two);
                break;
            case 3:
                imageButtonFan.setImageResource(R.drawable.ic_fan_three);
                break;
            case 4:
                imageButtonFan.setImageResource(R.drawable.ic_fan_four);
                break;
        }

        ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(level),"ACFragment",
                "fan_level","Fan level was set.");

    }




    public void initACFragment() {
        String property = MainActivity.taskObject.getProperty();
        String initAC = MainActivity.taskObject.getInit().toLowerCase();

        switch (property) {
            case "temperature_left":
                leftTemperature.setText(initAC);
                break;
            case "temperature_right":
                rightTemperature.setText(initAC);
                break;
            case "fan_level":
                fanLevel = Integer.valueOf(initAC);
                setFanImage(fanLevel);
                break;
            case "window_front":
                switchFront.setChecked(Boolean.valueOf(initAC));
                break;
            case "circulation":
                switchCirculation.setChecked(Boolean.valueOf(initAC));
                break;
            case "window_rear":
                switchRear.setChecked(Boolean.valueOf(initAC));
                break;
            case "seat_left":
                switchLeftSeat.setChecked(Boolean.valueOf(initAC));
                break;
            case "seat_right":
                switchRightSeat.setChecked(Boolean.valueOf(initAC));
                break;
            case "blow_window":
                switchWindow.setChecked(Boolean.valueOf(initAC));
                break;
            case "blow_head":
                switchHead.setChecked(Boolean.valueOf(initAC));
                break;
            case "blow_legs":
                switchLegs.setChecked(Boolean.valueOf(initAC));
                break;
        }

        MainActivity.fragmentInitialized = true;
    }
}