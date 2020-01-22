package com.example.carface;

import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;

public class SoundSettingsFragment extends Fragment {


    //region Variables


    //EQ object and sliders/seekbars

    public Equalizer equalizer;

    private SeekBar[] seekBars = new SeekBar[5];
    private int[] seekBarIds = {R.id.soundsettings_seekbar_0, R.id.soundsettings_seekbar_1, R.id.soundsettings_seekbar_2,
            R.id.soundsettings_seekbar_3, R.id.soundsettings_seekbar_4};

    //EQ presets

    private MyLogRadioButton[] radioButton = new MyLogRadioButton[4];
    private int[] radioButtonID = {R.id.soundsettings_button_preset1, R.id.soundsettings_button_preset2, R.id.soundsettings_button_preset3,
            R.id.soundsettings_button_preset4};
    private RadioGroup radioGroupPresets;

    //View for finding other views

    private View view;


    //endregion


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_settings_sound, container, false);


        //Initialize radiogroup for presets

        radioGroupPresets = view.findViewById(R.id.soundsettings_group_presets);

        radioGroupPresets.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId != -1) {

                    MyLogRadioButton currentRadioButton = view.findViewById(checkedId);

                    //Check to end task, if target is reached

                    ((MainActivity)getActivity()).isViewSetToTarget(currentRadioButton.getText().toString(), "soundSettingsFragment", "equalizer",
                            "EQ preset was set.");

                }

            }
        });


        //Initialize EQ and seekbars, set listeners

        if (equalizer == null) {
            equalizer = new Equalizer(0, MainActivity.globalMediaPlayer.getAudioSessionId());
            equalizer.setEnabled(true);
        }

        for (int i = 0; i < radioButtonID.length; i++) {
            radioButton[i] = view.findViewById(radioButtonID[i]);
            radioButton[i].setText(equalizer.getPresetName((short)i));

            final int j = i;
            radioButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setEqPreset(j);
                }
            });
        }

        for (int i = 0; i < seekBarIds.length; i++){
            seekBars[i] = view.findViewById(seekBarIds[i]);

            seekBars[i].setMax(equalizer.getBandLevelRange()[1]*2);

            final int j = i;
            seekBars[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    equalizer.setBandLevel((short) j, (short) (progress - equalizer.getBandLevelRange()[1]));
                    if (fromUser){
                        radioGroupPresets.clearCheck();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }


        //Set default preset (the first one, usually Normal)

        radioButton[0].performClick();

        return view;

    }



    //TODO App sometimes crashes because of the equalizer if you skip songs quickly
    //Initialize EQ to the current globalMediaPlayer (audioSessionId changes every time you skip a song )

    public void initEqualizer(){

        equalizer = new Equalizer(0, MainActivity.globalMediaPlayer.getAudioSessionId());

        for (int i = 0; i < seekBarIds.length; i++) {
            equalizer.setBandLevel((short) i, (short) (seekBars[i].getProgress() - equalizer.getBandLevelRange()[1]));
        }
        equalizer.setEnabled(true);

    }



    //Set EQ preset on selected id

    public void setEqPreset(int id){
        equalizer.usePreset((short)id);

        for (int i = 0; i < seekBars.length; i++){
            int currentProgress = equalizer.getBandLevel((short) i) + equalizer.getBandLevelRange()[1];
            seekBars[i].setProgress(currentProgress);
        }
    }



    //Initialize fragment for task

    public void initSoundSettingsFragment(){

        String initPreset = MainActivity.taskObject.getInit().toLowerCase();

        for (int i = 0; i < radioButtonID.length; i++){

            MyLogRadioButton currentRadioButton = view.findViewById(radioButtonID[i]);

            if (currentRadioButton.getText().toString().toLowerCase().equals(initPreset)){
                radioGroupPresets.check(radioButtonID[i]);
                break;
            }

        }

        MainActivity.fragmentInitialized = true;
    }



    //Release equalizer

    @Override
    public void onDestroy() {
        super.onDestroy();
        equalizer.release();
    }
}