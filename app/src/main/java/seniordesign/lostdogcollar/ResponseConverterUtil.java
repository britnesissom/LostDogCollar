package seniordesign.lostdogcollar;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seniordesign.lostdogcollar.Collar;

/**
 * Created by britne on 1/28/16.
 */
public class ResponseConverterUtil {

    private static final String TAG = "ConverterUtil";

    public ResponseConverterUtil() { }

    public static LatLng convertCoordsString(String coords) {


        // TODO: figure out how to remove q from this list
        if (coords.contains("q")) {
            Log.d(TAG, "is q");
            return null;
        }

        Log.i(TAG, "coords: " + coords);
        String[] latLng = coords.split(",");
        latLng[0] = latLng[0].substring(1);
        latLng[1] = latLng[1].substring(0, latLng[1].length()-1);
        //Log.i(TAG, "first latlng: " + latLng[0]);
        //Log.i(TAG, "second latlng: " + latLng[1]);
        return new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));
    }

    public static LatLng convertRecordsString(String record) {
        // TODO: create Record class with coords, battery, sleep time, date
        // TODO: figure out how to parse the rest of string
        // regex to get coords b/w parentheses and that's it
        String coords = "";
        Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(record);
        while(m.find()) {
            coords = m.group(1);
        }

        String[] latLng = coords.split(",");
        //Log.i(TAG, "first latlng: " + latLng[0]);
        //Log.i(TAG, "second latlng: " + latLng[1]);
        return new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));
    }

    public static List<String> convertResponseToList(String record) {
        String[] recordArray = record.split("\t");
        List<String> list = new ArrayList<>();
        Collections.addAll(list, recordArray);

        return list;
    }

    public static List<Collar> convertResponseToCollarList(String collars) {
        String[] collarArray = collars.split("\t");
        List<Collar> list = new ArrayList<>();

        for (int i = 1; i < collarArray.length; i+= 1) {
            // collarInfo[0] - collar id
            // collarInfo[1] - collar name
            String[] collarInfo = collarArray[i].split(" ");
            Collar collar = new Collar(Integer.parseInt(collarInfo[0]), collarInfo[1]);
            list.add(collar);
        }

        return list;
    }
}
