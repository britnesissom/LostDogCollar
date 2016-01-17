package seniordesign.lostdogcollar;

import android.app.Application;

import com.facebook.FacebookSdk;

/**
 * Created by britne on 1/4/16.
 */
public class AppStart extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
    }
}
