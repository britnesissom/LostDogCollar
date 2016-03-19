package seniordesign.lostdogcollar.fragments;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import seniordesign.lostdogcollar.Collar;
import seniordesign.lostdogcollar.CollarListRVAdapter;
import seniordesign.lostdogcollar.listeners.MyResponseListener;
import seniordesign.lostdogcollar.listeners.OnDisplayMapListener;
import seniordesign.lostdogcollar.listeners.OnSendResponseListener;
import seniordesign.lostdogcollar.R;
import seniordesign.lostdogcollar.async.RetrieveFromServerAsyncTask;
import seniordesign.lostdogcollar.utils.ResponseConverterUtil;

public class MultipleCollarsMapFragment extends MapsBaseFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, CollarListRVAdapter.OnSendCollarIdListener,
        OnDisplayMapListener {

    private static final String TAG = "MultiCollarFrag";
    private static final int RESOLVE_ERROR_CODE = 1001;

    private GoogleMap map;
    private MapView mapView;
    private GoogleApiClient mGoogleApiClient;
    private CollarListRVAdapter adapter;
    private BottomSheetBehavior behavior;
    private List<Collar> collarList;
    private List<String> safezones;
    private int collarId;

    public MultipleCollarsMapFragment() {
        // Required empty public constructor
    }

    public static MultipleCollarsMapFragment newInstance() {
        return new MultipleCollarsMapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        setmResolvingError(false);

        collarList = new ArrayList<>();
        safezones = new ArrayList<>();
        collarId = -1;

        adapter = new CollarListRVAdapter(collarList, getContext(), this);

        String message = "GET_COLLARS \r\n";
        RetrieveFromServerAsyncTask rsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                List<Collar> collars = ResponseConverterUtil.convertResponseToCollarList(response);
                collarList.clear();
                collarList.addAll(collars);
                adapter.notifyDataSetChanged();

                collarId = collarList.get(0).getId();
                checkPermission();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            rsat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } else {
            rsat.execute(message);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.multiple_maps_layout, container, false);
        setupToolbar((Toolbar) view.findViewById(R.id.toolbar), "Lost Dog Collar");
        setHasOptionsMenu(true);

        final ImageView image = (ImageView) view.findViewById(R.id.view_collar_list_btn);

        RelativeLayout relLay = (RelativeLayout) view.findViewById(R.id.view_collars_layout);
        relLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    image.setImageResource(R.drawable.ic_arrow_drop_down);
                } else if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    image.setImageResource(R.drawable.ic_arrow_drop_up);
                }
            }
        });

        // MapsInitializer.initialize(getContext());
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
            }
        });

        setupBottomSheet(view);
        setupRecyclerView(view);

        return view;
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            String[] reasons = {"Location is necessary to view pet's current location on Google " +
                    "Maps"};
            int[] codes = {DOG_LOCATION_CODE};
            getPermission(permissions, reasons, codes);
        }
        else {
            displayMap();
        }
    }

    private void setupBottomSheet(View view) {
        // The View with the BottomSheetBehavior
        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {

                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        });
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
    void displayMap() {
        sendMessage("GET_RECORDS " + collarId + " ");
        sendMessage("GET_SAFEZONES " + collarId + " \r\n");
    }

    @Override
    public void onSendCollarId(int id) {
        collarId = id;
        displayMap();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected");

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            displayMap();
        } else {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            String[] reasons = {"Location is necessary to view pet's current location on Google " +
                    "Maps"};
            int[] codes = {DOG_LOCATION_CODE};
            getPermission(permissions, reasons, codes);
        }
    }

    /**
     * sends message to server for storage
     *
     * @param message command to be sent to server
     */
    private void sendMessage(String message) {
        RetrieveFromServerAsyncTask rsat = new RetrieveFromServerAsyncTask(new MyResponseListener
                (getContext(), this));
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
        Log.d(TAG, "latlng: " + latLng);

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

    @Override
    public void displayOwnLocation() {

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission
                .ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation
                    .getLongitude());
            CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            map.animateCamera(camera);
        }


        Snackbar.make(getActivity().findViewById(R.id.coord_layout), "No previous " +
                "locations found for dog", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }).show();
    }

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

            // TODO: won't need null check once q problem is fixed
            if (coords == null) {
                return;
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.addCircle(new CircleOptions()
                            .center(coords)
                            .radius(Double.parseDouble(safezone[1]))
                            .fillColor(0x44ff0000)
                            .strokeColor(0xffff0000)
                            .strokeWidth(0));
                }
            });
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
