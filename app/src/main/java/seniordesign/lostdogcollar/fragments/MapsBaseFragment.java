package seniordesign.lostdogcollar.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import seniordesign.lostdogcollar.fragments.dialogs.GoogleClientErrorDialogFragment;
import seniordesign.lostdogcollar.R;

/**
 * Created by britne on 1/6/16.
 */
public abstract class MapsBaseFragment extends Fragment {

    public static final int DOG_LOCATION_CODE = 101;

    private boolean mResolvingError;

    protected void setupToolbar(Toolbar toolbar, String title) {
        toolbar.setTitle(title);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setSaveEnabled(false); //TODO: look at this later
    }

    /**
     * checks if user's location is enabled on device
     *
     * if not enabled, displays dialog to allow user to enable it
     */
    protected void checkLocationServicesEnabled() {
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setMessage("Location Services are not enabled. Please enable them to use this " +
                    "application.");
            dialog.setPositiveButton("ENABLE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getContext().startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // close app since we cannot use location
                    getActivity().finish();
                    System.exit(0);
                }
            });
            dialog.show();
        }
    }

    /* Creates a dialog for an error message */
    protected void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        GoogleClientErrorDialogFragment dialogFragment = new GoogleClientErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt("dialog_error", errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getChildFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    public boolean getmResolvingError() {
        return mResolvingError;
    }

    public void setmResolvingError(boolean mResolvingError) {
        this.mResolvingError = mResolvingError;
    }

    /**
     * checks for permissions granted and if not granted, asks for them
     *
     * @param permissions - permissions that app needs from user
     * @param reasons - strings telling user why permission is necessary
     * @param codes - request codes to send to onPermissionsRequestResult
     * @return true if permission already granted
     */
    protected boolean getPermission(final String[] permissions, String[] reasons, int[] codes) {

        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(getContext(), permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        permissions[i])) {
                    onShowSnackbar(reasons[i], permissions, codes[i]);
                } else {
                    requestPermissions(new String[]{permissions[i]}, codes[i]);
                }
            } else {
                return true;
            }
        }
        return true;
    }


    protected void onShowSnackbar(String message, final String[] permissions, final int code) {
        Snackbar.make(getActivity().findViewById(R.id.coord_layout), message, Snackbar
                .LENGTH_INDEFINITE)
                .setAction("ACCEPT", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        ActivityCompat.requestPermissions(getActivity(), permissions, code);
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull final String[] permissions, @NonNull int[]
                                                   grantResults) {
        switch (requestCode) {
            case DOG_LOCATION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    // this isn't necessary for floating action button click so fix that
                    displayMap();

                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            permissions[0])) {
                        onShowSnackbar("Location is necessary to use Google Maps" +
                                " and set safe zones", permissions, DOG_LOCATION_CODE);
                    } else {
                        //Never ask again selected, or device policy prohibits the app from having that permission.
                        //So, disable that feature, or fall back to another situation...
                    }
                }
            }
        }
    }

    abstract void displayMap();
}
