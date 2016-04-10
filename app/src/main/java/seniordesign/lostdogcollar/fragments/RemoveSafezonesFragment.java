package seniordesign.lostdogcollar.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import seniordesign.lostdogcollar.Collar;
import seniordesign.lostdogcollar.views.CollarListRVAdapter;
import seniordesign.lostdogcollar.fragments.dialogs.RemoveSafeZoneDialogFragment;
import seniordesign.lostdogcollar.listeners.MyResponseListener;
import seniordesign.lostdogcollar.listeners.OnDisplayMapListener;
import seniordesign.lostdogcollar.listeners.OnSendResponseListener;
import seniordesign.lostdogcollar.R;
import seniordesign.lostdogcollar.RetrieveFromServerAsyncTask;
import seniordesign.lostdogcollar.ResponseConverterUtil;


public class RemoveSafezonesFragment extends MapsBaseFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, RemoveSafeZoneDialogFragment
                .OnRemoveSafeZoneListener,
        OnDisplayMapListener, CollarListRVAdapter.OnSendCollarIdListener {

    private static final String USERNAME = "username";

    private static final int RESOLVE_ERROR_CODE = 1001;
    private static final String TAG = "RemoveSzFragment";

    private String username;

    private MapView mapView;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private List<String> safezones;
    private HashMap<Integer, ArrayList<Circle>> circleMap;
    private CollarListRVAdapter adapter;
    private ImageView image;
    private BottomSheetBehavior behavior;
    private List<Collar> collarList;
    private ArrayList<Circle> circleList;
    private int collarId;

    public RemoveSafezonesFragment() {
        // Required empty public constructor
    }

    public static RemoveSafezonesFragment newInstance(String username) {
        RemoveSafezonesFragment fragment = new RemoveSafezonesFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "oncreate");

        if (getArguments() != null) {
            username = getArguments().getString(USERNAME);
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        setmResolvingError(false);
        safezones = new ArrayList<>();

        initCircleMap();

        collarList = new ArrayList<>();

        // sleep so user has time to register
        // TODO: find better way to do this
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }

        collarId = -1;

        adapter = new CollarListRVAdapter(collarList, this);
        initCollarList();
    }

    private void initCircleMap() {
        circleMap = new HashMap<>();
        circleMap.put(0, new ArrayList<Circle>());
        circleMap.put(1, new ArrayList<Circle>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_remove_safezones, container, false);
        setupToolbar((Toolbar) view.findViewById(R.id.toolbar), getString(R.string.app_name));
        setHasOptionsMenu(true);

        checkLocationServicesEnabled();

        image = (ImageView) view.findViewById(R.id.view_collar_list_btn);

        // MapsInitializer.initialize(getContext());
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                setOnMapClickListener();
                addCircles();
            }
        });

        setupBottomSheet(view);
        setupRecyclerView(view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Snackbar.make(getActivity().findViewById(R.id.coord_layout), "Tap safe zone to remove",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { }
                }).show();
    }

    /**
     * listener for map click to begin "add safezone" task
     */
    private void setOnMapClickListener() {
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                circleList = circleMap.get(collarId);

                for (int i = 0; i < circleList.size(); i++) {
                    LatLng center = circleList.get(i).getCenter();
                    double radius = circleList.get(i).getRadius();
                    float[] distance = new float[1];
                    Location.distanceBetween(latLng.latitude, latLng.longitude, center.latitude,
                            center.longitude, distance);

                    if (distance[0] < radius) {
                        // open dialog asking to remove circle
                        DialogFragment removeSafeZoneDialog = RemoveSafeZoneDialogFragment
                                .newInstance(center, radius, i);
                        removeSafeZoneDialog.show(getChildFragmentManager(), "dialog");

                        break;
                    }
                }
            }
        });
    }

    @Override
    public void removeSafezoneFromServer(LatLng center, double radius, int index) {
        sendMessage("REMOVE_SAFEZONE " + collarId + " (" + center.latitude + ","
                + center.longitude + ") " + (int) radius + "\r\n");
        circleList.get(index).remove();
    }

    private void setupBottomSheet(View view) {
        // The View with the BottomSheetBehavior
        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);

        RelativeLayout relLay = (RelativeLayout) view.findViewById(R.id.view_collars_layout);
        relLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBehaviorChange();
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBehaviorChange();
            }
        });
    }

    private void onBehaviorChange() {
        Log.d(TAG, "" + behavior.getState());
        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            image.setImageResource(R.drawable.ic_arrow_drop_down);
        } else if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            image.setImageResource(R.drawable.ic_arrow_drop_up);
        }
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.collar_rv);
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setAdapter(adapter);
    }

    /**
     * get the list of collars for a certain user
     */
    private void initCollarList() {
        String message = "GET_COLLARS \r\n";
        RetrieveFromServerAsyncTask rsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                Log.d(TAG, response);
                // TODO: find out why this is called twice
                if (response.equals("")) {
                    return;
                }
                List<Collar> collars = ResponseConverterUtil.convertResponseToCollarList(response);

                if (collars.size() == 0) {
                    // TODO: display dialog saying 'you have no saved collars!'
                    return;
                }

                collarList.clear();
                collarList.addAll(collars);

                // add new circle list for safezones if it's a new collar
                for (int i = 0; i < collarList.size(); i++) {
                    if (circleMap.get(collarList.get(i).getId()) == null) {
                        circleMap.put(collarList.get(i).getId(), new ArrayList<Circle>());
                    }
                }

                //Log.d(TAG, "init collar list " + Thread.currentThread().getId());
                Log.d(TAG, "size: " + collarList.size());
                collarId = collarList.get(0).getId();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        displayMap();
                    }
                });
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            rsat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } else {
            rsat.execute(message);
        }
    }

    /**
     * Displays dog's last known location on map
     *
     * @param response response from server containing latest location
     */
    @Override
    public void displayDogsLocation(String response) {
        List<String> records = ResponseConverterUtil.convertResponseToList(response);
        //Log.d(TAG, "displayLocation: record 1: " + records.get(0));

        final LatLng latLng = ResponseConverterUtil.convertRecordsString(records.get(0));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                map.animateCamera(camera);

                map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Last Known Location"));
            }
        });
    }

    /**
     * sends collarId from {@link CollarListRVAdapter} to display selected collar
     * @param id collar ID
     */
    @Override
    public void onSendCollarId(int id, String name) {
        collarId = id;

        //hide the list again
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        image.setImageResource(R.drawable.ic_arrow_drop_up);
        displayMap();
    }

    /**
     * displays user's location if a collar has no previous locations saved
     */
    @Override
    public void displayOwnLocation() {

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission
                .ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            if (map == null) {} // I'm sure there's a better way to do this

            Location lastUserLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (lastUserLocation != null) {
                LatLng latLng = new LatLng(lastUserLocation.getLatitude(), lastUserLocation
                        .getLongitude());
                final CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(latLng, 15);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        map.animateCamera(camera);
                    }
                });
            }
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(getActivity().findViewById(R.id.coord_layout), "No previous " +
                        "locations found for dog", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        }).show();
            }
        });
    }

    /**
     * displays safezones for selected collar
     * @param safezonesList list of safezones for collar
     */
    @Override
    public void displaySafezones(List<String> safezonesList) {
        safezones.addAll(safezonesList);
        addCircles();
    }

    /**
     * converts response string containing safezones into list of safezones and coordinates.
     * displays these safe zones on map.
     */
    private void addCircles() {
        for (String sz : safezones) {
            // splits string by any # of consecutive spaces
            // safezone[0] = dogLastLoc
            // safezone[1] = radius
            final String[] safezone = sz.split("\\s+");
            final LatLng coords = ResponseConverterUtil.convertCoordsString(safezone[0]);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Circle circle = map.addCircle(new CircleOptions()
                            .center(coords)
                            .radius(Double.parseDouble(safezone[1]))
                            .fillColor(0x44ff0000)
                            .strokeColor(0xffff0000)
                            .strokeWidth(0));

                    ArrayList<Circle> list = circleMap.get(collarId);

                    if (!list.contains(circle)) {
                        list.add(circle);
                    }

                    circleMap.put(collarId, list);
                }
            });
        }
    }

    /**
     * sends message to server for storage
     *
     * @param message command to be sent to server
     */
    private void sendMessage(String message) {
        Log.d(TAG, "message: " + message);

        RetrieveFromServerAsyncTask rsat = new RetrieveFromServerAsyncTask(new MyResponseListener
                (getContext(), this));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            rsat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } else {
            rsat.execute(message);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected");

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "location permission granted");
            //displayMap();
        } else {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            String[] reasons = {"Location is necessary to view pet's current location on Google " +
                    "Maps"};
            int[] codes = {DOG_LOCATION_CODE};
            getPermission(permissions, reasons, codes);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.d(TAG, "connection failed: " + result.getErrorMessage());

        if (getmResolvingError()) { return; }

        if (result.hasResolution()) {
            try {
                setmResolvingError(true);
                //mResolvingError = true;
                result.startResolutionForResult(getActivity(), RESOLVE_ERROR_CODE);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            //mResolvingError = true;
            setmResolvingError(true);
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        // I don't know what to do here


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.remove_sz_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_settings: {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.content_frag, SettingsPageFragment.newInstance(collarId))
                        .commit();
                break;
            }

            case R.id.refresh_map:
                displayMap();
                break;

            case R.id.app_logout: {
                logout();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * redisplays map for certain collar
     */
    @Override
    public void displayMap() {
        safezones.clear();
        circleMap.get(collarId).clear();

        // if map already initialized, you can clear it
        if (map != null) {
            map.clear();
        }
        sendMessage("GET_RECORDS " + collarId + " 1 ");
        sendMessage("GET_SAFEZONES " + collarId + " \r\n");
    }

    /**
     * log user out of app and clears any saved preferences
     */
    private void logout() {
        // forget user after logout
        SharedPreferences prefs = getActivity().getSharedPreferences(getResources()
                .getString(R.string.prefs_name), 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        String message = "LOGOUT " + username;

        RetrieveFromServerAsyncTask rsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                if (response.equals("OK")) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frag, LoginFragment.newInstance())
                            .commit();
                }
                else {
                    Log.d(TAG, "could not log out: " + response);
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            rsat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } else {
            rsat.execute(message);
        }
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESOLVE_ERROR_CODE) {
            setmResolvingError(false);
            if (resultCode == Activity.RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

}