package seniordesign.lostdogcollar.async;

import android.os.AsyncTask;
import android.util.Log;

import seniordesign.lostdogcollar.listeners.OnSendResponseListener;
import seniordesign.lostdogcollar.TCPClient;

public class SendMessageAsyncTask extends AsyncTask<String,Void,Void> {

    private OnSendResponseListener listener = null;

    public SendMessageAsyncTask(OnSendResponseListener listener) {
        this.listener = listener;
    }

    public SendMessageAsyncTask() {}

    @Override
    protected Void doInBackground(String... message) {

        TCPClient tcpClient = TCPClient.getInstance();
        tcpClient.setListener(new TCPClient.OnResponseReceivedListener() {
            @Override
            public void onResponseReceived(String response) {
                Log.d("SMAT", "response: " + response);
                if (listener != null) {
                    listener.onSendResponse(response);
                }
            }
        });
        tcpClient.sendMessage(message[0]);

        return null;
    }
}