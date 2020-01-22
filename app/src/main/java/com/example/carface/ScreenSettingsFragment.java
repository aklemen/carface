package com.example.carface;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

public class ScreenSettingsFragment extends Fragment {


    //region Variables

    private SeekBar seekBrightness;
    private Switch switchAutoBrightness;
    private Switch switchLockScreen;
    private Switch switchDimScreen;

    private SettingsFragment settingsFragment;

    private ContentResolver contentResolver;

    //endregion


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings_screen, container, false);


        //Initialize ContentResolver, views and SettingsFragment

        contentResolver = getActivity().getContentResolver();

        seekBrightness = view.findViewById(R.id.screensettings_seekbar);
        switchAutoBrightness = view.findViewById(R.id.screensettings_switch_auto);
        switchLockScreen = view.findViewById(R.id.screensettings_switch_lock);
        switchDimScreen = view.findViewById(R.id.screensettings_switch_screen);

        settingsFragment = (SettingsFragment) getParentFragment();


        //Initialize brightness seekbar

        seekBrightness.setMax(255);
        seekBrightness.setProgress(getBrightness());

        //Set listeners for seekbar and switches

        seekBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                setBrightness(progress);


                //Check to end task, if target is reached

                ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(progress), "screenSettingsFragment", "brightness",
                        "Brightness was set.");


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                switchDimScreen.setChecked(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        switchAutoBrightness.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                    seekBrightness.setEnabled(false);
                    seekBrightness.setAlpha(0.3f);

                }
                else {
                    Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    seekBrightness.setEnabled(true);
                    seekBrightness.setAlpha(1.0f);
                }


                //Check to end task, if target is reached

                ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(isChecked), "screenSettingsFragment", "auto_brightness",
                        "Auto brightness was set.");


            }
        });

        switchLockScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    lockEverything();
                }
                else {
                    unlockEverything();
                }


                //Check to end task, if target is reached

                ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(isChecked), "screenSettingsFragment", "lock_screen",
                        "Lock screen was set.");


            }
        });

        switchDimScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){
                    switchAutoBrightness.setChecked(false);
                    seekBrightness.setProgress(0, true);
                }
                else {
                    seekBrightness.setProgress(200, true);
                }

                //Check to end task, if target is reached

                ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(isChecked), "screenSettingsFragment", "dim_screen",
                        "Dim screen was set.");


            }
        });


        return view;
    }



    //Disable all views

    private void lockEverything(){
        seekBrightness.setEnabled(false);
        switchAutoBrightness.setEnabled(false);
        switchDimScreen.setEnabled(false);
        settingsFragment.lockButtons();
        ((MainActivity)getActivity()).lockMenuButtons();
    }



    //Enable all views

    private void unlockEverything(){
        seekBrightness.setEnabled(true);
        switchAutoBrightness.setEnabled(true);
        switchDimScreen.setEnabled(true);
        settingsFragment.unlockButtons();
        ((MainActivity)getActivity()).unlockMenuButtons();

    }



    //Set brightness to desired value

    private void setBrightness(int brightness) {
        if(!Settings.System.canWrite(getActivity())) {
            Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            startActivity(permissionIntent);
        } else {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        }

        WindowManager.LayoutParams mLayoutParams = getActivity().getWindow().getAttributes();
        mLayoutParams.screenBrightness = brightness;
        getActivity().getWindow().setAttributes(mLayoutParams);
    }



    //Get current brightness

    private int getBrightness(){
        return Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS,-1);
    }



    //Unlocks screen
    public void unlockScreen(){
        switchLockScreen.setChecked(false);
    }



    //Initialize fragment for task

    public void initScreenSettingsFragment(){

        String property = MainActivity.taskObject.getProperty();
        String initMode = MainActivity.taskObject.getInit().toLowerCase();

        switch (property) {
            case "brightness":
                switchAutoBrightness.setChecked(false);
                seekBrightness.setProgress(Integer.valueOf(initMode));
                break;
            case "auto_brightness":
                switchAutoBrightness.setChecked(Boolean.valueOf(initMode));
                break;
            case "lock_screen":
                switchLockScreen.setChecked(Boolean.valueOf(initMode));
                break;
            case "dim_screen":
                switchDimScreen.setChecked(Boolean.valueOf(initMode));
                break;
        }

        MainActivity.fragmentInitialized = true;

    }

}