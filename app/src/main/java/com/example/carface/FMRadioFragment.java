package com.example.carface;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FMRadioFragment extends Fragment {


    //region Variables


    //Player

    private SeekBar seekBarVolume;
    private AudioManager audioManager;

    private String audioUrl;

    private MyLogImageButton buttonPlay;
    private MyLogImageButton buttonForward;
    private MyLogImageButton buttonBack;
    private MyLogImageButton buttonSearch;
    private MyLogImageButton buttonClose;

    private boolean isFirst = true;

    private TextView stationNameText;
    private TextView frequencyText;

    //List of stations

    private MyLogButton buttonSortFrequency;
    private MyLogButton buttonSortName;
    private ImageView sortArrow1;
    private ImageView sortArrow2;
    private EditText searchView;
    private MyLogImageButton buttonClear;

    private String currentListState = "freqAsc";

    private List<RadioStation> listOfStations;
    private List<RadioStation> listOfStationsByFrequency;

    public SlideUp slideUpList;
    private View slideView;
    private RecyclerView recyclerView;
    private ListOfStationsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    //Saved stations

    private RadioGroup radioGroupStations;
    private MyLogRadioButton[] radioButton = new MyLogRadioButton[4];
    private int[] radioButtonID = {R.id.fmradio_button_station1, R.id.fmradio_button_station2, R.id.fmradio_button_station3, R.id.fmradio_button_station4};
    RadioGroup.OnCheckedChangeListener radioGroupListener;

    //Format for frequency

    private DecimalFormat df = new DecimalFormat("#.00");

    //The current frequency to save state

    private String currentFrequency;

    //Fragments for accessing their methods

    private SettingsFragment settingsFragment;
    private SoundSettingsFragment soundSettingsFragment;


    //endregion


    //TODO Read current song title and artist from server (Icecast etc.)
    //TODO Enlarge the area for button press.


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_radio_fm, container, false);

        //Initialize fragments for EQ

        settingsFragment = (SettingsFragment) getActivity().getSupportFragmentManager().findFragmentByTag("settingsFragment");
        soundSettingsFragment = (SoundSettingsFragment) settingsFragment.getChildFragmentManager().findFragmentByTag("soundSettingsFragment");


        //Initialize new list of stations

        listOfStations = new ArrayList<>();


        //Get stations from stations.json, add them to the list of RadioStation objects and sort them by frequency

        try {
            JSONObject jsonObject = new JSONObject(((MainActivity)getActivity()).loadJSONFromAsset("stations.json"));
            JSONArray jsonArray = jsonObject.getJSONArray("stations");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject stationJSONObject = jsonArray.getJSONObject(i);
                RadioStation station = new RadioStation(i, stationJSONObject.getString("name"), stationJSONObject.getString("frequency"), stationJSONObject.getString("url"));
                listOfStations.add(station);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //Sort list of stations and create a dedicated list for this sort

        sortStationsByFrequency(listOfStations);
        listOfStationsByFrequency = new ArrayList<>(listOfStations);


        //Initialize views

        stationNameText = view.findViewById(R.id.fmradio_text_station);
        frequencyText = view.findViewById(R.id.fmradio_text_frequency);

        buttonPlay = view.findViewById(R.id.fmradio_button_play);
        buttonForward = view.findViewById(R.id.fmradio_button_forward);
        buttonBack = view.findViewById(R.id.fmradio_button_back);
        buttonSearch = view.findViewById(R.id.fmradio_button_search);
        buttonClose = view.findViewById(R.id.fmradio_button_close);

        buttonSortFrequency = view.findViewById(R.id.fmradio_button_sortFrequency);
        buttonSortName = view.findViewById(R.id.fmradio_button_sortName);
        sortArrow1 = view.findViewById(R.id.fmradio_image_arrow1);
        sortArrow2 = view.findViewById(R.id.fmradio_image_arrow2);
        searchView = view.findViewById(R.id.fmradio_edit_search);
        buttonClear = view.findViewById(R.id.fmradio_button_clear);


        //Initialize UI

        for (int i = 0; i < radioButtonID.length; i++) {
            radioButton[i] = view.findViewById(radioButtonID[i]);
            radioButton[i].setText(listOfStationsByFrequency.get(i).getStationName());
        }

        buttonSortFrequency.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_blue_50, null));


        //Initialize radio play and button listeners for play/pause, forward, back etc.

        MainActivity.globalMediaPlayer.reset();
        MainActivity.globalMediaPlayer = new MediaPlayer();
        MainActivity.globalMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        stationNameText.setText(listOfStationsByFrequency.get(0).getStationName());
        frequencyText.setText(listOfStationsByFrequency.get(0).getStationFrequency());
        audioUrl = listOfStationsByFrequency.get(0).getStationUrl();

        currentFrequency = listOfStationsByFrequency.get(0).getStationFrequency();

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try{
                    if(MainActivity.globalMediaPlayer.isPlaying()) {
                        buttonPlay.setBackgroundResource(R.drawable.ic_play);
                        MainActivity.globalMediaPlayer.stop();
                        MainActivity.globalMediaPlayer.reset();
                    }
                    else {
                        buttonPlay.setBackgroundResource(R.drawable.ic_pause);
                        MainActivity.globalMediaPlayer.setDataSource(audioUrl);
                        MainActivity.globalMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.start();
                                soundSettingsFragment.initEqualizer();
                            }
                        });
                        MainActivity.globalMediaPlayer.prepareAsync();
                    }
                }catch (IllegalArgumentException e){
                    e.printStackTrace();
                }catch (SecurityException e){
                    e.printStackTrace();
                }catch (IllegalStateException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        MainActivity.globalMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                soundSettingsFragment.initEqualizer();
            }
        });

        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String freq = String.valueOf(df.format(Double.parseDouble(frequencyText.getText().toString()) + 0.10));
                if (freq.equals("108.00")){
                    freq = "95.40";
                }
                changeStation(freq);
            }
        });

        buttonForward.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                for (int i = 0; i < listOfStations.size(); i++){
                    String freq = listOfStationsByFrequency.get(i).getStationFrequency();
                    if (Double.parseDouble(freq) > Double.parseDouble(frequencyText.getText().toString())){
                        changeStation(freq);
                        return true;
                    }
                }
                changeStation(listOfStationsByFrequency.get(0).getStationFrequency());
                return true;
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String freq = String.valueOf(df.format(Double.parseDouble(frequencyText.getText().toString()) - 0.10));
                if (freq.equals("95.20")){
                    freq = "107.80";
                }
                changeStation(freq);
            }
        });

        buttonBack.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                for (int i = (listOfStations.size()-1); i > -1; i--){
                    String freq = listOfStationsByFrequency.get(i).getStationFrequency();
                    if (Double.parseDouble(freq) < Double.parseDouble(frequencyText.getText().toString())){
                        changeStation(freq);
                        return true;
                    }
                }
                changeStation(listOfStationsByFrequency.get(listOfStations.size()-1).getStationFrequency());
                return true;
            }
        });


        //Initialize and set listener for radiogroup (saved stations)

        radioGroupStations = view.findViewById(R.id.fmradio_group_stations);

        radioGroupListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.fmradio_button_station1:
                        changeStation(listOfStationsByFrequency.get(0).getStationFrequency());
                        break;

                    case R.id.fmradio_button_station2:
                        changeStation(listOfStationsByFrequency.get(1).getStationFrequency());
                        break;

                    case R.id.fmradio_button_station3:
                        changeStation(listOfStationsByFrequency.get(2).getStationFrequency());
                        break;

                    case R.id.fmradio_button_station4:
                        changeStation(listOfStationsByFrequency.get(3).getStationFrequency());
                        break;

                }
            }
        };

        radioGroupStations.setOnCheckedChangeListener(radioGroupListener);


        //Initialize list of stations (slideview, recyclerview ...)

        slideView = view.findViewById(R.id.fmradio_relative_slideView);
        recyclerView = view.findViewById(R.id.fmradio_recycler_stations);

        slideUpList = new SlideUpBuilder(slideView)
                .withStartState(SlideUp.State.HIDDEN)
                .withStartGravity(Gravity.BOTTOM)
                .withListeners(new SlideUp.Listener.Visibility() {
                    @Override
                    public void onVisibilityChanged(int visibility) {
                        if (visibility == View.GONE){
                            MainActivity.hideKeyboard(getActivity());
                        }
                    }
                })
                .build();


        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ListOfStationsAdapter(listOfStations, this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider));

        recyclerView.addItemDecoration(itemDecorator);


        //Set OnClick listeners for showing and closing search, sorting stations and clearing searchView

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideUpList.show();
            }
        });
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideUpList.hide();
            }
        });

        buttonSortFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSortName.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_black_50, null));
                buttonSortFrequency.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_blue_50, null));
                switch (currentListState) {
                    case "freqAsc":
                        Collections.reverse(adapter.getListOfStations());
                        sortArrow1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_up, null));
                        currentListState = "freqDesc";
                        break;
                    case "freqDesc":
                        Collections.reverse(adapter.getListOfStations());
                        sortArrow1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_down, null));
                        currentListState = "freqAsc";
                        break;
                    default:
                        sortStationsByFrequency(adapter.getListOfStations());
                        sortArrow1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_down, null));
                        currentListState = "freqAsc";
                        break;
                }
                adapter.notifyDataSetChanged();
            }
        });

        buttonSortName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSortFrequency.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_black_50, null));
                buttonSortName.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_blue_50, null));
                switch (currentListState) {
                    case "nameAsc":
                        Collections.reverse(adapter.getListOfStations());
                        sortArrow2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_up, null));
                        currentListState = "nameDesc";
                        break;
                    case "nameDesc":
                        Collections.reverse(adapter.getListOfStations());
                        sortArrow2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_down, null));
                        currentListState = "nameAsc";
                        break;
                    default:
                        sortStationsByName(adapter.getListOfStations());
                        sortArrow2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_down, null));
                        currentListState = "nameAsc";
                        break;
                }
                adapter.notifyDataSetChanged();
            }
        });

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0){
                    buttonClear.setVisibility(View.VISIBLE);
                }
                else {
                    buttonClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setText("");
            }
        });



        seekBarVolume = view.findViewById(R.id.fmradio_seekbar_volume);


        try
        {
            audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            seekBarVolume.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            seekBarVolume.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onStopTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2)
                {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                    ((MainActivity)getActivity()).isViewSetToTarget(String.valueOf(progress),"", "volume", "Volume was set.");

                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        buttonPlay.performClick();

        return view;

    }



    //Change radio station - sets TextFields, plays selected station, sets station button focus

    public void changeStation(String frequency){

        currentFrequency = frequency;

        frequencyText.setText(frequency);
        int currentIx = -1;
        for (int i = 0; i < listOfStations.size(); i++){
            if (listOfStations.get(i).getStationFrequency().equals(frequency)){
                currentIx = i;
                break;
            }
        }
        if (currentIx != -1){
            stationNameText.setText(listOfStations.get(currentIx).getStationName());
            playStation(listOfStations.get(currentIx).getStationUrl());
            if (currentIx <= 3){
                radioGroupStations.check(radioButtonID[currentIx]);
            }
            else {
                radioGroupStations.setOnCheckedChangeListener(null);
                radioGroupStations.clearCheck();
                radioGroupStations.setOnCheckedChangeListener(radioGroupListener);
            }
        }
        else {
            stationNameText.setText(getString(R.string.name_noise));
            playStation(getString(R.string.url_noise));
            radioGroupStations.setOnCheckedChangeListener(null);
            radioGroupStations.clearCheck();
            radioGroupStations.setOnCheckedChangeListener(radioGroupListener);
        }

        //Check to end task, if target is reached

        ((MainActivity)getActivity()).isViewSetToTarget(frequency, "FMRadioFragment", "frequency", "Frequency was set.");

    }



    //Plays station if it is not already playing

    public void playStation(String stationURL){

        buttonPlay.setBackgroundResource(R.drawable.ic_pause);
        if (!audioUrl.equals(stationURL) || isFirst) {
            audioUrl = stationURL;
            MainActivity.globalMediaPlayer.stop();
            MainActivity.globalMediaPlayer.reset();
            try {
                MainActivity.globalMediaPlayer.setDataSource(audioUrl);
                MainActivity.globalMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
                MainActivity.globalMediaPlayer.prepareAsync();
//                soundSettingsFragment.initEqualizer();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isFirst = false;
        }

    }



    //Resume station - used for switching between FM, USB and BT

    public void resumeStation(){

        MainActivity.globalMediaPlayer.reset();
        MainActivity.globalMediaPlayer = new MediaPlayer();
        MainActivity.globalMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            MainActivity.globalMediaPlayer.setDataSource(audioUrl);
            MainActivity.globalMediaPlayer.prepare();
            MainActivity.globalMediaPlayer.start();
            soundSettingsFragment.initEqualizer();
            buttonPlay.setBackgroundResource(R.drawable.ic_pause);
        } catch (IOException e) {
            e.printStackTrace();
        }

        seekBarVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

    }



    //Sorting methods

    public void sortStationsByName(List<RadioStation> list){

        Collections.sort(list, new Comparator<RadioStation>() {
            @Override
            public int compare(RadioStation first, RadioStation second) {
                return first.getStationName().toLowerCase().compareTo(second.getStationName().toLowerCase());
            }
        });

    }

    public void sortStationsByFrequency(List<RadioStation> list){

        Collections.sort(list, new Comparator<RadioStation>() {
            @Override
            public int compare(RadioStation first, RadioStation second) {
                float firstFloat = Float.parseFloat(first.getStationFrequency());
                float secondFloat = Float.parseFloat(second.getStationFrequency());
                return Float.compare(firstFloat, secondFloat);
            }
        });

    }



    //Filter for search

    private void filter (String text){

        List<RadioStation> filteredList = new ArrayList<>();
        for(RadioStation r : listOfStations){
            if (r.getStationName().toLowerCase().contains(text.toLowerCase()) || r.getStationFrequency().contains(text)){
                filteredList.add(r);
            }
        }
        adapter.filterList(filteredList);

    }



    //Initialize fragment for task

    public void initFMRadioFragment(){


        String property = MainActivity.taskObject.getProperty();

        if (property.equals("volume")){
            seekBarVolume.setProgress(Integer.valueOf(MainActivity.taskObject.getInit()));
        }
        else if (property.equals("frequency")) {
            changeStation(MainActivity.taskObject.getInit());
        }

        MainActivity.fragmentInitialized = true;

    }

}