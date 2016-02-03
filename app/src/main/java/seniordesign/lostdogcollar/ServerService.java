package seniordesign.lostdogcollar;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ServerService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d("ServerService", "handleMessage");

            // start TCPClient here
            TCPClient tcpClient = TCPClient.getInstance();

            // if tcp client is already running, there's no need to run it again
            if (!tcpClient.isRunning()) {
                tcpClient.run();
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    public ServerService() { }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        /*Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);*/

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                TCPClient tcpClient = TCPClient.getInstance();
                if (!tcpClient.run()) {
                    Toast.makeText(getApplicationContext(), "Unable to connect to server", Toast
                            .LENGTH_SHORT).show();

                    /*Snackbar.make(getActivity().findViewById(R.id.coord_layout),
                            message, Snackbar
                            .LENGTH_INDEFINITE)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                }
                            }).show();*/
                }

            }
        });
        thread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TCPClient tcpClient = TCPClient.getInstance();
        Log.d("service", "service OnDestroy");
        tcpClient.stopClient();
    }
}
