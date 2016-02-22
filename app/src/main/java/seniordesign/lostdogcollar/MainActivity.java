package seniordesign.lostdogcollar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.facebook.appevents.AppEventsLogger;

import seniordesign.lostdogcollar.fragments.HomeFragment;
import seniordesign.lostdogcollar.fragments.LoginFragment;
import seniordesign.lostdogcollar.services.ServerService;

public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, ServerService.class);
        startService(intent);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frag, LoginFragment.newInstance());
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Main", "on resume");

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Main", "on pause");

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Main", "on destroy");

        Intent intent = new Intent(this, ServerService.class);
        stopService(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if(item.getItemId() == android.R.id.home){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frag, HomeFragment.newInstance());
            transaction.commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
