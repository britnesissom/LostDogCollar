package seniordesign.lostdogcollar;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by britne on 1/28/16.
 */
public class ResponseConverterUtil {

    private static final String TAG = "ConverterUtil";

    public ResponseConverterUtil() { }

    public static LatLng convertCoordsString(String coords) {
        String[] latLng = coords.split(",");
        latLng[0] = latLng[0].substring(1);
        latLng[1] = latLng[1].substring(0, latLng[1].length()-1);
        Log.i(TAG, "first latlng: " + latLng[0]);
        Log.i(TAG, "second latlng: " + latLng[1]);
        return new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));
    }

    public static LatLng convertRecordsString(String record) {
        // get first part of record - coords
        // TODO: create Record class with coords, battery, sleep time, date
        String[] latLng = record.split(",");
        latLng[0] = latLng[0].substring(1);
        latLng[1] = latLng[1].substring(0, latLng[1].length()-1);
        Log.i(TAG, "first latlng: " + latLng[0]);
        Log.i(TAG, "second latlng: " + latLng[1]);
        return new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));
    }

    // TODO: try with \n
    public static List<String> convertResponseToList(String record) {
        String[] recordArray = record.split("\t");

        for (int i = 0; i < recordArray.length; i++) {
            Log.i(TAG, "" + recordArray[i]);
        }

        List<String> list = new ArrayList<>();
        Collections.addAll(list, recordArray);

        return list;
        //Log.i(TAG, "safezones size: " + safezones.size());
    }
}
