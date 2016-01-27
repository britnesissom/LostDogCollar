package seniordesign.lostdogcollar.fragments.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import seniordesign.lostdogcollar.R;

/**
 * Created by britne on 1/17/16.
 */
public class WifiPasswordDialogFragment extends DialogFragment {

    private static final String TAG = "WifiPassDialog";
    private static final String SCAN_RESULT = "scanResult";
    private static final String TYPE = "type";

    private OnConnectCollarListener listener;
    private EditText identityEditText;

    public interface OnConnectCollarListener {
        void connectCollarToWifi(String pass, ScanResult scanResult);
        void connectCollarToEapWifi(String identity, String pass, ScanResult scanResult);
    }

    public static WifiPasswordDialogFragment newInstance(ScanResult scanResult, String type) {
        Log.d(TAG, "newInstance");
        WifiPasswordDialogFragment fragment = new WifiPasswordDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(SCAN_RESULT, scanResult);
        args.putString(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            listener = (OnConnectCollarListener) getParentFragment();
            Log.d(TAG, "listener: " + listener);
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnConnectCollarListener");
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        final ScanResult result = args.getParcelable(SCAN_RESULT);
        final String type = args.getString(TYPE);

        /*if (result == null) {

        }*/

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.wifi_password_dialog, null);

        if (type.equals("EAP")) {
            identityEditText = (EditText) view.findViewById(R.id.identity_editText);
            identityEditText.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "view should be inflated: " + view.toString());

        final EditText passwordEditText = (EditText) view.findViewById(R.id.password_editText);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.show_pass_checkbox);

        // if it's checked, display the password
        // if it's not, display the password dots
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passwordEditText.setTransformationMethod(null);
                } else {
                    passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle(result.SSID);
        builder.setPositiveButton("CONNECT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (type.equals("EAP")) {
                    listener.connectCollarToEapWifi(identityEditText.getText().toString(),
                            passwordEditText.getText().toString(), result);
                } else {
                    listener.connectCollarToWifi(passwordEditText.getText().toString(), result);
                }
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }
}
