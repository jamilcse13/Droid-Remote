package com.abtahi.droidremote.server;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class ServerActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = ServerActivity.class.getSimpleName();
    ServerSocket server;
    Socket client;
    PrintWriter out;
    BufferedReader in;
    String line;
    NetworkTask networktask;
    static final int port = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        //startService(new Intent(this, MyService.class));
        //client = new Socket();
        //sendDataToNetwork("Hello Server...");
    }

    void listenToSocket() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocket listener;
                try {
                    listener = new ServerSocket(port);
                    Log.d(TAG, String.format("listening on port = %d", port));
                    while (true) {
                        Log.d(TAG, "waiting for client");
                        Socket socket = listener.accept();
                        Log.d(TAG, String.format("client connected from: %s", socket.getRemoteSocketAddress().toString()));
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        Log.i(TAG, "1");
                        PrintStream out = new PrintStream(socket.getOutputStream());
                        Log.i(TAG, "2");
                        for (String inputLine; (inputLine = in.readLine()) != null; ) {
                            Log.d(TAG, "received");
                            Log.d(TAG, inputLine);
                            StringBuilder outputStringBuilder = new StringBuilder("");
                            char inputLineChars[] = inputLine.toCharArray();
                            for (char c : inputLineChars)
                                outputStringBuilder.append(Character.toChars(c + 1));
                            out.println(outputStringBuilder);
                            Log.i(TAG, "RECEIVED MESSAGE FROM Client " + outputStringBuilder);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.startServerButton) {
            Log.d(TAG, "Starting Server...");
            //startService(new Intent(this, MyService.class));
            listenToSocket();
            return;
        }
//
//        if (view.getId() == R.id.connect_server) {
//            if (null == networktask) {
//                Log.d(TAG, "Connect to Server...");
//                networktask = new NetworkTask();
//                networktask.execute();
//            }
//            return;
//        }

        if (view.getId() == R.id.shutdownButton) {
            if (null != networktask && networktask.getStatus() == AsyncTask.Status.RUNNING) {
                Log.d(TAG, "Sending data to Server...");
                networktask.SendDataToNetwork("Hello Server...");
            }
        }
    }

    public static class MyService extends IntentService {
        public MyService() {
            super("MyService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Log.d(TAG, "onHandleIntent");
            ServerSocket listener;
            try {
                listener = new ServerSocket(port);
                Log.d(TAG, String.format("listening on port = %d", port));
                while (true) {
                    Log.d(TAG, "waiting for client");
                    Socket socket = listener.accept();
                    Log.d(TAG, String.format("client connected from: %s", socket.getRemoteSocketAddress().toString()));
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintStream out = new PrintStream(socket.getOutputStream());
                    StringBuilder outputStringBuilder = null;
                    for (String inputLine; (inputLine = in.readLine()) != null; ) {
                        Log.d(TAG, "received");
                        Log.d(TAG, inputLine);
                        outputStringBuilder = new StringBuilder("");
                        char inputLineChars[] = inputLine.toCharArray();
                        for (char c : inputLineChars)
                            outputStringBuilder.append(Character.toChars(c + 1));
                        out.println(outputStringBuilder);
                    }
                    Log.i(TAG, "RECEIVED MESSAGE FROM SERVER " + outputStringBuilder);
                    Toast.makeText(getBaseContext(), "RECEIVED MESSAGE FROM SERVER " + outputStringBuilder, Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void listenSocket() {
        try {
            server = new ServerSocket(4321);
        } catch (IOException e) {
            System.out.println("Could not listen on port 4321");
            System.exit(-1);
        }

        // listenSocketSocketserver.acceptSocket
        try {
            client = server.accept();
        } catch (IOException e) {
            System.out.println("Accept failed: 4321");
            System.exit(-1);
        }

        // listenSocketBufferedReaderclientPrintWriter
        try {
            in = new BufferedReader(new InputStreamReader(
                    client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(),
                    true);
        } catch (IOException e) {
            System.out.println("Read failed");
            System.exit(-1);
        }

        // listenSocket
        while (true) {
            try {
                line = in.readLine();
                //Send data back to client
                out.println(line);
            } catch (IOException e) {
                System.out.println("Read failed");
                System.exit(-1);
            }
        }
    }

    public class NetworkTask extends AsyncTask<Void, byte[], Boolean> {
        Socket nsocket; //Network Socket
        InputStream nis; //Network Input Stream
        OutputStream nos; //Network Output Stream

        @Override
        protected void onPreExecute() {
            Log.i("AsyncTask", "onPreExecute");
        }

        @Override
        protected Boolean doInBackground(Void... params) { //This runs on a different thread
            boolean result = false;
            try {
                Log.i("AsyncTask", "doInBackground: Creating socket");
                SocketAddress sockaddr = new InetSocketAddress("10.10.7.33", port);
                nsocket = new Socket();
                nsocket.connect(sockaddr, 10000); //10 second connection timeout
                if (nsocket.isConnected()) {
                    nis = nsocket.getInputStream();
                    nos = nsocket.getOutputStream();
                    Log.i("AsyncTask", "doInBackground: Socket created, streams assigned");
                    Log.i("AsyncTask", "doInBackground: Waiting for inital data...");
                    byte[] buffer = new byte[4096];
                    int read = nis.read(buffer, 0, 4096); //This is blocking
                    while (read != -1) {
                        byte[] tempdata = new byte[read];
                        System.arraycopy(buffer, 0, tempdata, 0, read);
                        publishProgress(tempdata);
                        Log.i("AsyncTask", "doInBackground: Got some data");
                        read = nis.read(buffer, 0, 4096); //This is blocking
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("AsyncTask", "doInBackground: IOException");
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("AsyncTask", "doInBackground: Exception");
                result = true;
            } finally {
                try {
                    nis.close();
                    nos.close();
                    nsocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("AsyncTask", "doInBackground: Finished");
            }
            return result;
        }

        public void SendDataToNetwork(final String cmd) { //You run this from the main thread.
            new Thread(new Runnable() {
                public void run() {
                    try {
                        if (null != nsocket && nsocket.isConnected()) {
                            Log.i("AsyncTask", "SendDataToNetwork: Writing received message to socket");
                            nos.write(cmd.getBytes());
                        } else {
                            Log.i("AsyncTask", "SendDataToNetwork: Cannot send message. Socket is closed");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        protected void onProgressUpdate(byte[]... values) {
            if (values.length > 0) {
                Log.i("AsyncTask", "onProgressUpdate: " + values[0].length + " bytes received.");
            }
        }

        @Override
        protected void onCancelled() {
            Log.i("AsyncTask", "Cancelled.");
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Log.i("AsyncTask", "onPostExecute: Completed with an Error.");
            } else {
                Log.i("AsyncTask", "onPostExecute: Completed.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != networktask) {
            networktask.cancel(true);
        }
    }

}
