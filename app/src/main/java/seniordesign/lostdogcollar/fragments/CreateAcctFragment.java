package seniordesign.lostdogcollar.fragments;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import seniordesign.lostdogcollar.listeners.OnSendResponseListener;
import seniordesign.lostdogcollar.R;
import seniordesign.lostdogcollar.async.RetrieveFromServerAsyncTask;
import seniordesign.lostdogcollar.services.RegistrationIntentService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateAcctFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateAcctFragment extends Fragment {

    private static final String TAG = "CreateAcctFrag";

    private EditText usernameEdit;

    public CreateAcctFragment() {
        // Required empty public constructor
    }

    public static CreateAcctFragment newInstance() {
        return new CreateAcctFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_acct, container, false);

        usernameEdit = (EditText) view.findViewById(R.id.username);
        final EditText password = (EditText) view.findViewById(R.id.password);
        final EditText confirm = (EditText) view.findViewById(R.id.confirm_password);

        Button signUpBtn = (Button) view.findViewById(R.id.signup);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check sign in credentials
                checkSignUp(usernameEdit.getText().toString(), password.getText().toString(),
                        confirm
                        .getText().toString());
            }
        });

        return view;
    }

    private void checkSignUp(final String username, final String password, final String
            confirmPwd) {

        String message = "USERNAME_AVAIL " + username + "\r\n";

        RetrieveFromServerAsyncTask rfsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                if (response.contains("unavail")) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Username taken", Toast.LENGTH_SHORT).show();
                            usernameEdit.setText("");
                        }
                    });
                } else {
                    checkPassword(username, password, confirmPwd);
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            rfsat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } else {
            rfsat.execute(message);
        }
    }

    private void checkPassword(String username, String password, String confirmPwd) {
        if (password.equals(confirmPwd)) {
            Intent intent = new Intent(getContext(), RegistrationIntentService.class);

            intent.putExtra("username", username);
            intent.putExtra("password", password);
            getActivity().startService(intent);

            FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.content_frag, HomeFragment.newInstance(username));
            transaction.commit();
        }
    }
}
