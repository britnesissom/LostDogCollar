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
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import seniordesign.lostdogcollar.R;
import seniordesign.lostdogcollar.async.SendMessageAsyncTask;
import seniordesign.lostdogcollar.fragments.dialogs.SafeZoneDialogFragment;

public class AddSafeZoneFragment extends MapsBaseFragment implements GoogleApiClient
        .OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, SafeZoneDialogFragment.OnSendRadiusListener {

    private static final String TAG = "AddSFFrag";
    private static final int RESOLVE_ERROR_CODE = 1001;
    private MapView mapView;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private LatLng coords;

    public AddSafeZoneFragment() {
        // Required empty public constructor
    }

    public static AddSafeZoneFragment newInstance() {
        return new AddSafeZoneFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_safe_zone, container, false);
        setupToolbar((Toolbar) getActivity().findViewById(R.id.toolbar), "Safezone Setup");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                setOnMapClickListener();
            }
        });

        return view;
    }

    public void locationPermissionGranted() {
        try {
            map.setMyLocationEnabled(true);
            Location location = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(latlng, 15);
            map.animateCamera(camera);
        }
        catch (SecurityException e) {
            Log.e(TAG, "this error should not occur because permission has already " +
                    "been granted");
            Log.e(TAG, e.getMessage());
        }

        Snackbar.make(getActivity().findViewById(R.id.coord_layout), "Tap area on map to add " +
                "safe zone", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { }
                }).show();
    }

    private void setOnMapClickListener() {
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                coords = latLng;
                DialogFragment safeZoneDialog = SafeZoneDialogFragment.newInstance();
                safeZoneDialog.show(getChildFragmentManager(), "dialog");
            }
        });
    }

    public void sendToServer(int radius) {
        Log.d(TAG, "latlng: " + coords.latitude + "," + coords.longitude);
        String message = "SAFEZONE (" + coords.latitude + "," + coords.longitude + ") " +
                radius + "\r\n";

        map.addCircle(new CircleOptions()
                .center(coords)
                .radius(radius)
                .fillColor(0x44ff0000)
                .strokeColor(0xffff0000)
                .strokeWidth(0));

        // probably do this in asynctask
        SendMessageAsyncTask smat = new SendMessageAsyncTask();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            smat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } else {
            smat.execute(message);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted();
        } else {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            String[] reasons = {"Location access allows you to easily add safe zones near your " +
                    "current location."};
            int[] codes = {SAFE_ZONE_CODE};
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
                result.startResolutionForResult(getActivity(), RESOLVE_ERROR_CODE);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
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
