package seniordesign.lostdogcollar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by britne on 1/17/16.
 */
public class PermissionUtils extends Activity {

    private static final int SAFE_ZONE_CODE = 100;
    private static final int DOG_LOCATION_CODE = 101;

    private Context context;
    private OnCreateMapListener mapListener;
    private OnShowSnackbarListener snackbarListener;

    public interface OnCreateMapListener {
        void onCreateMap();
    }

    public interface OnShowSnackbarListener {
        void onShowSnackbar(String message, String[] permissions);
    }

    public PermissionUtils(Context context, OnCreateMapListener mapListener, OnShowSnackbarListener
            snackbarListener) {
        this.context = context;
        this.mapListener = mapListener;
        this.snackbarListener = snackbarListener;
    }

    public boolean getPermission(final String[] permissions, String[] reasons, int[] codes) {

        for (int i = 0; i < permissions.length; i++) {

            if (ContextCompat.checkSelfPermission(context, permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("Utils", "activity: " + this);

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        permissions[i])) {
                    snackbarListener.onShowSnackbar(reasons[i], permissions);

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{permissions[i]}, codes[i]);
                }
            } else {
                return true;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[]
                                                   grantResults) {
        switch (requestCode) {
            case DOG_LOCATION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    // this isn't necessary for floating action button click so fix that
                    mapListener.onCreateMap();

                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest
                            .permission.ACCESS_FINE_LOCATION)) {

                        snackbarListener.onShowSnackbar("Location is necessary to use Google Maps" +
                                " and set safe zones", permissions);
                    } else {
                        //Never ask again selected, or device policy prohibits the app from having that permission.
                        //So, disable that feature, or fall back to another situation...
                    }
                }
            }

            case SAFE_ZONE_CODE:
                break;

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
