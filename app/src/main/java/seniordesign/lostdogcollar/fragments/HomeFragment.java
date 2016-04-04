package seniordesign.lostdogcollar.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import seniordesign.lostdogcollar.Collar;
import seniordesign.lostdogcollar.CollarListRVAdapter;
import seniordesign.lostdogcollar.fragments.dialogs.SafeZoneDialogFragment;
import seniordesign.lostdogcollar.listeners.MyResponseListener;
import seniordesign.lostdogcollar.listeners.OnDisplayMapListener;
import seniordesign.lostdogcollar.listeners.OnSendResponseListener;
import seniordesign.lostdogcollar.R;
import seniordesign.lostdogcollar.RetrieveFromServerAsyncTask;
import seniordesign.lostdogcollar.fragments.dialogs.AddCollarDialogFragment;
import seniordesign.lostdogcollar.ResponseConverterUtil;


// TODO: implement notification stopper
// TODO: callback for when login/register message actually received instead of doing thread.sleep
public class HomeFragment extends MapsBaseFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, SafeZoneDialogFragment.OnSendRadiusListener,
        OnDisplayMapListener, CollarListRVAdapter.OnSendCollarIdListener,
        AddCollarDialogFragment.OnRefreshCollarsListener {

    private static final String USERNAME = "username";

    private static final int RESOLVE_ERROR_CODE = 1001;
    private static final String TAG = "HomeFragment";

    private String username;

    private MapView mapView;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private List<String> safezones;
    private HashMap<Integer, ArrayList<Circle>> circleMap;
    private LatLng dogLastLoc = null;
    private CallbackManager callbackManager;
    private Handler mHandler;
    private LocalBroadcastManager lbm;
    private CollarListRVAdapter adapter;
    private ImageView image;
    private BottomSheetBehavior behavior;
    private List<Collar> collarList;
    private Location lastUserLocation;
    private int collarId;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String username) {
        HomeFragment fragment = new HomeFragment();
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

        initCircleMap();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        setmResolvingError(false);
        safezones = new ArrayList<>();

        collarList = new ArrayList<>();
        mHandler = new Handler();

        // sleep so user has time to register
        // TODO: find better way to do this
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }

        collarId = -1;

        adapter = new CollarListRVAdapter(collarList, getContext(), this);
        initCollarList();

        lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.registerReceiver(receiver, new IntentFilter("update-notif-prefs"));
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);
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

        Button stopNotifsButton = (Button) view.findViewById(R.id.stop_notifs_button);
        stopNotifsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendMessage("STOP NOTIFICATIONS");
                SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string
                        .prefs_name), 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("notifTime", 0);  // this means don't send notifs unless dog lost
                editor.apply();
            }
        });

        SwitchCompat ledSwitch = (SwitchCompat) view.findViewById(R.id.led_switch);
        ledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // turn light on if switch is on
                    sendMessage("NEW_MESSAGE " + collarId + " LIGHT_ON\r\n");
                } else {
                    sendMessage("NEW_MESSAGE " + collarId + " LIGHT_OFF\r\n");
                }
            }
        });

        setupBottomSheet(view);
        setupRecyclerView(view);

        return view;
    }

    /**
     * listener for map click to begin "add safezone" task
     */
    private void setOnMapClickListener() {
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //Log.d(TAG, "clicked area: " + latLng);
                DialogFragment safeZoneDialog = SafeZoneDialogFragment.newInstance(latLng
                        .latitude, latLng.longitude);
                safeZoneDialog.show(getChildFragmentManager(), "dialog");
            }
        });
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


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Snackbar.make(getActivity().findViewById(R.id.coord_layout), "Tap area on map to add " +
                "safe zone", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { }
                }).show();
    }

    @Override
    public void refreshCollarList() {
        initCollarList();
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
        dogLastLoc = latLng;
        //Log.d(TAG, "latlng: " + latLng);

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
    public void onSendCollarId(int id) {
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

            lastUserLocation = LocationServices.FusedLocationApi.getLastLocation(
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
     * sends just created safe zone to server
     *
     * is called by
     * {@link SafeZoneDialogFragment.OnSendRadiusListener}
     * from {@link SafeZoneDialogFragment}
     *
     * @param radius radius in meters of safe zone
     */
    public void sendSafezoneToServer(int radius, double lat, double longi) {
        //Log.d(TAG, "safezone latlng: " + lat + "," + longi);

        String message = "NEW_SAFEZONE " + collarId + " (" + lat + "," + longi + ")" +
                " " + radius + "\r\n";

        map.addCircle(new CircleOptions()
                .center(new LatLng(lat, longi))
                .radius(radius)
                .fillColor(0x44ff0000)
                .strokeColor(0xffff0000)
                .strokeWidth(0));

        sendMessage(message);
    }

    /**
     * sends message to server for storage
     *
     * @param message command to be sent to server
     */
    private void sendMessage(String message) {
        //Log.d(TAG, "message: " + message);

        RetrieveFromServerAsyncTask rsat = new RetrieveFromServerAsyncTask(new MyResponseListener
                (getContext(), this));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            rsat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } else {
            rsat.execute(message);
        }
    }

    /**
     * Open Facebook share dialog to allow user to post pet's last known location to Facebook
     */
    private void postToFacebook() {
        if (checkIfLoggedIn()) {
            ShareDialog share = new ShareDialog(this);
            if (ShareDialog.canShow(ShareLinkContent.class)) {

                // there are no locations saved for dog so show user's location
                if (dogLastLoc == null) {
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentTitle("My Dog is Lost!")
                            .setContentDescription("Please help me find my dog. This is where I " +
                                    "am right now.")
                            .setContentUrl(Uri.parse("https://www.google.com/maps/?q=" + lastUserLocation
                                    .getLatitude()
                                    + "," + lastUserLocation.getLongitude()))
                            .build();

                    share.show(linkContent);
                }
                else {
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentTitle("My Dog's Location")
                            .setContentDescription("This is where my dog was last seen")
                            .setContentUrl(Uri.parse("https://www.google.com/maps/?q=" + dogLastLoc.latitude
                                    + "," + dogLastLoc.longitude))
                        /*.setContentUrl(Uri.parse("https://www.google.com/maps/?q=30,90"))*/
                            .build();

                    share.show(linkContent);
                }
            }
        }
        else {
            Log.i(TAG, "not logged into facebook");
            Snackbar.make(getActivity().findViewById(R.id.coord_layout), "Please log into " +
                    "Facebook to share", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Log In", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            logIntoFacebook();
                        }
                    }).show();
        }
    }

    /**
     * logs user into Facebook after user clicks Share button and they are not signed in
     * stores accessToken in SharedPreferences
     */
    private void logIntoFacebook() {
        LoginManager loginManager = LoginManager.getInstance();

        Collection<String> permissions = Collections.singletonList("publish_actions");
        loginManager.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK)
                .logInWithPublishPermissions(this, permissions);

        callbackManager = CallbackManager.Factory.create();
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                AccessToken token = loginResult.getAccessToken();
                SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string
                        .prefs_name), 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("accessToken", token.getToken());
                editor.apply();

                postToFacebook();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "login cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }

    /**
     * checks if user is logged into facebook
     * @return true if logged into facebook, false if not
     */
    private boolean checkIfLoggedIn() {
        SharedPreferences prefs = getActivity().getSharedPreferences(getResources().getString(R
                .string.prefs_name), 0);

        return prefs.getString("accessToken", null) != null;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected");

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "location permission granted");
            displayMap();
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
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_settings: {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.content_frag, SettingsPageFragment.newInstance())
                        .commit();
                break;
            }

            case R.id.remove_safezone:
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.content_frag, RemoveSafezonesFragment.newInstance
                        (username)).commit();
                break;

            case R.id.app_add_collar:
                DialogFragment addCollarDialog = AddCollarDialogFragment.newInstance();
                addCollarDialog.show(getChildFragmentManager(), "dialog");
                break;

            case R.id.refresh_map:
                displayMap();
                break;

            case R.id.post_to_fb:
                postToFacebook();
                break;

            case R.id.app_logout:
                logout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * redisplays map for certain collar
     */
    @Override
    public void displayMap() {
        safezones.clear();

        if (circleMap.get(collarId) != null) {
            circleMap.get(collarId).clear();
        }

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
                if (response.equals("LOGGED OUT")) {
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

    /**
     * when phone receives notification, broadcast sent to HomeFragment to update location
     * periodically. Specifically, update every notifTime seconds.
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int notifTime = intent.getIntExtra("notifTime", 0);

            new Runnable() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            map.clear();
                            sendMessage("GET_MESSAGES 0 ");
                        }
                    });

                    mHandler.postDelayed(this, notifTime);
                }
            };
        }
    };

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
        lbm.unregisterReceiver(receiver);

        SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string
                .prefs_name), 0);
        prefs.edit().putBoolean("initCollarList", false).apply();
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
        else {
            if (callbackManager == null) {
                callbackManager = CallbackManager.Factory.create();
            }
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

    }

}