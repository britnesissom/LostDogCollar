package seniordesign.lostdogcollar.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.maps.model.Circle;

import java.util.ArrayList;
import java.util.List;

import seniordesign.lostdogcollar.DogNotifRVAdapter;
import seniordesign.lostdogcollar.R;
import seniordesign.lostdogcollar.ResponseConverterUtil;
import seniordesign.lostdogcollar.RetrieveFromServerAsyncTask;
import seniordesign.lostdogcollar.fragments.dialogs.SelectDogDialogFragment;
import seniordesign.lostdogcollar.listeners.OnSendCollarIdListener;
import seniordesign.lostdogcollar.listeners.OnSendResponseListener;
import seniordesign.lostdogcollar.models.Collar;


// TODO: change notification rate for each collar instead of in general?
public class SettingsPageFragment extends Fragment implements AdapterView.OnItemSelectedListener,
        SelectDogDialogFragment.OnSelectDogListener {

    private static final String TAG = "SettingsFragment";
    private static final int THIRTY_SECONDS = 30000;
    private static final int ONE_MINUTE = 60000;
    private static final int TWO_MINUTES = 120000;
    private static final int FIVE_MINUTES = 300000;
    private static final int TEN_MINUTES = 1600000;

    private static final String COLLAR = "collar";

    private CallbackManager callbackManager;
    private int collarId;
    private Spinner spinner;
    private boolean first = true;
    private int ms;
    private int selection;

    public SettingsPageFragment() {
        // Required empty public constructor
    }

    public static SettingsPageFragment newInstance(int collarId) {
        SettingsPageFragment fragment = new SettingsPageFragment();
        Bundle args = new Bundle();
        args.putInt(COLLAR, collarId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();

        if (getArguments() != null) {
            collarId = getArguments().getInt(COLLAR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_settings, container, false);
        setupToolbar((Toolbar) view.findViewById(R.id.toolbar), "Settings");

        setupSpinner(view);

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setPublishPermissions("publish_actions");
        loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                AccessToken token = loginResult.getAccessToken();
                SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string
                        .prefs_name), 0);
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

    @Override
    public void onSelectDog(int id) {
        collarId = id;
        String message = "NOTIFICATION_RATE " + collarId + " " + ms/1000 + " \r\n";
        RetrieveFromServerAsyncTask rfsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                // we don't care about the response
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            rfsat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } else {
            rfsat.execute(message);
        }

        SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string
                .prefs_name), 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("notifSelect", selection);
        editor.putInt("notifTime", ms); // send notifs every ms milliseconds
        editor.apply();
    }

    private void setupToolbar(Toolbar toolbar, String title) {
        toolbar.setTitle(title);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setSaveEnabled(false); //TODO: look at this later
    }

    private void setupSpinner(View view) {
        spinner = (Spinner) view.findViewById(R.id.send_notif_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.notif_time_choices, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string
                .prefs_name), 0);
        int notifSelect = prefs.getInt("notifSelect", 0);
        int notifTime = prefs.getInt("notifTime", 30000);
        Log.d(TAG, "notif select: " + notifSelect);
        Log.d(TAG, "notif time: " + notifTime);

        //if nothing selected, send notifications every 30 seconds
        spinner.setSelection(notifSelect);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // send notification interval to server
        if (first) {
            first = false;
            return;
        }

        switch (parent.getSelectedItemPosition()) {
            case 0:
                setNotifTime(THIRTY_SECONDS, 0);
                break;
            case 1:
                setNotifTime(ONE_MINUTE, 1);
                break;
            case 2:
                setNotifTime(TWO_MINUTES, 2);
                break;
            case 3:
                setNotifTime(FIVE_MINUTES, 3);
                break;
            case 4:
                setNotifTime(TEN_MINUTES, 4);
                break;
        }
    }

    private void setNotifTime(int ms, int selection) {
        this.ms = ms;
        this.selection = selection;

        DialogFragment sddf = SelectDogDialogFragment.newInstance();
        sddf.show(getChildFragmentManager(), null);
    }

    public void onNothingSelected(AdapterView<?> parent) {}
}
