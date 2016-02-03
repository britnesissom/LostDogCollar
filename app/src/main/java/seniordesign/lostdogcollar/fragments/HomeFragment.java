package seniordesign.lostdogcollar.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import java.util.List;

import seniordesign.lostdogcollar.OnSendResponseListener;
import seniordesign.lostdogcollar.R;
import seniordesign.lostdogcollar.ResponseConverterUtil;
import seniordesign.lostdogcollar.async.RetrieveFromServerAsyncTask;
import seniordesign.lostdogcollar.async.SendMessageAsyncTask;
import seniordesign.lostdogcollar.fragments.dialogs.SafeZoneDialogFragment;


// TODO: implement collar LED, notification stopper, response converter for other types
// TODO: (maybe) implement removal of safezones
// TODO: update dog's location every 10 seconds
public class HomeFragment extends MapsBaseFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, SafeZoneDialogFragment.OnSendRadiusListener {

    private static final int RESOLVE_ERROR_CODE = 1001;
    private static final String TAG = "HomeFragment";

    private MapView mapView;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private List<Geofence> safeZoneList;
    private List<String> safezones;
    private LatLng dogLastLoc;

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

        Button szButton = (Button) view.findViewById(R.id.add_sz_button);
        szButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //createDialog();
                addSafeZone();
            }
        });

        Button stopNotifsButton = (Button) view.findViewById(R.id.stop_notifs_button);
        stopNotifsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: implement notification stopper method in server
                //sendMessage("STOP NOTIFICATIONS");
            }
        });

        // TODO: possibly change to button on toolbar
        SwitchCompat ledSwitch = (SwitchCompat) view.findViewById(R.id.led_switch);
        ledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // turn light on if switch is on
                    sendMessage("MESSAGE LIGHT_ON\r\n");
                } else {
                    sendMessage("MESSAGE LIGHT_OFF\r\n");
                }
            }
        });

        checkLocationServicesEnabled();

        return view;
    }

    private void checkLocationServicesEnabled() {
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setMessage("Location Services are not enabled. Please enable them to use this " +
                    "application.");
            dialog.setPositiveButton("ENABLE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getContext().startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // close app since we cannot use location
                    getActivity().finish();
                    System.exit(0);
                }
            });
            dialog.show();
        }
    }

    private void setOnMapClickListener() {
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                dogLastLoc = latLng;
                DialogFragment safeZoneDialog = SafeZoneDialogFragment.newInstance();
                safeZoneDialog.show(getChildFragmentManager(), "dialog");
            }
        });
    }

    private class MyResponseListener implements OnSendResponseListener {
        @Override
        public void onSendResponse(String response) {
            List<String> responses = ResponseConverterUtil.convertResponseToList(response);

            if (responses.get(0).contains("RECORDS")) {
                responses.remove(0);    // the RECORDS n is unnecessary
                displayLocation(responses.get(0));  // display dog's last known location
            }
            else if (responses.get(0).contains("SAFEZONES")) {
                responses.remove(0);
                safezones.addAll(responses);
                addCircles();
            }
        }
    }

    /**
     * Retrieves safezones from server which returns all safezones as string
     */
    private void retrieveSafezones() {
        String message = getResources().getString(R.string.get_safezones);
        RetrieveFromServerAsyncTask rsat = new RetrieveFromServerAsyncTask(new MyResponseListener
                ());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            rsat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } else {
            rsat.execute(message);
        }
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

            //sc.useDelimiter("\\D*"); // skip everything that is not a digit

            // converts safezone[0] into lat lng dogLastLoc
            // TODO: find better way to get dogLastLoc
            final LatLng coords = ResponseConverterUtil.convertCoordsString(safezone[0]);

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
     * sends just created safe zone to server
     *
     * @param radius - radius in meters of safe zone
     */
    public void sendToServer(int radius) {
        Log.d(TAG, "latlng: " + dogLastLoc.latitude + "," + dogLastLoc.longitude);
        String message = "SAFEZONE (" + dogLastLoc.latitude + "," + dogLastLoc.longitude + ") " +
                radius + "\r\n";

        map.addCircle(new CircleOptions()
                .center(dogLastLoc)
                .radius(radius)
                .fillColor(0x44ff0000)
                .strokeColor(0xffff0000)
                .strokeWidth(0));

        sendMessage(message);
    }

    private void sendMessage(String message) {
        SendMessageAsyncTask smat = new SendMessageAsyncTask();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            smat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } else {
            smat.execute(message);
        }
    }

    /**
     * Opens fragment to add safezone to map and store on server
     */
    private void addSafeZone() {
        Snackbar.make(getActivity().findViewById(R.id.coord_layout), "Tap area on map to add " +
                "safe zone", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { }
                }).show();
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
            case R.id.post_to_fb:
                postToFacebook();
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

            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("My Dog's Location")
                    .setContentDescription("This is where my dog was last seen")
                    .setContentUrl(Uri.parse("https://www.google.com/maps/?q=" + dogLastLoc.latitude
                            + "," + dogLastLoc.longitude))
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
            /*map.setMyLocationEnabled(true);
            location = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);*/
            RetrieveFromServerAsyncTask rsat = new RetrieveFromServerAsyncTask(new MyResponseListener());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                rsat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "GET 1 ");
            } else {
                rsat.execute("GET 1 ");
            }
        }
        catch (SecurityException e) {
            Log.e(TAG, "this error should not occur because permission has already " +
                    "been granted");
            Log.e(TAG, e.getMessage());
        }
    }

    private void displayLocation(String response) {
        List<String> records = ResponseConverterUtil.convertResponseToList(response);
        Log.d(TAG, "record 1: " + records.get(0));
        records.remove(0);  // remove the "RECORDS n" message
        Log.d(TAG, "record 1: " + records.get(0));

        for (String record : records) {
            LatLng latLng = ResponseConverterUtil.convertCoordsString(record);
            Log.d(TAG, "latlng: " + latLng);
            CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            map.animateCamera(camera);
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