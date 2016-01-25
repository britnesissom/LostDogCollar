package seniordesign.lostdogcollar;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.maps.model.LatLng;

import java.util.List;


public class HomeFragment extends BaseFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int SAFE_ZONE_CODE = 100;
    private static final int DOG_LOCATION_CODE = 101;
    private static final int RESOLVE_ERROR_CODE = 1001;
    private static final String TAG = "MainFragment";

    //private boolean mResolvingError;

    private MapView mapView;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private Location location;
    private int radius;
    private List<Geofence> safeZoneList;
    private TCPClient tcpClient = null;

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
        radius = -1;

        ServerResponseAsyncTask srat = new ServerResponseAsyncTask();
        srat.execute();

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
                if (tcpClient != null) {
                    Log.d(TAG, "sending coord request message");
                    tcpClient.sendMessage("GET 1\r\n");
                }
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

    private void addSafeZone() {
        /*if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {*/

            //do some stuff
            Log.d(TAG, "safe zone click");
            FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction();
            transaction.addToBackStack(null);
            transaction.replace(R.id.content_frag, AddSafeZoneFragment.newInstance());
            transaction.commit();

        /*} else {
            //PermissionUtils permissionUtils = new PermissionUtils(getContext(), this);
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            String[] reasons = {"Location is necessary to view safe zones on Google Maps"};
            int[] codes = {SAFE_ZONE_CODE};
            getPermission(permissions, reasons, codes);
            //this.requestPermissions(permissions, SAFE_ZONE_CODE);
        }*/
    }

    public boolean getPermission(final String[] permissions, String[] reasons, int[] codes) {

        for (int i = 0; i < permissions.length; i++) {

            if (ContextCompat.checkSelfPermission(getContext(), permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("Utils", "activity: " + this + ", permission: " + permissions[i]);

                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        permissions[i])) {
                    onShowSnackbar(reasons[i], permissions);
                } else {
                    requestPermissions(new String[]{permissions[i]}, codes[i]);
                }
            } else {
                return true;
            }
        }
        return true;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            onCreateMap();
        } else {
            //PermissionUtils permissionUtils = new PermissionUtils(getContext(), this);
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
                transaction.replace(R.id.content_frag, SettingsFragment.newInstance())
                        .commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

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

    public void onCreateMap() {
        try {
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
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(latlng, 14);
        map.animateCamera(camera);
    }

    public void onShowSnackbar(String message, final String[] permissions) {
        Snackbar.make(getActivity().findViewById(R.id.coord_layout), message, Snackbar
                .LENGTH_INDEFINITE)
                .setAction("ACCEPT", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        ActivityCompat.requestPermissions(getActivity(), permissions,
                                SAFE_ZONE_CODE);
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull final String[] permissions, @NonNull int[]
                                                       grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case DOG_LOCATION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    // this isn't necessary for floating action button click so fix that
                    onCreateMap();

                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            permissions[0])) {
                        onShowSnackbar("Location is necessary to use Google Maps" +
                                " and set safe zones", permissions);
                    } else {
                        //Never ask again selected, or device policy prohibits the app from having that permission.
                        //So, disable that feature, or fall back to another situation...
                    }
                }
            }
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

    public class ServerResponseAsyncTask extends AsyncTask<String,String,Void> {

        private TCPClient.OnResponseReceivedListener listener;

        public ServerResponseAsyncTask() {
            //this.listener = listener;
        }

        @Override
        protected Void doInBackground(String... message) {

            //we create a TCPClient object and

            if (tcpClient == null) {
                tcpClient = new TCPClient(new TCPClient.OnResponseReceivedListener() {
                    @Override
                    //here the messageReceived method is implemented
                    public void onResponseReceived(String message) {
                        Log.d("myapp", "message: " + message);
                        //listener.onResponseReceived(message);
                        //this method calls the onProgressUpdate
                        publishProgress(message);
                    }
                });
                tcpClient.run();
            }
            else {
                Log.d(TAG, "tcpClient already running");
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            //dynamicAdapter.addItem(values[0]);
            Log.d(TAG, "coords: " + values[0]);

        }
    }
}