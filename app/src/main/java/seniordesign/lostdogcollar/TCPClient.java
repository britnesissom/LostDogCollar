package seniordesign.lostdogcollar;


import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Description
 *
 * @author Catalin Prata
 *         Date: 2/12/13
 */
public class TCPClient {

    private static final String TAG = "TCPClient";

    private static final String SERVER_IP = "10.146.235.158"; //your computer IP address
    private static final int SERVER_PORT = 12000;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnResponseReceivedListener mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    private Socket socket;

    // tracks # of times app tries to reconnect to server
    private int retry = 1;

    private static TCPClient tcpClient = null;

    private TCPClient() { }

    /**
     * TCPClient is singleton because we want one client to communicate with the server across
     * the app
     * @return instance of TCPClient
     */
    public static TCPClient getInstance() {
        if (tcpClient == null) {
            tcpClient = new TCPClient();
        }
        return tcpClient;
    }

    public void setListener(OnResponseReceivedListener listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {

        if (mBufferOut != null && !mBufferOut.checkError()) {
            Log.i(TAG, "message should be sending now: " + message);
            mBufferOut.print(message);
            mBufferOut.flush();
        }
    }

    public boolean isRunning() {
        return mRun;
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        // send message that we are closing the connection
        //sendMessage(Constants.CLOSED_CONNECTION + "Kazy");
        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        try {
            if (mBufferIn != null) {
                mBufferIn.close();
            }
        }
        catch (IOException e) {
            Log.d(TAG, "error closing input buffer: " + e.getMessage());
        }

        try {
            if (socket != null) {
                socket.close();
                Log.i(TAG, "Socket closed in stopClient");
            }
        }
        catch (IOException e) {
            Log.e(TAG, "error closing socket: " + e.getMessage());
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    /**
     * Opens socket to communicate with server and closes socket when communication is complete.
     * Sends response from server to listener
     */
    public void run() {

        mRun = false;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVER_PORT);


            Log.i(TAG, "run: connected");

            mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            //receives the message which the server sends back
            mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            mRun = true;    // socket attached so connected

            String line = mBufferIn.readLine();

            while (line != null) {
                mServerMessage = line;

                if (mMessageListener != null) {
                    Log.i(TAG, "server response: " + mServerMessage);
                    //call the method onResponseReceived from MyActivity class
                    mMessageListener.onResponseReceived(mServerMessage);
                }

                line = mBufferIn.readLine();
            }

        } catch (ConnectException e) {
            /*Looper.prepare();
            final Handler handler = new Handler();*/

            Log.d(TAG, "trying to reconnect");
            if (retry < 3) {
                retry++;
                Log.i(TAG, "" + retry);

                // try to reconnect after 3 seconds
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Log.d(TAG, "interruptedexception");
                }

                run();
            } else {
                mRun =  false;
            }

        } catch (IOException e) {
            Log.e(TAG, "S: Error", e);
        }
        finally {
            //the socket must be closed. It is not possible to reconnect to this socket
            // after it is closed, which means a new socket instance has to be created.
            try {
                if (socket != null) {
                    socket.close();
                    retry = 1;
                }
            }
            catch (IOException e) {
                Log.e(TAG, "error closing socket: " + e.getMessage());
            }
        }

        mRun = true;
    }

    //Declare the interface. The method onResponseReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnResponseReceivedListener {
        void onResponseReceived(String response);
    }
}