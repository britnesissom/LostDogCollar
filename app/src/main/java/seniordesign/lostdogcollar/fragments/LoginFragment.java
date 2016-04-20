package seniordesign.lostdogcollar.fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import seniordesign.lostdogcollar.listeners.OnSendResponseListener;
import seniordesign.lostdogcollar.R;
import seniordesign.lostdogcollar.RetrieveFromServerAsyncTask;


// TODO: add password security
public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFrag";

    private boolean rememberUser = false;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        TextView createAcct = (TextView) view.findViewById(R.id.create_acct_btn);
        createAcct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send to create account screen
                FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                transaction.replace(R.id.content_frag, CreateAcctFragment.newInstance());
                transaction.commit();
            }
        });

        CheckBox checkbox = (CheckBox) view.findViewById(R.id.remember_user_check);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rememberUser = isChecked;
            }
        });

        final EditText usernameEditText = (EditText) view.findViewById(R.id.username);
        final EditText passwordEditText = (EditText) view.findViewById(R.id.password);

        Button signInBtn = (Button) view.findViewById(R.id.signin);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check sign in credentials
                checkSignIn(usernameEditText.getText().toString(), passwordEditText.getText()
                        .toString());
            }
        });


        SharedPreferences prefs = getActivity().getSharedPreferences(getResources().getString(R
                .string.prefs_name), 0);

        String username = null;
        String password = null;

        if (rememberUser) {
            username = prefs.getString("username", null);
            password = prefs.getString("password", null);
        }

        // if user isn't remembered, send to login screen
        // else, send to home screen
        if (username != null && password != null) {
            usernameEditText.setText(username);
            passwordEditText.setText(password);
            checkbox.setChecked(true);
        }

        return view;
    }

    private void checkSignIn(final String username, final String password) {
        String message = "LOGIN "+ username + " " + password;

        RetrieveFromServerAsyncTask rfsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                checkLoginResponse(username, password, response);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            rfsat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } else {
            rfsat.execute(message);
        }
    }

    private void checkLoginResponse(String username, String password, String response) {
        switch(response) {
            case "INVALID USERNAME":
                runOnUi(response);
                break;
            case "INVALID PASSWORD":
                runOnUi(response);
                break;
            case "SUCCESS":
                SharedPreferences prefs = getActivity().getSharedPreferences(getResources()
                        .getString(R.string.prefs_name), 0);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putString("username", username);
                editor.putString("password", password);

                editor.apply();

                FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                transaction.replace(R.id.content_frag, HomeFragment.newInstance(username));
                transaction.commit();
                break;
            default:
                Log.e(TAG, "response error: " + response);

        }
    }

    private void runOnUi(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
