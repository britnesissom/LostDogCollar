package seniordesign.lostdogcollar.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import seniordesign.lostdogcollar.BuildConfig;
import seniordesign.lostdogcollar.TCPClient;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegistrationIS";
    private static boolean REGID_SENT = false;

    public RegistrationIntentService() {
        super("RegistrationIntentService");
    }

    @Override
    public void onHandleIntent(Intent intent) {

        String user = intent.getStringExtra("username");
        String pwd = intent.getStringExtra("password");
        Log.i(TAG, "user: " + user);

        InstanceID instanceID = InstanceID.getInstance(this);

        try {
            String token = instanceID.getToken(BuildConfig.SENDER_ID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "token: " + token);

            String encrypted = encrypt(pwd);
            //String encrypted = pwd;

            String message = "REGISTER_USER " + user + " " + encrypted + " " + token + "\r\n";

            TCPClient tcpClient = TCPClient.getInstance();
            tcpClient.setListener(new TCPClient.OnResponseReceivedListener() {
                @Override
                public void onResponseReceived(String response) {
                    Log.d(TAG, "response: " + response);
                }
            });

            // if tcpClient hasn't started yet, sleep for one second then send registration id
            while (!tcpClient.isRunning()) { }

            try {
                Log.d("RIS", "tcpClient not running yet");
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                Log.e("RIS", e.getMessage());
            }


            tcpClient.sendMessage(message);
            //Log.i(TAG, "reg id sent");
        }
        catch (IOException e) {
            Log.e("RegIntentService", e.getMessage());
        }
    }

    private static String encrypt(String pass) {
        String toReturn = "";
        for (int i = 0; i < pass.length(); i++) {
            char c = pass.charAt(i);
            if (c >= 'a' && c <= 'm') {
                c += 13;
            }
            else if (c >= 'A' && c <= 'M') {
                c += 13;
            }
            else if  (c >= 'n' && c <= 'z') {
                c -= 13;
            }
            else if  (c >= 'N' && c <= 'Z') {
                c -= 13;
            }
            toReturn += c;
        }
        return toReturn;
    }
}
