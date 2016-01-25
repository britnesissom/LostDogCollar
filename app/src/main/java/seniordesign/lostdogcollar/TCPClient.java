package seniordesign.lostdogcollar;


import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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

    private static final String SERVER_IP = BuildConfig.SERVER_IP; //your computer IP address
    private static final String PRIVATE_SERVER_IP = BuildConfig.PRIVATE_SERVER_IP; //your computer IP address
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

    public TCPClient() {
    }

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnResponseReceivedListener listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        //Log.d(TAG, "Sending message maybe...");
        if (mBufferOut != null && !mBufferOut.checkError()) {
            Log.i(TAG, "message should be sending now");
            mBufferOut.print(message);
            mBufferOut.flush();
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {
        Log.i("Debug", "stopClient");

        // send mesage that we are closing the connection
        //sendMessage(Constants.CLOSED_CONNECTION + "Kazy");

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() {

        mRun = true;
        Socket socket = null;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.i(TAG, "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVER_PORT);


            Log.i("Debug", "inside try catch");
            //sends the message to the server
            mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            //receives the message which the server sends back
            mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // send login name
            //sendMessage(Constants.LOGIN_NAME + PreferencesManager.getInstance().getUserName());
            //sendMessage("Hi");
            //in this while the client listens for the messages sent by the server
            Log.i(TAG, "about to run + send message to server");
            while (mRun) {

                mServerMessage = mBufferIn.readLine();
                //Log.d("myap", "running... after");

                if(mServerMessage != null){
                    Log.i(TAG, "Received: " + mServerMessage);
                }
                if (mServerMessage != null && mMessageListener != null) {
                    Log.i(TAG,"receiving server response...");
                    //call the method onResponseReceived from MyActivity class
                    mMessageListener.onResponseReceived(mServerMessage);
                }

            }
            Log.i("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

        } catch (IOException e) {

            Log.e(TAG, "S: Error", e);

        } finally {
            //the socket must be closed. It is not possible to reconnect to this socket
            // after it is closed, which means a new socket instance has to be created.
            try {
                if (socket != null) {
                    socket.close();
                    Log.i(TAG, "Socket closed");
                }
            }
            catch (IOException e) {
                Log.e(TAG, "error closing socket: " + e.getMessage());
            }
        }

    }

    //Declare the interface. The method onResponseReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnResponseReceivedListener {
        void onResponseReceived(String message);
    }
}