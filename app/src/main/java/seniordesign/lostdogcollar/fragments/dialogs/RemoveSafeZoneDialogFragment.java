package seniordesign.lostdogcollar.fragments.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by britne on 1/20/16.
 */
public class RemoveSafeZoneDialogFragment extends DialogFragment {

    private static final String TAG = "RemoveSafeZoneDialog";

    private static final String CENTER = "center";
    private static final String INDEX = "index";
    private static final String RADIUS = "radius";

    private OnRemoveSafeZoneListener listener;
    private LatLng center;
    private double radius;
    private int index;

    public interface OnRemoveSafeZoneListener {
        void removeSafezoneFromServer(LatLng center, double radius, int index);
    }

    public static RemoveSafeZoneDialogFragment newInstance(LatLng center, double radius, int
            index) {
        RemoveSafeZoneDialogFragment fragment = new RemoveSafeZoneDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(CENTER, center);
        args.putInt(INDEX, index);
        args.putDouble(RADIUS, radius);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            listener = (OnRemoveSafeZoneListener) getParentFragment();

            if (getArguments() != null) {
                center = getArguments().getParcelable(CENTER);
                index = getArguments().getInt(INDEX);
                radius = getArguments().getDouble(RADIUS);
            }
            //Log.d(TAG, "listener: " + listener);
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement " +
                    "OnRemoveSafeZoneListener");
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Remove Safe Zone");
        builder.setMessage("Are you sure you want to remove this safe zone?");
        builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.removeSafezoneFromServer(center, radius, index);
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
