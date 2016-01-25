package seniordesign.lostdogcollar;

import android.os.AsyncTask;
import android.util.Log;

public class SendMessageAsyncTask extends AsyncTask<String,String,Void> {

    private TCPClient.OnResponseReceivedListener listener;

    public SendMessageAsyncTask(TCPClient.OnResponseReceivedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(String... message) {

        //we create a TCPClient object and
        Log.d("SMAT", "about to send message");
        TCPClient mTcpClient = new TCPClient();
        mTcpClient.run();
        mTcpClient.sendMessage(message[0]);

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        //dynamicAdapter.addItem(values[0]);
        Log.d("SRAT", values[0]);

    }
}