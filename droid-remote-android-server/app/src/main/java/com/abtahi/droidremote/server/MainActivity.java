package com.abtahi.droidremote.server;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private ServerSocket serverSocket;
    private Socket tempClientSocket;
    Thread serverThread = null;
    Handler handler;
    public static final int SERVER_PORT = 4007;
    TextView messageTv;
    ImageView screenshotImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        handler = new Handler();
        messageTv = (TextView) findViewById(R.id.messageTextView);
        screenshotImage = (ImageView) findViewById(R.id.screenshotImage);
    }

    public void updateMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageTv.append(">> " + message + "\n");
            }
        });
    }

    @Override
    public void onClick(View view) {
        String message;

        switch (view.getId()) {
            case R.id.startServerButton:
                Log.d(TAG, "Starting Server...");
                messageTv.setText("");
                updateMessage("Starting Server....");
                this.serverThread = new Thread(new ServerThread());
                this.serverThread.start();
                break;
            case R.id.shutdownButton:
                new SendMessage().execute("Shutdown");
                break;
            case R.id.screenshotButton:
                message = "ScreenShot";
                new SendMessage().execute(message);
                updateMessage("Processing: " + message);
                break;
            case R.id.webshotButton:
                message = "WebShot";
                new SendMessage().execute(message);
                updateMessage("Processing: " + message);
                break;
            default:
                break;
        }
    }


    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress() + ":" + getPort();
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    public class SendMessage extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String message = params[0];
            sendMessage(message);
            return null;
        }

        private void sendMessage(String message) {
            try {
                if (null != tempClientSocket) {
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(tempClientSocket.getOutputStream())),
                            true);
                    out.println(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
                updateMessage("Sending failed :" + e.toString());
            }
        }
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                updateMessage("Server running at : "
                        + getIpAddress());
                findViewById(R.id.startServerButton).setActivated(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (null != serverSocket) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();
                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File filePath = new File(directory,"ScreenShot.png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 0, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filePath.getAbsolutePath();
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader bufferedReader;
        private DataInputStream dataInputStream;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;
            tempClientSocket = clientSocket;

            try {
                //this.bufferedReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                InputStream inputStream = clientSocket.getInputStream();
                this.dataInputStream = new DataInputStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            new SendMessage().execute("Connected to: " + getIpAddress());
            updateMessage("Client connected from: " + clientSocket.getInetAddress() + ":" + clientSocket.getLocalPort());
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    byte[] data;//String read = input.readLine();
                    int len = this.dataInputStream.readInt();
                    data = new byte[len];
                    if (len > 0) {
                        this.dataInputStream.readFully(data,0,data.length);
                    }
                    updateMessage("Image received");
                    //Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                    //String filePath = saveToInternalStorage(image);
                    //screenshotImage.setImageBitmap(Bitmap.createScaledBitmap(image, 150, 150, false));
                    //updateMessage("Image: " + image);
                    handler.post(new UpdateUIThread(data));

                } catch (IOException e) {
                    e.printStackTrace();
                    updateMessage(e.getMessage());
                }
            }
        }
    }

    class UpdateUIThread implements Runnable {
        private byte[] byteArray;//private String msg;

        public UpdateUIThread(byte[] array){    //public updateUIThread(String str) {
            this.byteArray=array;   //this.msg = str;
        }

        @Override
        public void run() {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray , 0, byteArray .length);
            screenshotImage.setImageBitmap(bitmap);//text.setText(text.getText().toString()+"Client Says: "+ msg + "\n");
        }
    }

    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != serverThread) {
            //sendMessage("Disconnect");
            serverThread.interrupt();
            serverThread = null;
        }
    }
}


