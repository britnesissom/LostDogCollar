package seniordesign.lostdogcollar.fragments.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import seniordesign.lostdogcollar.R;

/**
 * Created by britne on 1/20/16.
 */
public class SafeZoneDialogFragment extends DialogFragment {

    private static final String TAG = "SafeZoneDialog";

    private static final String LAT = "lat";
    private static final String LONG = "longi";

    private OnSendRadiusListener listener;

    public interface OnSendRadiusListener {
        void sendSafezoneToServer(int meters, double lat, double longi);
    }

    public static SafeZoneDialogFragment newInstance(double lat, double longi) {
        SafeZoneDialogFragment fragment = new SafeZoneDialogFragment();
        Bundle args = new Bundle();
        args.putDouble(LAT, lat);
        args.putDouble(LONG, longi);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            listener = (OnSendRadiusListener) getParentFragment();
            //Log.d(TAG, "listener: " + listener);
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement " +
                    "OnSendRadiusListener");
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.safe_zone_dialog, null);

        final EditText radius = (EditText) view.findViewById(R.id.sz_radius);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle("Specify safe zone radius");
        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.sendSafezoneToServer(Integer.parseInt(radius.getText().toString()),
                        getArguments().getDouble(LAT), getArguments().getDouble(LONG));
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
