package seniordesign.lostdogcollar;

import android.app.Application;
import android.content.Intent;

import com.facebook.FacebookSdk;

import seniordesign.lostdogcollar.services.ServerService;

/**
 * Created by britne on 1/4/16.
 */
public class AppStart extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());

        Intent intent = new Intent(this, ServerService.class);
        startService(intent);
    }
}
