package seniordesign.lostdogcollar;

import android.os.AsyncTask;
import android.util.Log;

import seniordesign.lostdogcollar.listeners.OnSendResponseListener;
import seniordesign.lostdogcollar.TCPClient;

/**
 * Created by britne on 1/26/16.
 */
public class RetrieveFromServerAsyncTask extends AsyncTask<String,String,Void> {

    private OnSendResponseListener listener = null;

    public RetrieveFromServerAsyncTask(OnSendResponseListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(final String... message) {
        Log.d("RFSAT", "message: " + message[0]);

        //synchronized(this) {
            TCPClient tcpClient = TCPClient.getInstance();
            tcpClient.setListener(new TCPClient.OnResponseReceivedListener() {
                @Override
                public void onResponseReceived(String response) {
                    //Log.d("RFSAT", "message: " + message[0] + ", " + "response: " + response);
                    if (listener != null) {
                        listener.onSendResponse(response);
                    }
                }
            });
            tcpClient.sendMessage(message[0]);
       // }

        return null;
    }

}
