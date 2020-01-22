package com.example.carface;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class USBRadioFragment extends Fragment {

    //region Variables


    //Player

    private SeekBar seekBarVolume;
    private AudioManager audioManager;

    private int currentSongIx;
    public int currentDuration;
    private long currentSongDuration;

    private MyLogImageButton buttonPlay;
    private MyLogImageButton buttonForward;
    private MyLogImageButton buttonBack;
    private MyLogImageButton buttonSearch;
    private MyLogImageButton buttonClose;
    private SeekBar seekBar;

    private TextView songText;
    private TextView artistText;
    private TextView albumText;
    private TextView timeText;

    //List of songs

    private MyLogButton buttonSortName;
    private MyLogButton buttonSortArtist;
    private ImageView sortArrow1;
    private ImageView sortArrow2;
    private EditText searchView;
    private MyLogImageButton buttonClear;

    private RelativeLayout nextSongView;
    private TextView upNextText;
    private TextView nextSongText;

    private List<Song> listOfSongs;
    private List<Song> listOfSongsByName;

    private String currentListState = "nameAsc";

    public SlideUp slideUpList;
    private View slideView;
    private RecyclerView recyclerView;
    private ListOfSongsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    //Shared preferences for player initialization

    private  SharedPreferences sharedPref;

    //Fragments for accessing their methods and elements

    private RadioFragment radioFragment;
    private SettingsFragment settingsFragment;
    private SoundSettingsFragment soundSettingsFragment;


    //endregion


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_radio_usb, container, false);


        //Initialize fragments for EQ

        settingsFragment = (SettingsFragment) getActivity().getSupportFragmentManager().findFragmentByTag("settingsFragment");
        soundSettingsFragment = (SoundSettingsFragment) settingsFragment.getChildFragmentManager().findFragmentByTag("soundSettingsFragment");


        //Initialize new list of songs

        listOfSongs = new ArrayList<>();


        //Get songs from raw folder, add them to the list of Song objects

        int resourceId;

        Field[] fields=R.raw.class.getFields();
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

        for (int i = 0; i < fields.length; i++) {
            try {
                resourceId = fields[i].getInt(fields[i]);
                AssetFileDescriptor afd = getResources().openRawResourceFd(resourceId);
                mediaMetadataRetriever.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                Song song = new Song(i, mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM), resourceId);
                listOfSongs.add(song);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }


        //Sort list of songs by name and create a dedicated list for this sort

        sortSongsByName(listOfSongs);
        listOfSongsByName = new ArrayList<>(listOfSongs);


        //Initialize views

        songText = view.findViewById(R.id.usbradio_text_song);
        artistText = view.findViewById(R.id.usbradio_text_artist);
        albumText = view.findViewById(R.id.usbradio_text_album);
        timeText = view.findViewById(R.id.usbradio_text_time);

        nextSongView = view.findViewById(R.id.usbradio_relative_nextSong);
        upNextText = view.findViewById(R.id.usbradio_text_upNext);
        nextSongText= view.findViewById(R.id.usbradio_text_nextSong);

        buttonPlay = view.findViewById(R.id.usbradio_button_play);
        buttonForward = view.findViewById(R.id.usbradio_button_forward);
        buttonBack = view.findViewById(R.id.usbradio_button_back);
        buttonSearch = view.findViewById(R.id.usbradio_button_search);

        buttonClose = view.findViewById(R.id.usbradio_button_close);
        buttonSortName = view.findViewById(R.id.usbradio_button_sortName);
        buttonSortArtist = view.findViewById(R.id.usbradio_button_sortArtist);
        sortArrow1 = view.findViewById(R.id.usbradio_image_arrow1);
        sortArrow2 = view.findViewById(R.id.usbradio_image_arrow2);
        searchView = view.findViewById(R.id.usbradio_edit_search);
        buttonClear = view.findViewById(R.id.usbradio_button_clear);

        seekBar = view.findViewById(R.id.usbradio_seekbar);


        //Initialize UI

        buttonSortName.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_blue_50, null));


        //Initialize player and button listeners, set textviews, get current song played from SharedPreferences

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        currentSongIx = sharedPref.getInt(getString(R.string.current_song_index_usb), 0);
        currentDuration = sharedPref.getInt(getString(R.string.current_song_time_usb), 0);

        MainActivity.globalMediaPlayer.reset();
        MainActivity.globalMediaPlayer = MediaPlayer.create(getActivity(), listOfSongsByName.get(currentSongIx).getSongResourceId());

        seekBar.setMax(MainActivity.globalMediaPlayer.getDuration());
        MainActivity.globalMediaPlayer.seekTo(currentDuration);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(MainActivity.globalMediaPlayer != null && fromUser){
                    MainActivity.globalMediaPlayer.seekTo(progress);
                }
            }
        });

        final Handler mHandler = new Handler();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(MainActivity.globalMediaPlayer != null){
                    int mCurrentPosition = MainActivity.globalMediaPlayer.getCurrentPosition();
                    seekBar.setProgress(mCurrentPosition);
                }
                mHandler.postDelayed(this, 1000);
            }
        });

        MainActivity.globalMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                int nextIx = currentSongIx + 1;
                if (nextIx == listOfSongs.size()){
                    nextIx = 0;
                }
                changeSong(nextIx);
            }
        });

        songText.setText(listOfSongsByName.get(currentSongIx).getSongName());
        artistText.setText(listOfSongsByName.get(currentSongIx).getSongArtist());
        albumText.setText(listOfSongsByName.get(currentSongIx).getSongAlbum());

        updatePlayer();

        int nextSongIx = currentSongIx + 1;

        if (currentSongIx + 1 == listOfSongs.size()){
            nextSongIx = 0;
        }
        else if (currentSongIx - 1 == -1){
            nextSongIx = listOfSongs.size() - 1;
        }

        String nextSongString = listOfSongsByName.get(nextSongIx).getSongArtist() + " - " + listOfSongs.get(nextSongIx).getSongName();
        nextSongText.setText(nextSongString);

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (MainActivity.globalMediaPlayer.isPlaying()) {
                    buttonPlay.setBackgroundResource(R.drawable.ic_play);
                    MainActivity.globalMediaPlayer.pause();
                } else {
                    buttonPlay.setBackgroundResource(R.drawable.ic_pause);
                    MainActivity.globalMediaPlayer.start();
                    soundSettingsFragment.initEqualizer();
                    currentSongDuration = MainActivity.globalMediaPlayer.getDuration();
                    timeText.post(mUpdateTime);
                }
            }
        });

        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nextIx = currentSongIx + 1;
                if (nextIx == listOfSongs.size()){
                    nextIx = 0;
                }
                changeSong(nextIx);
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int previousIx = currentSongIx - 1;
                if (previousIx == -1){
                    previousIx = listOfSongs.size() - 1;
                }
                changeSong(previousIx);
            }
        });


        //Initialize list of songs(slideview, recyclerview ...)

        slideView = view.findViewById(R.id.usbradio_relative_slideView);
        recyclerView = view.findViewById(R.id.usbradio_recycler_songs);

        slideUpList = new SlideUpBuilder(slideView)
                .withStartState(SlideUp.State.HIDDEN)
                .withSlideFromOtherView(nextSongView)
                .withListeners(new SlideUp.Listener.Events() {
                    @Override
                    public void onSlide(float percent) {
                    }


                    @Override
                    public void onVisibilityChanged(int visibility) {
                        if (visibility == View.GONE) {
                            showView(upNextText, 200);
                            showView(nextSongText, 200);
                            MainActivity.hideKeyboard(getActivity());
                        }
                        else if (visibility == View.VISIBLE){
                            hideView(upNextText, 200);
                            hideView(nextSongText, 200);
                        }
                    }
                })
                .withStartGravity(Gravity.BOTTOM)
                .build();

        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ListOfSongsAdapter(listOfSongs, this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider));

        recyclerView.addItemDecoration(itemDecorator);


        //Set OnClick listeners for showing and closing search, sorting songs and clearing searchView

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

        buttonSortName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSortArtist.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_black_50, null));
                buttonSortName.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_blue_50, null));
                switch (currentListState) {
                    case "nameAsc":
                        Collections.reverse(adapter.getListOfSongs());
                        sortArrow1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_up, null));
                        currentListState = "nameDesc";
                        break;
                    case "nameDesc":
                        Collections.reverse(adapter.getListOfSongs());
                        sortArrow1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_down, null));
                        currentListState = "nameAsc";
                        break;
                    default:
                        sortSongsByName(adapter.getListOfSongs());
                        sortArrow1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_down, null));
                        currentListState = "nameAsc";
                        break;
                }
                adapter.notifyDataSetChanged();
            }
        });

        buttonSortArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSortName.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_black_50, null));
                buttonSortArtist.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_blue_50, null));
                switch (currentListState) {
                    case "artistAsc":
                        Collections.reverse(adapter.getListOfSongs());
                        sortArrow2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_up, null));
                        currentListState = "artistDesc";
                        break;
                    case "artistDesc":
                        Collections.reverse(adapter.getListOfSongs());
                        sortArrow2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_down, null));
                        currentListState = "artistAsc";
                        break;
                    default:
                        sortSongsByArtist(adapter.getListOfSongs());
                        sortArrow2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_down, null));
                        currentListState = "artistAsc";
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


        //Get radio fragment

        radioFragment = (RadioFragment) getParentFragment();

        seekBarVolume = (SeekBar)view.findViewById(R.id.usbradio_seekbar_volume);

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
                    Log.d("Napredek USB", String.valueOf(progress));
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return view;

    }



    //Change song - sets TextFields, plays selected song

    public void changeSong(int songIndex){

        currentSongIx = songIndex;

        songText.setText(listOfSongsByName.get(songIndex).getSongName());
        artistText.setText(listOfSongsByName.get(songIndex).getSongArtist());
        albumText.setText(listOfSongsByName.get(songIndex).getSongAlbum());

        int nextSongIx = currentSongIx + 1;

        if (currentSongIx + 1 == listOfSongs.size()){
            nextSongIx = 0;
        }
        else if (currentSongIx - 1 == -1){
            nextSongIx = listOfSongs.size() - 1;
        }

        String nextSongString = listOfSongsByName.get(nextSongIx).getSongArtist() + " - " + listOfSongs.get(nextSongIx).getSongName();
        nextSongText.setText(nextSongString);


        buttonPlay.setBackgroundResource(R.drawable.ic_pause);
        MainActivity.globalMediaPlayer.reset();
        MainActivity.globalMediaPlayer = MediaPlayer.create(getActivity(), listOfSongsByName.get(songIndex).getSongResourceId());
        seekBar.setMax(MainActivity.globalMediaPlayer.getDuration());
        seekBar.setProgress(0);

        MainActivity.globalMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                int nextIx = currentSongIx + 1;
                if (nextIx == listOfSongs.size()){
                    nextIx = 0;
                }
                changeSong(nextIx);
            }
        });

        MainActivity.globalMediaPlayer.start();
        soundSettingsFragment.initEqualizer();
        currentSongDuration = MainActivity.globalMediaPlayer.getDuration();
        timeText.post(mUpdateTime);


        //Check to end task, if target is reached

        ((MainActivity)getActivity()).isViewSetToTarget(listOfSongsByName.get(songIndex).getSongName(), "USBRadioFragment", "song_name", "Song was set.");


    }



    //Resume song - used for switching between FM, USB and BT

    public void resumeSong(){
        changeSong(currentSongIx);
        MainActivity.globalMediaPlayer.seekTo(currentDuration);

        seekBarVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

    }



    //Sorting methods

    public void sortSongsByName(List<Song> list){
        Collections.sort(list, new Comparator<Song>() {
            @Override
            public int compare(Song first, Song second) {
                return first.getSongName().toLowerCase().compareTo(second.getSongName().toLowerCase());
            }
        });
    }

    public void sortSongsByArtist(List<Song> list){
        Collections.sort(list, new Comparator<Song>() {
            @Override
            public int compare(Song first, Song second) {
                return first.getSongArtist().toLowerCase().compareTo(second.getSongArtist().toLowerCase());
            }
        });
    }



    //Filter for search

    private void filter (String text){
        List<Song> filteredList = new ArrayList<>();
        for(Song s : listOfSongs){
            if (s.getSongName().toLowerCase().contains(text.toLowerCase()) || s.getSongArtist().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(s);
            }
        }
        adapter.filterList(filteredList);
    }



    //Hide view animation

    private void hideView (final View view, int duration){
        Animation a = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        a.setDuration(duration);
        a.setAnimationListener(new Animation.AnimationListener() {

            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}

        });
        view.startAnimation(a);
    }



    //Show view animation

    private void showView (final View view, int duration){
        Animation a = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        a.setDuration(duration);
        a.setAnimationListener(new Animation.AnimationListener() {

            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}

        });
        view.startAnimation(a);
    }



    //Timer in player

    private Runnable mUpdateTime = new Runnable() {
        public void run() {
            if (MainActivity.globalMediaPlayer.isPlaying() && radioFragment.radioGroupSource.getCheckedRadioButtonId() == R.id.radio_button_usb) {
                currentDuration = MainActivity.globalMediaPlayer.getCurrentPosition();
                updatePlayer();
                timeText.postDelayed(this, 1000);
            }else {
                timeText.removeCallbacks(this);
            }
        }
    };

    private void updatePlayer(){
        String s =  "" + milliSecondsToTimer((long) currentDuration);
        timeText.setText(s);
    }

    public  String milliSecondsToTimer(long milliseconds) {
        long time = currentSongDuration - milliseconds;
        String finalTimerString = "";
        String secondsString = "";

        int hours = (int) (time / (1000 * 60 * 60));
        int minutes = (int) (time % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((time % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = "- " + finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }



    //Get song index in list by its name

    private int getSongIxByName(String songName){
        for (Song s : listOfSongsByName){
            if (songName.equals(s.getSongName())){
                return listOfSongsByName.indexOf(s);
            }
        }
        return 0;
    }



    //Initialize fragment for task

    public void initUSBRadioFragment(){
        String property = MainActivity.taskObject.getProperty();

        if (property.equals("volume")){
            seekBarVolume.setProgress(Integer.valueOf(MainActivity.taskObject.getInit()));
        }
        else if (property.equals("song_name")) {
            int index = getSongIxByName(MainActivity.taskObject.getInit());
            changeSong(index);
        }

        MainActivity.fragmentInitialized = true;
    }



    //Save current song in shared preferences on stop

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.current_song_index_usb), currentSongIx);
        editor.putInt(getString(R.string.current_song_time_usb), currentDuration);
        editor.apply();
    }

}