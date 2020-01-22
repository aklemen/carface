package com.example.carface;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;

import java.util.regex.Pattern;


public class SettingsFragment extends Fragment {


    //region Variables


    //Fragment initialization

    private FragmentManager fm;
    private DriveSettingsFragment driveSettingsFragment = new DriveSettingsFragment();
    private SoundSettingsFragment soundSettingsFragment = new SoundSettingsFragment();
    private ScreenSettingsFragment screenSettingsFragment = new ScreenSettingsFragment();

    //Radio group for buttons, for switching between fragments

    private RadioGroup radioGroupSettings;

    //Radio buttons for fragments

    private MyLogRadioButton buttonDrive;
    private MyLogRadioButton buttonSound;
    private MyLogRadioButton buttonScreen;

    //App settings slide view

    public SlideUp slideUp;
    private View slideView;
    private RelativeLayout slideFrom;

    private EditText editTextIP;
    private Button buttonSaveIP;
    private Button buttonCancelTask;

    //Shared preferences for saving current IP for server connection

    private SharedPreferences sharedPrefs;


    //endregion


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_settings, container, false);


        //Initialize views

        buttonDrive = view.findViewById(R.id.settings_button_drive);
        buttonSound = view.findViewById(R.id.settings_button_sound);
        buttonScreen = view.findViewById(R.id.settings_button_screen);

        editTextIP = view.findViewById(R.id.settings_edit_ip);
        buttonSaveIP = view.findViewById(R.id.settings_button_saveIP);
        buttonCancelTask = view.findViewById(R.id.settings_button_cancelTask);


        //Initialize UI

        fm = getChildFragmentManager();

        fm.beginTransaction()
                .add(R.id.settings_placeholder, screenSettingsFragment, "screenSettingsFragment")
                .add(R.id.settings_placeholder, soundSettingsFragment, "soundSettingsFragment")
                .add(R.id.settings_placeholder, driveSettingsFragment, "driveSettingsFragment")
                .commit();


        //Handle fragment switches on button clicks

        radioGroupSettings = view.findViewById(R.id.settings_group_settings);

        radioGroupSettings.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.settings_button_drive:
                        fm.beginTransaction()
                                .hide(soundSettingsFragment)
                                .hide(screenSettingsFragment)
                                .show(driveSettingsFragment)
                                .commit();
                        break;

                    case R.id.settings_button_sound:
                        fm.beginTransaction()
                                .hide(screenSettingsFragment)
                                .hide(driveSettingsFragment)
                                .show(soundSettingsFragment)
                                .commit();
                        break;

                    case R.id.settings_button_screen:
                        fm.beginTransaction()
                                .hide(soundSettingsFragment)
                                .hide(driveSettingsFragment)
                                .show(screenSettingsFragment)
                                .commit();
                        break;
                }
            }
        });

        //Initialize app settings - get IP, set IP text, init buttons etc.

        sharedPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        String serverIP = sharedPrefs.getString(getString(R.string.current_ip), "192.168.0.2:80");

        editTextIP.setText(serverIP);

        slideView = view.findViewById(R.id.settings_relative_slideView);
        slideFrom = view.findViewById(R.id.settings_relative_slideFrom);

        slideUp = new SlideUpBuilder(slideView)
                .withStartState(SlideUp.State.HIDDEN)
                .withStartGravity(Gravity.END)
                .withSlideFromOtherView(slideFrom)
                .build();

        buttonCancelTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.taskObject != null){

                    ((MainActivity)getActivity()).endTask(getString(R.string.task_canceled_description));
                    MainActivity.fragmentInitialized = false;

                    slideUp.hide();

                }
                else {
                    Toast.makeText(getActivity(), "No task in progress", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSaveIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIpAndConnect(editTextIP.getText().toString());
            }
        });


        return view;
    }


    //Check if IP is valid and try to connect to websocket server with it

    private void checkIpAndConnect(String ip) {

        if (Pattern.matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
                "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):[0-9]+$", ip)) {

            ((MainActivity) getActivity()).connectToWebSocket(ip);
            MainActivity.hideKeyboard(getActivity());

            Toast.makeText(getActivity(), "Trying to connect ...", Toast.LENGTH_LONG).show();

            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(getString(R.string.current_ip), ip);
            editor.apply();

            slideUp.hide();
        }
        else {
            Toast.makeText(getActivity(), "Invalid IP! Please try again.", Toast.LENGTH_LONG).show();
            slideUp.show();
        }

    }



    //Open app settings slide up

    public void openSlideUp(){
        slideUp.show();
    }



    //Disable all views in app settings

    public void connectionActive(){
        buttonSaveIP.setText("Already connected");
        buttonSaveIP.setEnabled(false);

        editTextIP.setEnabled(false);
    }



    //Enable all views in app settings

    public void connectionNotActive(){
        buttonSaveIP.setText("Save and connect");
        buttonSaveIP.setEnabled(true);

        editTextIP.setEnabled(true);
    }



    //Disable all radiogroup buttons

    public void lockButtons(){
        buttonDrive.setEnabled(false);
        buttonSound.setEnabled(false);
        buttonScreen.setEnabled(false);
    }



    //Enable all radiogroup buttons

    public void unlockButtons(){
        buttonDrive.setEnabled(true);
        buttonSound.setEnabled(true);
        buttonScreen.setEnabled(true);
    }

}