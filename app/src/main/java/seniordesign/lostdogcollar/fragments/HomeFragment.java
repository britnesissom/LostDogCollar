package seniordesign.lostdogcollar.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import seniordesign.lostdogcollar.OnSendResponseListener;
import seniordesign.lostdogcollar.R;
import seniordesign.lostdogcollar.async.RetrieveFromServerAsyncTask;


public class HomeFragment extends MapsBaseFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int RESOLVE_ERROR_CODE = 1001;
    private static final String TAG = "HomeFragment";

    //private boolean mResolvingError;

    private MapView mapView;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private Location location;
    private List<Geofence> safeZoneList;
    private List<String> safezones;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
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
        safezones = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setupToolbar((Toolbar) getActivity().findViewById(R.id.toolbar), "Lost Dog Collar");
        setHasOptionsMenu(true);

        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                addCircles();
            }
        });

        Button fbButton = (Button) view.findViewById(R.id.fb_post_button);
        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //createDialog();
                postToFacebook();
            }
        });

        Button foundButton = (Button) view.findViewById(R.id.found_button);
        foundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_map);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSafeZone();
            }
        });

        return view;
    }

    /**
     * Retrieves safezones from server which returns all safezones as string
     */
    private void retrieveSafezones() {
        String message = getResources().getString(R.string.get_safezones);
        RetrieveFromServerAsyncTask rsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                //Log.d(TAG, "safezones: " + response);
                convertResponseToList(response);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            rsat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } else {
            rsat.execute(message);
        }
    }


    /**
     * converts string of all safezones to List containing separated safezones
     */
    private void convertResponseToList(String safezoneString) {
        String[] safezoneArray = safezoneString.split("\t");

        Collections.addAll(safezones, safezoneArray);
        addCircles();
        //Log.i(TAG, "safezones size: " + safezones.size());
    }


    private void addCircles() {
        for (String sz : safezones) {
            // splits string by any # of consecutive spaces
            // safezone[0] = coords
            // safezone[1] = radius
            final                           String[] safezone = sz.split("\\s+");

            //sc.useDelimiter("\\D*"); // skip everything that is not a digit

            // converts safezone[0] into lat lng coords
            // TODO: find better way to get coords
            String[] latLng = safezone[0].split(",");
            latLng[0] = latLng[0].substring(1);
            latLng[1] = latLng[1].substring(0, latLng[1].length()-1);
            final LatLng coords = new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble
                    (latLng[1]));

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

    /**
     * Opens fragment to add safezone to map and store on server
     */
    private void addSafeZone() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                .beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.content_frag, AddSafeZoneFragment.newInstance());
        transaction.commit();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted();
            retrieveSafezones();
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
            case R.id.app_settings:
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.content_frag, SettingsPageFragment.newInstance())
                        .commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Open Facebook share dialog to allow user to post pet's last known location to Facebook
     */
    private void postToFacebook() {
        ShareDialog share = new ShareDialog(this);
        if (ShareDialog.canShow(ShareLinkContent.class)) {

            //TODO: setContentUrl will be filled with dog's latlong
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("My Dog's Location")
                    .setContentDescription("This is where my dog was last seen")
                    .setContentUrl(Uri.parse("https://www.google.com/maps/?q=32.920484,-97.305561"))
                    .build();

            share.show(linkContent);
        }
    }

    /**
     * initializes map after permission has been granted
     */
    public void locationPermissionGranted() {
        try {
            // TODO: change location to pet's last location, not yours
            map.setMyLocationEnabled(true);
            location = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        }
        catch (SecurityException e) {
            Log.e(TAG, "this error should not occur because permission has already " +
                    "been granted");
            Log.e(TAG, e.getMessage());
        }

        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(latlng, 15);
        map.animateCamera(camera);
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