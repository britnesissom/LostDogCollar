package seniordesign.lostdogcollar.fragments;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import seniordesign.lostdogcollar.OnSendResponseListener;
import seniordesign.lostdogcollar.R;
import seniordesign.lostdogcollar.async.RetrieveFromServerAsyncTask;

public class LoginFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LoginFrag";

    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions gso;
    private static final int RC_SIGN_IN = 9001;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        /*gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .enableAutoManage(getActivity() *//* FragmentActivity *//*, this *//* OnConnectionFailedListener *//*)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();*/
        // [END build_client]


        // automatically sign user in
        /*OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);

        if (pendingResult.isDone()) {
            // There's immediate result available.
            checkIfPrevSignIn(pendingResult.get());
        } else {
            // There's no immediate result ready, displays some progress indicator and waits for the
            // async callback.
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    checkIfPrevSignIn(result);
                }
            });
        }*/


    }

    /*private void checkIfPrevSignIn(GoogleSignInResult result) {
        GoogleSignInAccount acct = result.getSignInAccount();

        if (acct != null) {
            sendRegToServer(acct);
        }
    }*/

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // [START customize_button]
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
        /*SignInButton signInButton = (SignInButton) view.findViewById(R.id.google_signin);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });*/

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

        final EditText username = (EditText) view.findViewById(R.id.username);
        final EditText password = (EditText) view.findViewById(R.id.password);

        Button signInBtn = (Button) view.findViewById(R.id.signin);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check sign in credentials
                checkSignIn(username.getText().toString(), password.getText().toString());
            }
        });

        return view;
    }

    private void checkSignIn(String username, String password) {
        String message = "LOGIN "+ username + " " + password + "\r\n";

        RetrieveFromServerAsyncTask rfsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                checkLoginResponse(response);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            rfsat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } else {
            rfsat.execute(message);
        }
    }

    private void checkLoginResponse(String response) {
        switch(response) {
            case "INVALID USERNAME":
                Toast.makeText(getContext(), "Invalid username", Toast.LENGTH_SHORT).show();
                break;
            case "INVALID PASSWORD":
                Toast.makeText(getContext(), "Invalid password", Toast.LENGTH_SHORT).show();
                break;
            case "SUCCESS":
                FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                transaction.replace(R.id.content_frag, HomeFragment.newInstance());
                transaction.commit();
                break;
            default:
                Log.e(TAG, "response error: " + response);

        }
    }

/*    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void sendRegToServer(GoogleSignInAccount acct) {
        Intent intent = new Intent(getContext(), RegistrationIntentService.class);

        // TODO: change to acct.getIdToken() for security
        intent.putExtra("user", acct.getEmail());
        //intent.putExtra("username", )
        getActivity().startService(intent);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.content_frag, HomeFragment.newInstance());
        transaction.commit();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("LoginFragment", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            sendRegToServer(acct);

        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
        }
    }*/
}
