package seniordesign.lostdogcollar.fragments.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.common.GoogleApiAvailability;

import seniordesign.lostdogcollar.fragments.MapsBaseFragment;

/**
 * Created by britne on 1/23/16.
 */
public class GoogleClientErrorDialogFragment extends DialogFragment {

    private static final int RESOLVE_ERROR_CODE = 1001;

    public GoogleClientErrorDialogFragment() { }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the error code and retrieve the appropriate dialog
        int errorCode = this.getArguments().getInt("dialog_error");
        return GoogleApiAvailability.getInstance().getErrorDialog(
                this.getActivity(), errorCode, RESOLVE_ERROR_CODE);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        ((MapsBaseFragment) getParentFragment()).onDialogDismissed();
    }
}
