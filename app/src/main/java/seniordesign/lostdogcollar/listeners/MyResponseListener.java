package seniordesign.lostdogcollar.listeners;


import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;

import seniordesign.lostdogcollar.ResponseConverterUtil;

/**
 * custom server response class that parses response from server and performs appropriate task
 */
public class MyResponseListener implements OnSendResponseListener {

    private Context context;
    private OnDisplayMapListener listener;

    public MyResponseListener(Context context, OnDisplayMapListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void onSendResponse(String response) {
        //Log.i(TAG, "my response listener: " + response);

        if (response.contains("MORE RECORDS")) {

            // display user's location if no previous locations found
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission
                    .ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                listener.displayOwnLocation();
            }

        }
        else if (response.contains("Need to Login")) {
            Log.d("MyResponse", "need to login");
            listener.onReLogin();
        }
        else {

            List<String> responses = ResponseConverterUtil.convertResponseToList(response);

            if (responses.get(0).contains("RECORDS")) {
                Log.d("MyResponse", "records time");
                // TODO: change this because collar id's are necessary now
                responses.remove(0);    // the RECORDS n is unnecessary

                if (responses.size() == 0) {
                    listener.displayOwnLocation();
                }
                else {
                    listener.displayDogsLocation(responses.get(0));
                }
            }
            else if (responses.get(0).contains("SAFEZONES")) {
                Log.d("MyResponse", "safezones time");
                responses.remove(0);
                listener.displaySafezones(responses);
            }
        }
    }
}
