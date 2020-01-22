package com.example.carface;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;

public class DriveSettingsFragment extends Fragment {


    //region Variables


    //Radiogroup for mode buttons, current checked radiobutton, switches

    private RadioGroup radioGroupMode;

    private MyLogRadioButton currentRadioButton;

    private Switch switchTractionControl;
    private Switch switchParkingSensors;
    private Switch switchAutoLock;


    //endregion


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings_drive, container, false);


        //Initialize views

        radioGroupMode = view.findViewById(R.id.drivesettings_group_mode);
        switchTractionControl = view.findViewById(R.id.drivesettings_switch_traction);
        switchParkingSensors = view.findViewById(R.id.drivesettings_switch_sensors);
        switchAutoLock = view.findViewById(R.id.drivesettings_switch_lock);


        //Listeners for task completion checking

        radioGroupMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                currentRadioButton = view.findViewById(checkedId);


                //Check to end task, if target is reached

                ((MainActivity)getActivity()).isViewSetToTarget(currentRadioButton.getText().toString(), "driveSettingsFragment", "driving_mode", "Driving mode was set.");

            }
        });

        switchTractionControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //Check to end task, if target is reached

                ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(isChecked), "driveSettingsFragment", "traction_control", "Traction control was set.");

            }
        });

        switchParkingSensors.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //Check to end task, if target is reached

                ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(isChecked), "driveSettingsFragment", "parking_sensors", "Parking sensors were set.");


            }
        });

        switchAutoLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //Check to end task, if target is reached

                ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(isChecked), "driveSettingsFragment", "auto_lock", "Auto lock was set.");


            }
        });


        return view;

    }



    //Initialize fragment for task

    public void initDriveSettingsFragment(){

        String property = MainActivity.taskObject.getProperty();
        String initMode = MainActivity.taskObject.getInit().toLowerCase();

        switch (property) {
            case "driving_mode":
                switch (initMode) {
                    case ("normal"):
                        radioGroupMode.check(R.id.drivesettings_button_normal);
                        break;
                    case "sport":
                        radioGroupMode.check(R.id.drivesettings_button_sport);
                        break;
                    case "eco":
                        radioGroupMode.check(R.id.drivesettings_button_eco);
                        break;
                }
                break;
            case "traction_control":
                switchTractionControl.setChecked(Boolean.valueOf(initMode));
                break;
            case "parking_sensors":
                switchParkingSensors.setChecked(Boolean.valueOf(initMode));
                break;
            case "auto_lock":
                switchAutoLock.setChecked(Boolean.valueOf(initMode));
                break;
        }

        MainActivity.fragmentInitialized = true;

    }

}