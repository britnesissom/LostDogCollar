package seniordesign.lostdogcollar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.appevents.AppEventsLogger;

import java.util.List;

import seniordesign.lostdogcollar.fragments.DeleteCollarFragment;
import seniordesign.lostdogcollar.fragments.HomeFragment;
import seniordesign.lostdogcollar.fragments.LoginFragment;
import seniordesign.lostdogcollar.services.ServerService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, ServerService.class);
        startService(intent);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frag, LoginFragment.newInstance());
        transaction.commit();
    }

 /*   @Override
    public void onBackPressed() {
        tellFragments();
        super.onBackPressed();
    }

    private void tellFragments() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f : fragments) {
            if (f != null && f instanceof HomeFragment) {
                Log.d("MainActivity", "home frag");
                ((HomeFragment) f).onBackPressed();
            }
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d("Main", "on resume");

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.d("Main", "on pause");

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Log.d("Main", "on destroy");

        Intent intent = new Intent(this, ServerService.class);
        stopService(intent);
    }
}
