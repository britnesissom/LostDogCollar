package seniordesign.lostdogcollar.models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by britne on 4/13/16.
 */
public class Record {

    private String battery;
    private LatLng coords;

    public Record(LatLng coords, String battery) {
        this.coords = coords;
        this.battery = battery;
    }

    public String getBattery() {
        return battery;
    }

    public LatLng getCoords() {
        return coords;
    }
}
