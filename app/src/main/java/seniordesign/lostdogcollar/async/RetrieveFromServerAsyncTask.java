package seniordesign.lostdogcollar.async;

import android.os.AsyncTask;

import seniordesign.lostdogcollar.OnSendResponseListener;
import seniordesign.lostdogcollar.TCPClient;

/**
 * Created by britne on 1/26/16.
 */
public class RetrieveFromServerAsyncTask extends AsyncTask<String,Void,Void> {

    private OnSendResponseListener listener = null;

    public RetrieveFromServerAsyncTask(OnSendResponseListener listener) {
        this.listener = listener;
    }

    public RetrieveFromServerAsyncTask() {}

    @Override
    protected Void doInBackground(String... message) {

        TCPClient tcpClient = TCPClient.getInstance();
        tcpClient.setListener(new TCPClient.OnResponseReceivedListener() {
            @Override
            public void onResponseReceived(String response) {
                //Log.d("RFSAT", "response: " + response);
                if (listener != null) {
                    listener.onSendResponse(response);
                }
            }
        });
        tcpClient.sendMessage(message[0]);

        return null;
    }

}
