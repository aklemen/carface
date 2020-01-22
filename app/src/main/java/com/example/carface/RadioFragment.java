package com.example.carface;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

public class RadioFragment extends Fragment {


    //region Variables


    //Fragment initialization

    private FragmentManager fm;
    private FMRadioFragment FMRadioFragment = new FMRadioFragment();
    private USBRadioFragment USBRadioFragment = new USBRadioFragment();
    private BTRadioFragment BTRadioFragment = new BTRadioFragment();

    //Radio group for buttons, for switching between fragments

    public RadioGroup radioGroupSource;


    //endregion


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_radio, container, false);


        //Initialize UI

        fm = getChildFragmentManager();

        fm.beginTransaction()
                .add(R.id.radio_placeholder, BTRadioFragment, "BTRadioFragment")
                .add(R.id.radio_placeholder, USBRadioFragment, "USBRadioFragment")
                .add(R.id.radio_placeholder, FMRadioFragment, "FMRadioFragment")
                .commit();



        //Handle fragment switches on button clicks

        radioGroupSource = view.findViewById(R.id.radio_group_source);

        radioGroupSource.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_button_fm:
                        FMRadioFragment.resumeStation();
                        fm.beginTransaction()
                                .hide(BTRadioFragment)
                                .hide(USBRadioFragment)
                                .show(FMRadioFragment)
                                .commit();
                        break;

                    case R.id.radio_button_usb:
                        USBRadioFragment.resumeSong();
                        fm.beginTransaction()
                                .hide(BTRadioFragment)
                                .hide(FMRadioFragment)
                                .show(USBRadioFragment)
                                .commit();
                        break;

                    case R.id.radio_button_bt:
                        BTRadioFragment.resumeSong();
                        fm.beginTransaction()
                                .hide(USBRadioFragment)
                                .hide(FMRadioFragment)
                                .show(BTRadioFragment)
                                .commit();
                        break;
                }
            }
        });

        return view;
    }

}
