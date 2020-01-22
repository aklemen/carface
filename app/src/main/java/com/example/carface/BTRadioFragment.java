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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BTRadioFragment extends Fragment {

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
    private SeekBar seekBar;

    private TextView songText;
    private TextView artistText;
    private TextView albumText;
    private TextView timeText;

    //List of songs

    private List<Song> listOfSongs;

    //Shared preferences for player initialization

    private  SharedPreferences sharedPref;

    //Fragments for accessing their methods and elements

    private RadioFragment radioFragment;
    private SettingsFragment settingsFragment;
    private SoundSettingsFragment soundSettingsFragment;

    //Phones radiogroup

    private RadioGroup radioGroupPhones;


    //endregion


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_radio_bt, container, false);


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


        //Mix up songs to play more randomly, not the same as in USB

        Collections.shuffle(listOfSongs);


        //Initialize views

        songText = view.findViewById(R.id.btradio_text_song);
        artistText = view.findViewById(R.id.btradio_text_artist);
        albumText = view.findViewById(R.id.btradio_text_album);
        timeText = view.findViewById(R.id.btradio_text_time);

        buttonPlay = view.findViewById(R.id.btradio_button_play);
        buttonForward = view.findViewById(R.id.btradio_button_forward);
        buttonBack = view.findViewById(R.id.btradio_button_back);

        seekBar = view.findViewById(R.id.btradio_seekbar);

        radioGroupPhones = view.findViewById(R.id.btradio_group_phones);


        //Initialize player and button listeners, set textviews, get current song played from SharedPreferences

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        currentSongIx = sharedPref.getInt(getString(R.string.current_song_index_bt), 0);
        currentDuration = sharedPref.getInt(getString(R.string.current_song_time_bt), 0);

        MainActivity.globalMediaPlayer.reset();
        MainActivity.globalMediaPlayer = MediaPlayer.create(getActivity(), listOfSongs.get(currentSongIx).getSongResourceId());

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

        songText.setText(listOfSongs.get(currentSongIx).getSongName());
        artistText.setText(listOfSongs.get(currentSongIx).getSongArtist());
        albumText.setText(listOfSongs.get(currentSongIx).getSongAlbum());

        updatePlayer();

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

        radioGroupPhones.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                buttonForward.performClick();
            }
        });


        //Get radio fragment

        radioFragment = (RadioFragment) getParentFragment();


        seekBarVolume = (SeekBar)view.findViewById(R.id.btradio_seekbar_volume);

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




        return view;

    }



    //Change song - sets TextFields, plays selected song

    public void changeSong(int songIndex){

        currentSongIx = songIndex;

        songText.setText(listOfSongs.get(songIndex).getSongName());
        artistText.setText(listOfSongs.get(songIndex).getSongArtist());
        albumText.setText(listOfSongs.get(songIndex).getSongAlbum());

        buttonPlay.setBackgroundResource(R.drawable.ic_pause);
        MainActivity.globalMediaPlayer.reset();
        MainActivity.globalMediaPlayer = MediaPlayer.create(getActivity(), listOfSongs.get(songIndex).getSongResourceId());
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

        ((MainActivity)getActivity()).isViewSetToTarget(listOfSongs.get(songIndex).getSongName(), "BTRadioFragment", "song_name", "Song was set.");

    }



    //Resume song - used for switching between FM, USB and BT

    public void resumeSong(){
        changeSong(currentSongIx);
        MainActivity.globalMediaPlayer.seekTo(currentDuration);

        seekBarVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }



    //Timer in player

    private Runnable mUpdateTime = new Runnable() {
        public void run() {
            if (MainActivity.globalMediaPlayer.isPlaying() && radioFragment.radioGroupSource.getCheckedRadioButtonId() == R.id.radio_button_bt) {
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
        for (Song s : listOfSongs){
            if (songName.equals(s.getSongName())){
                return listOfSongs.indexOf(s);
            }
        }
        return 0;
    }



    //Initialize fragment for task

    public void initBTRadioFragment(){

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
        editor.putInt(getString(R.string.current_song_index_bt), currentSongIx);
        editor.putInt(getString(R.string.current_song_time_bt), currentDuration);
        editor.apply();
    }

}