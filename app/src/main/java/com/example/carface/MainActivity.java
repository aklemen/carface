/*

UL, Fakulteta za elektrotehniko
Avtor: Anton Klemen
Leto: 2019

*/


package com.example.carface;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends FragmentActivity {

    //ID of an app, used for logging

    public final static String idApp = "Id1";


    //region Variables


    //Static MediaPlayer object for music

    public static MediaPlayer globalMediaPlayer;

    //Static objects for tasks

    public static TaskObject taskObject;
    public static LogObject logObject;
    public static Gson gson;
    public static WebSocket ws;
    public static boolean fragmentInitialized;

    //Fragment initialization

    private FragmentManager fm = getSupportFragmentManager();

    private ACFragment acFragment = new ACFragment();
    private RadioFragment radioFragment = new RadioFragment();
    private NavigationFragment navigationFragment = new NavigationFragment();
    private PhoneFragment phoneFragment = new PhoneFragment();
    private SettingsFragment settingsFragment = new SettingsFragment();
    private Fragment currentFragment;

    //Other fragments in app

    private FMRadioFragment FMRadioFragment;
    private USBRadioFragment USBRadioFragment;
    private BTRadioFragment BTRadioFragment;
    private DriveSettingsFragment driveSettingsFragment;
    private SoundSettingsFragment soundSettingsFragment;
    private ScreenSettingsFragment screenSettingsFragment;

    //Other variables

    public Typeface regularFont;
    public Typeface mediumFont;
    public long startTime;
    public long endTime;

    private MyLogRadioButton acButton;
    private MyLogRadioButton radioButton;
    private MyLogRadioButton navigationButton;
    private MyLogRadioButton phoneButton;
    private MyLogRadioButton settingsButton;


    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Initialize UI

        fm.beginTransaction()
                .add(R.id.main_placeholder, settingsFragment, "settingsFragment")
                .add(R.id.main_placeholder, phoneFragment)
                .add(R.id.main_placeholder, navigationFragment)
                .add(R.id.main_placeholder, radioFragment)
                .add(R.id.main_placeholder, acFragment)
                .commit();

        currentFragment = acFragment;

        updateUI();


        //Keep screen on

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //Initialize fonts

        regularFont = ResourcesCompat.getFont(this, R.font.montserrat_regular);
        mediumFont = ResourcesCompat.getFont(this, R.font.montserrat_medium);


        //Initialize buttons

        acButton = findViewById(R.id.main_button_ac);
        radioButton = findViewById(R.id.main_button_radio);
        navigationButton = findViewById(R.id.main_button_navigation);
        phoneButton = findViewById(R.id.main_button_phone);
        settingsButton = findViewById(R.id.main_button_settings);


        //Menu button focus and fragment management

        acButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(acFragment);
            }
        });
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(radioFragment);
            }
        });

        navigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(navigationFragment);
            }
        });

        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(phoneFragment);
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(settingsFragment);
            }
        });


        //Initialize global MediaPlayer and Gson instance

        globalMediaPlayer = new MediaPlayer();

        gson = new Gson();


        //This variable is used as an indicator, to know if a fragment has been initialized and is ready for a task

        fragmentInitialized = false;


        //Get saved IP from SharedPreferences and try to connect to websocket server

        SharedPreferences sharedPrefs = getPreferences(Context.MODE_PRIVATE);
        String serverIP = sharedPrefs.getString(getString(R.string.current_ip), "192.168.0.2:80");
        connectToWebSocket(serverIP);

    }



    //Assign child fragments - used for task handling

    @Override
    protected void onStart() {
        super.onStart();

        FMRadioFragment = (FMRadioFragment) radioFragment.getChildFragmentManager().findFragmentByTag("FMRadioFragment");
        USBRadioFragment = (USBRadioFragment) radioFragment.getChildFragmentManager().findFragmentByTag("USBRadioFragment");
        BTRadioFragment = (BTRadioFragment) radioFragment.getChildFragmentManager().findFragmentByTag("BTRadioFragment");

        driveSettingsFragment = (DriveSettingsFragment) settingsFragment.getChildFragmentManager().findFragmentByTag("driveSettingsFragment");
        soundSettingsFragment = (SoundSettingsFragment) settingsFragment.getChildFragmentManager().findFragmentByTag("soundSettingsFragment");
        screenSettingsFragment = (ScreenSettingsFragment) settingsFragment.getChildFragmentManager().findFragmentByTag("screenSettingsFragment");

    }



    //Try to connect to websocket sever on given ip and port (url) and handle messages and failure

    public void connectToWebSocket(String url) {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://" + url).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {

                ws = webSocket;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Connection successful!", Toast.LENGTH_LONG).show();
                        settingsFragment.connectionActive();
                    }
                });

            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initTaskObject(text);
                    }
                });

            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {

            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {

                webSocket.close(1000, null);
                webSocket.cancel();

            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Connection closed!", Toast.LENGTH_LONG).show();
                        settingsFragment.connectionNotActive();
                    }
                });

            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Connection failed! Please, enter the IP of the server running", Toast.LENGTH_LONG).show();
                        settingsButton.performClick();
                        settingsFragment.openSlideUp();
                        settingsFragment.connectionNotActive();
                    }
                });
            }
        };

        client.newWebSocket(request, webSocketListenerCoinPrice);
        client.dispatcher().executorService().shutdown();
    }



    //Initialize TaskObject from (JSON) string

    public void initTaskObject(String s){

        try {
            JSONObject jsonObject = new JSONObject(s);
            taskObject = new TaskObject(jsonObject.getString("Name"), jsonObject.getString("Recepient"), jsonObject.getString("Property"),
                    jsonObject.getString("Init"), jsonObject.getString("Target"), jsonObject.getString("Description"),
                    jsonObject.getString("IdApp"));

            startTask();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    //Start the task - initialize target fragment and show a dialog with instructions to user

    public void startTask(){

        new MaterialDialog.Builder(this)
                .title(R.string.instruction_title)
                .content(taskObject.getDescription())
                .positiveText(R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        startTime = System.currentTimeMillis();
                    }
                })
                .typeface(mediumFont, regularFont)
                .canceledOnTouchOutside(false)
                .show();

        if (MainActivity.taskObject.getProperty().equals("volume")){
            FMRadioFragment.initFMRadioFragment();
            BTRadioFragment.initBTRadioFragment();
            BTRadioFragment.initBTRadioFragment();
            phoneButton.performClick();
        }

        //For radio fragments (FMRadioFragment, USBRadioFragment, BTRadioFragment) - the right fragment has to be chosen (checked), because of music

        switch (taskObject.getRecipient()){
            case "ACFragment":

                acFragment.initACFragment();

                navigationButton.performClick();

                break;
            case "FMRadioFragment":
                if (radioFragment.radioGroupSource.getCheckedRadioButtonId() != R.id.radio_button_fm) {
                    radioFragment.radioGroupSource.check(R.id.radio_button_fm);
                }
                FMRadioFragment.initFMRadioFragment();

                phoneButton.performClick();

                break;
            case "USBRadioFragment":
                if (radioFragment.radioGroupSource.getCheckedRadioButtonId() != R.id.radio_button_usb) {
                    radioFragment.radioGroupSource.check(R.id.radio_button_usb);
                }
                USBRadioFragment.initUSBRadioFragment();

                acButton.performClick();

                break;
            case "BTRadioFragment":
                if (radioFragment.radioGroupSource.getCheckedRadioButtonId() != R.id.radio_button_bt) {
                    radioFragment.radioGroupSource.check(R.id.radio_button_bt);
                }
                BTRadioFragment.initBTRadioFragment();

                navigationButton.performClick();

                break;
            case "navigationFragment":
                navigationFragment.initNavigationFragment();

                settingsButton.performClick();

                break;
            case "phoneFragment":
                phoneFragment.initPhoneFragment();

                radioButton.performClick();

                break;
            case "driveSettingsFragment":
                driveSettingsFragment.initDriveSettingsFragment();

                phoneButton.performClick();

                break;
            case "soundSettingsFragment":
                soundSettingsFragment.initSoundSettingsFragment();

                navigationButton.performClick();

                break;
            case "screenSettingsFragment":
                screenSettingsFragment.initScreenSettingsFragment();

                acButton.performClick();

                break;
        }

    }



    //End task - notify the user and send log to the server (with description of the action and the time it took to complete the task)

    public void endTask(String actionDescription){

        endTime = System.currentTimeMillis() - startTime;

        MaterialDialog dialog;

        //If actionDescription is R.string.task_canceled_description, the method knows, that the task was canceled

        if (actionDescription.equals(getString(R.string.task_canceled_description))) {
            dialog = new MaterialDialog.Builder(this)
                    .title(R.string.task_canceled_title)
                    .content(R.string.task_canceled)
                    .typeface(mediumFont, regularFont)
                    .canceledOnTouchOutside(false)
                    .show();
        }
        else {
            dialog = new MaterialDialog.Builder(this)
                    .title(R.string.task_completed_title)
                    .content(R.string.task_completed)
                    .typeface(mediumFont, regularFont)
                    .canceledOnTouchOutside(false)
                    .show();
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, 2000);

        sendLogToServer(taskObject.getProperty(), actionDescription, "true", Long.toString(endTime));

        taskObject = null;

    }



    //Checks if view is set to the value of target in TaskObject and calls endTask method if it is

    public void isViewSetToTarget(String value, String recipient, String property, String actionDescription){

        if (taskObject != null &&
                taskObject.getTarget().toLowerCase().equals(value.toLowerCase()) &&
                taskObject.getRecipient().equals(recipient) &&
                taskObject.getProperty().equals(property) &&
                fragmentInitialized){


            //Unlocks screen so it doesn't stay locked

            if (taskObject.getProperty().equals("lock_screen")){
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        screenSettingsFragment.unlockScreen();
                    }
                }, 2000);
            }

            fragmentInitialized = false;
            endTask(actionDescription);


        }
    }



    //Send log to server with all the needed data

    public static void sendLogToServer(String actionId, String actionDescription, String done, String taskCompletionTime){

        if (taskObject != null && ws != null){
            String currentTime = Calendar.getInstance().getTime().toString();
            logObject = new LogObject(idApp, taskObject.getName(), actionId, actionDescription, currentTime, done, taskCompletionTime);
            ws.send("##" + gson.toJson(logObject));
        }

    }



    //Switch fragment - used in menu

    public void switchFragment(Fragment fragment) {
        hideKeyboard(this);
        hideSystemUI();
        fm.beginTransaction()
                .hide(acFragment)
                .hide(radioFragment)
                .hide(navigationFragment)
                .hide(phoneFragment)
                .hide(settingsFragment)
                .show(fragment)
                .commit();
    }



    //Loading stations.json from Assets folder

    public String loadJSONFromAsset(String fileName) {
        String json;
        try {
            InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }



    //Disable all menu buttons

    public void lockMenuButtons(){
        radioButton.setEnabled(false);
        navigationButton.setEnabled(false);
        phoneButton.setEnabled(false);
        acButton.setEnabled(false);
        settingsButton.setEnabled(false);
    }



    //Enable all menu buttons

    public void unlockMenuButtons(){
        radioButton.setEnabled(true);
        navigationButton.setEnabled(true);
        phoneButton.setEnabled(true);
        acButton.setEnabled(true);
        settingsButton.setEnabled(true);
    }



    // Hide the keyboard

    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }



    //Methods to enable fullscreen (hide status and navigation bar)

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    public void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public void updateUI() {
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }
        });
    }



    //Update UI on resume

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }



    //Release MediaPlayer object

    @Override
    protected void onDestroy() {
        super.onDestroy();
        globalMediaPlayer.release();
    }

}
