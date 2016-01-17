package seniordesign.lostdogcollar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;


public class SettingsFragment extends BaseFragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "SettingsFragment";
    private static final int THIRTY_SECONDS = 0;
    private static final int ONE_MINUTE = 1;
    private static final int TWO_MINUTES = 2;
    private static final int FIVE_MINUTES = 3;
    private static final int TEN_MINUTES = 4;
    private static final int PERMISSION_CODE = 10;

    private CallbackManager callbackManager;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_settings, container, false);
        setupToolbar((Toolbar) view.findViewById(R.id.toolbar), "Settings");

        SwitchCompat wifiSwitch = (SwitchCompat) view.findViewById(R.id.wifi_switch);

        Spinner spinner = (Spinner) view.findViewById(R.id.send_notif_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.notif_time_choices, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //wifi is on, allow user to set up wifi
                if (isChecked) {
                    askForPermission();
                    Button setupWifi = (Button) view.findViewById(R.id.setup_wifi_button);
                    setupWifi.setVisibility(View.VISIBLE);
                    setupWifi.setOnClickListener(new MyOnClickListener());
                }
            }
        });

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        //loginButton.setReadPermissions("user_friends");
        loginButton.setPublishPermissions("publish_actions");
        // If using in a fragment
        loginButton.setFragment(this);
        // Other app specific specialization

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                AccessToken token = loginResult.getAccessToken();
                SharedPreferences prefs = getActivity().getSharedPreferences("LOST_COLLAR_PREFS",
                        0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("accessToken", token.getToken());
                editor.apply();
                Log.d(TAG, "logged in");
            }

            @Override
            public void onCancel() {
                // App code
                Log.d(TAG, "login cancelled");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d(TAG, exception.getMessage());
            }
        });

        return view;
    }

    private void setupWifi() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.content_frag, WifiSetupFragment.newInstance()).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

        // send notification interval to server
        switch (parent.getSelectedItemPosition()) {
            case THIRTY_SECONDS:
                break;
            case ONE_MINUTE:
                break;
            case TWO_MINUTES:
                break;
            case FIVE_MINUTES:
                break;
            case TEN_MINUTES:
                break;
            default:

        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        //if nothing selected, send notifications every 30 seconds
    }

    private void askForPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission
                    .ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Location is necessary to scan for nearby Wi-Fi networks");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission
                                .ACCESS_FINE_LOCATION}, PERMISSION_CODE);
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

            } else {

                // No explanation needed, we can request the permission.

                requestPermissions(new String[]{Manifest.permission
                                .ACCESS_FINE_LOCATION}, PERMISSION_CODE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "location permission granted");
                } else {
                    //permission denied, do something
                    Log.d(TAG, "location permission denied");
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("You cannot connect wirelessly to your dog collar without " +
                            "location permission.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            setupWifi();
        }
    }
}
