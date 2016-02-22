package seniordesign.lostdogcollar.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import seniordesign.lostdogcollar.TCPClient;

// TODO: bindService
public class ServerService extends Service {

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
                //Log.d("ServerService", "server run");

                //while (true) {
                    tcpClient.run();

                    if (!tcpClient.isRunning()) {
                        Log.d("serverservice", "server not running");
                        Toast.makeText(getApplicationContext(), "Unable to connect to server", Toast
                                .LENGTH_SHORT).show();

                        return;
                    }
                //}
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
