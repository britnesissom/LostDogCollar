package seniordesign.lostdogcollar;

import java.util.List;

/**
 * Created by britne on 3/13/16.
 */
public interface OnDisplayMapListener {

    void displayOwnLocation();
    void displayDogsLocation(String location);
    void displaySafezones(List<String> safezones);
}
