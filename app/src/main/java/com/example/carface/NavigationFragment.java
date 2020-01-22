package com.example.carface;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.madapps.placesautocomplete.PlaceAPI;
import in.madapps.placesautocomplete.adapter.PlacesAutoCompleteAdapter;
import in.madapps.placesautocomplete.model.Place;


public class NavigationFragment extends Fragment implements OnMapReadyCallback {

    //IMPORTANT!!!
    //This fragment needs API key with Maps API, Places API and Direction API enabled to work properly


    //region Variables


    //Static values for bundle, permissions and map zoom

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final float DEFAULT_ZOOM = 16f;

    //Map and polyline for directions

    private MapView mapView;
    private GoogleMap googleMap;
    private Marker marker;
    private Polyline polyline;
    private FusedLocationProviderClient fusedLocationProviderClient;

    //Lat and lng for location

    private LatLng currentDestinationLocation;
    private LatLng currentDeviceLocation;

    //UI

    private MyLogImageButton buttonMyLocation;
    private AutoCompleteTextView searchView;
    private MyLogImageButton buttonClear;
    private MyLogImageButton buttonNavigate;
    private MyLogImageButton buttonCancel;


    //endregion


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_navigation, container, false);


        //Checking for location permission

        checkLocationPermission();


        //FusedLocationProviderClient for getting the device location later

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());


        //Initialize the map

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView = view.findViewById(R.id.navigation_map);
        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);


        //Initialize views

        buttonMyLocation = view.findViewById(R.id.navigation_button_myLocation);
        searchView = view.findViewById(R.id.navigation_edit_search);
        buttonClear = view.findViewById(R.id.navigation_button_clear);
        buttonNavigate = view.findViewById(R.id.navigation_button_navigate);
        buttonCancel = view.findViewById(R.id.navigation_button_cancel);


        //Add places autosuggestions, initialize search, add button and other listeners

        buttonMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

        PlaceAPI placesApi = new PlaceAPI.Builder().apiKey(getString(R.string.api_key)).build(getActivity());
        searchView.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), placesApi));
        searchView.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_black_50, null));

        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Place place = (Place) parent.getItemAtPosition(position);
                searchView.setText(place.getDescription());
                geoLocate(searchView.getText().toString());
                ((MainActivity)getActivity()).hideKeyboard(getActivity());
            }
        });

        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER){
                    geoLocate(searchView.getText().toString());
                }
                return false;
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
                buttonNavigate.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s){
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setText("");
            }
        });

        buttonNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                navigate(currentDestinationLocation);


                //Check to end task, if target is reached

                if (MainActivity.taskObject != null &&
                    searchView.getText().toString().toLowerCase().contains(MainActivity.taskObject.getTarget().toLowerCase()) &&
                    MainActivity.fragmentInitialized){

                    ((MainActivity)getActivity()).endTask("Navigation started.");
                    MainActivity.fragmentInitialized = false;

                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelNavigation();
            }
        });


        return view;

    }



    //Navigate, show route to any destination from current device location

    private void navigate(LatLng destination){

        buttonNavigate.setVisibility(View.GONE);
        buttonCancel.setVisibility(View.VISIBLE);
        getDeviceLocation();
        GoogleDirection.withServerKey(getString(R.string.api_key))
                .from(currentDeviceLocation)
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
//                        Toast.makeText(getActivity(),"Request status: " + direction.getStatus(), Toast.LENGTH_LONG).show();
                        if(direction.isOK()) {
                            Toast.makeText(getActivity(), "Navigation started.", Toast.LENGTH_SHORT).show();
                            Route route = direction.getRouteList().get(0);
                            ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
                            if (polyline != null){
                                polyline.remove();
                            }
                            polyline = googleMap.addPolyline(DirectionConverter.createPolyline(getActivity(),
                                    directionPositionList, 5, Color.BLUE));

                        } else {
                            Toast.makeText(getActivity(), "Sorry, directions aren't available.", Toast.LENGTH_LONG).show();
                            cancelNavigation();
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Toast.makeText(getActivity(), "Sorry, directions aren't available.", Toast.LENGTH_LONG).show();
                        cancelNavigation();
                    }
                });
    }



    //Cancel the navigation

    private void cancelNavigation(){
        buttonCancel.setVisibility(View.GONE);
        if (polyline != null){
            polyline.remove();
            Toast.makeText(getActivity(), "Navigation canceled.", Toast.LENGTH_SHORT).show();
        }
    }



    //Locate any destination with a String, zoom in there

    private void geoLocate(String location){
        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> addressList = new ArrayList<>();

        try {
            addressList = geocoder.getFromLocationName(location, 1);

        }catch (IOException e){

        }

        if (addressList.size() > 0){
            Address address = addressList.get(0);
            currentDestinationLocation = new LatLng(address.getLatitude(), address.getLongitude());
            moveCamera(currentDestinationLocation);
            marker.setPosition(currentDestinationLocation);
            marker.setTitle(address.getAddressLine(0));
            buttonNavigate.setVisibility(View.VISIBLE);
        }
        else{
            Toast.makeText(getActivity(), "Location wasn't found.", Toast.LENGTH_LONG).show();
        }
    }



    //Move the camera to selecter LatLng

    private void moveCamera(LatLng latLng){
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }



    //Stylize the map, change UI settings, change zoom to current location

    @Override
    public void onMapReady(GoogleMap map) {

        googleMap = map;

        try {
            String jObject = ((MainActivity)getActivity()).loadJSONFromAsset("mapStyle.json");

            MapStyleOptions styleOptions = new MapStyleOptions(jObject);
            boolean success = googleMap.setMapStyle(styleOptions);

        } catch (Resources.NotFoundException e) {
        }

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setCompassEnabled(false);
        }

        getDeviceLocation();
    }



    //Get devices location

    private void getDeviceLocation() {
        try {
            Task locationResult = fusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Location location = (Location) task.getResult();
                        currentDeviceLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentDeviceLocation, DEFAULT_ZOOM));
                        if (marker == null){
                            marker = googleMap.addMarker(new MarkerOptions().position(currentDeviceLocation).title("My location"));
                        }
                    }
                }
            });
        } catch(SecurityException e)  {
        }
    }



    //Methods for getting the location permission

    public boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(getActivity())
                        .setTitle("Location permission")
                        .setMessage("We need location permission. Please change it in the settings.")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(getActivity(),"Permission granted.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getActivity(),"Permission denied.", Toast.LENGTH_SHORT).show();
                }

            }

        }
    }



    //Initialize fragment for task

    public void initNavigationFragment(){
        searchView.setText("");
        cancelNavigation();
        MainActivity.fragmentInitialized = true;
    }



    //region obligatory methods because of mapView

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    //endregion

}