package seniordesign.lostdogcollar.models;

import android.util.Log;

/**
 * Created by britne on 3/13/16.
 */
public class Collar {

    private int id;
    private String name;
    private String battery;

    public Collar(int id, String name, String battery) {
        this.id = id;
        this.name = name;
        this.battery = battery;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Collar)) {
            return false;
        }
        Collar other = (Collar) o;
        Log.d("Collar", other.getName() + " " + other.getId());
        Log.d("Collar", this.getName() + " " + this.getId());
        return name.equals(other.name) && id == other.id;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
