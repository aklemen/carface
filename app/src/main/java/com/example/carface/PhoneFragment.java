package com.example.carface;

import android.content.res.AssetFileDescriptor;
import android.graphics.Typeface;
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
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class PhoneFragment extends Fragment  implements View.OnClickListener {


    //region Variables


    //Number pad

    private EditText dialText;

    private MyLogButton buttonZero;
    private MyLogButton buttonOne;
    private MyLogButton buttonTwo;
    private MyLogButton buttonThree;
    private MyLogButton buttonFour;
    private MyLogButton buttonFive;
    private MyLogButton buttonSix;
    private MyLogButton buttonSeven;
    private MyLogButton buttonEight;
    private MyLogButton buttonNine;
    private MyLogButton buttonStar;
    private MyLogButton buttonHashtag;

    //Call buttons

    private MyLogImageButton buttonContacts;
    private MyLogImageButton buttonCall;
    private MyLogImageButton buttonMute;
    private MyLogImageButton buttonDelete;

    //List of contacts

    private MyLogImageButton buttonClose;
    private MyLogButton buttonSortName;
    private ImageView sortArrow;
    private EditText searchView;
    private MyLogImageButton buttonClear;

    private List<Contact> listOfContacts;
    private List<Contact> listOfContactsByName;

    public SlideUp slideUpList;
    private View slideView;
    private RecyclerView recyclerView;
    private ListOfContactsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private String currentListState = "nameAsc";

    //Fonts

    public Typeface regularFont;
    public Typeface mediumFont;

    //MediaPlayer for ringing

    private MediaPlayer mediaPlayer;


    //endregion


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone, container, false);


        //Initialize new list of contacts

        listOfContacts = new ArrayList<>();


        //Get stations from stations.json, add them to the list of RadioStation objects and sort them by frequency

        try {
            JSONObject jsonObject = new JSONObject(((MainActivity)getActivity()).loadJSONFromAsset("contacts.json"));
            JSONArray jsonArray = jsonObject.getJSONArray("contacts");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject stationJSONObject = jsonArray.getJSONObject(i);
                Contact contact = new Contact(i, stationJSONObject.getString("name"), stationJSONObject.getString("number"));
                listOfContacts.add(contact);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //Sort list of stations and create a dedicated list for this sort

        sortContactsByName(listOfContacts);
        listOfContactsByName = new ArrayList<>(listOfContacts);

        for (int i = 0; i < listOfContacts.size(); i++){
            listOfContacts.get(i).setId(i);
            listOfContactsByName.get(i).setId(i);
        }


        //Initialize fonts

        regularFont = ResourcesCompat.getFont(getActivity(), R.font.montserrat_regular);
        mediumFont = ResourcesCompat.getFont(getActivity(), R.font.montserrat_medium);


        //Initialize views and set listeners

        dialText = view.findViewById(R.id.phone_edit_number);

        buttonZero = view.findViewById(R.id.phone_button_0);
        buttonOne = view.findViewById(R.id.phone_button_1);
        buttonTwo = view.findViewById(R.id.phone_button_2);
        buttonThree = view.findViewById(R.id.phone_button_3);
        buttonFour = view.findViewById(R.id.phone_button_4);
        buttonFive = view.findViewById(R.id.phone_button_5);
        buttonSix = view.findViewById(R.id.phone_button_6);
        buttonSeven = view.findViewById(R.id.phone_button_7);
        buttonEight = view.findViewById(R.id.phone_button_8);
        buttonNine = view.findViewById(R.id.phone_button_9);
        buttonStar = view.findViewById(R.id.phone_button_star);
        buttonHashtag = view.findViewById(R.id.phone_button_hashtag);

        buttonContacts = view.findViewById(R.id.phone_button_contacts);
        buttonCall = view.findViewById(R.id.phone_button_call);
        buttonMute = view.findViewById(R.id.phone_button_mute);
        buttonDelete = view.findViewById(R.id.phone_button_delete);

        buttonClose = view.findViewById(R.id.phone_button_close);
        buttonSortName = view.findViewById(R.id.phone_button_sortName);
        sortArrow = view.findViewById(R.id.phone_image_arrow);
        searchView = view.findViewById(R.id.phone_edit_search);
        buttonClear = view.findViewById(R.id.phone_button_clear);

        buttonZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialText.append("0");
            }
        });

        buttonZero.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialText.append("+");
                return true;
            }
        });

        buttonOne.setOnClickListener(this);
        buttonTwo.setOnClickListener(this);
        buttonThree.setOnClickListener(this);
        buttonFour.setOnClickListener(this);
        buttonFive.setOnClickListener(this);
        buttonSix.setOnClickListener(this);
        buttonSeven.setOnClickListener(this);
        buttonEight.setOnClickListener(this);
        buttonNine.setOnClickListener(this);
        buttonStar.setOnClickListener(this);
        buttonHashtag.setOnClickListener(this);

        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialText.getText().length() > 0) {

                    mediaPlayer = new MediaPlayer();
                    AssetFileDescriptor descriptor;
                    try {
                        descriptor = getActivity().getAssets().openFd("calling.mp3");
                        mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                        descriptor.close();
                        mediaPlayer.setLooping(true);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    MainActivity.globalMediaPlayer.pause();

                    String s = "Calling " + dialText.getText().toString() + " ...";
                    new MaterialDialog.Builder(getActivity())
                            .content(s)
                            .positiveText(R.string.cancel)
                            .typeface(mediumFont, regularFont)
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    mediaPlayer.stop();
                                    mediaPlayer.release();
                                    MainActivity.globalMediaPlayer.start();
                                }
                            })
                            .canceledOnTouchOutside(false)
                            .show();

                    String number = dialText.getText().toString().replaceAll("\\s","");


                    //Check to end task, if target is reached

                    ((MainActivity)getActivity()).isViewSetToTarget(number, "phoneFragment", "phone_number", "Call successful.");

                }
                else {
                    Toast.makeText(getActivity(), "Please, fill in the number.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        buttonMute.setOnClickListener(new View.OnClickListener() {

            public void onClick(View button) {
                button.setSelected(!button.isSelected());
            }

        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int length = dialText.getText().length();
                if (length > 0) {
                    dialText.getText().delete(length - 1, length);
                }
            }
        });


        //Initialize list of contacts (slideview, recyclerview ...)

        slideView = view.findViewById(R.id.phone_relative_slideView);
        recyclerView = view.findViewById(R.id.phone_recycler_contacts);

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

        adapter = new ListOfContactsAdapter(listOfContacts, this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider));

        recyclerView.addItemDecoration(itemDecorator);


        //Set OnClick listeners for showing and closing search, sorting contacts and clearing searchView

        buttonContacts.setOnClickListener(new View.OnClickListener() {
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
                Collections.reverse(adapter.getListOfContacts());
                switch (currentListState) {
                    case "nameAsc":
                        sortArrow.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_up, null));
                        currentListState = "nameDesc";
                        break;
                    case "nameDesc":
                        sortArrow.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_down, null));
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


        return view;

    }



    //Fill in the number from list of contacts at selected index

    public void fillNumber(int index){
        dialText.setText(listOfContactsByName.get(index).getContactNumber());
    }



    //Sorting method

    public void sortContactsByName(List<Contact> list){
        Collections.sort(list, new Comparator<Contact>() {
            @Override
            public int compare(Contact first, Contact second) {
                return first.getContactName().toLowerCase().compareTo(second.getContactName().toLowerCase());
            }
        });
    }



    //Filter for search

    private void filter (String text){
        List<Contact> filteredList = new ArrayList<>();
        for(Contact c : listOfContacts){
            if (c.getContactName().toLowerCase().contains(text.toLowerCase()) || c.getContactNumber().contains(text)){
                filteredList.add(c);
            }
        }
        adapter.filterList(filteredList);
    }



    //Click listener for number pad

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.phone_button_1:
                dialText.append("1");
                break;
            case R.id.phone_button_2:
                dialText.append("2");
                break;
            case R.id.phone_button_3:
                dialText.append("3");
                break;
            case R.id.phone_button_4:
                dialText.append("4");
                break;
            case R.id.phone_button_5:
                dialText.append("5");
                break;
            case R.id.phone_button_6:
                dialText.append("6");
                break;
            case R.id.phone_button_7:
                dialText.append("7");
                break;
            case R.id.phone_button_8:
                dialText.append("8");
                break;
            case R.id.phone_button_9:
                dialText.append("9");
                break;
            case R.id.phone_button_star:
                dialText.append("*");
                break;
            case R.id.phone_button_hashtag:
                dialText.append("#");
                break;
        }
    }



    //Initialize fragment for task

    public void initPhoneFragment(){
        dialText.setText("");
        MainActivity.fragmentInitialized = true;
    }

}