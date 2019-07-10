/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package droidserver;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

/**
 *
 * @author Abtahi
 */
public class Client {

    String host;
    int port;
    String status = "";
    JLabel statusText;
    
    public Socket serverSocket;

    public ClientThread clientThread;
    public Thread thread;

    public Client(String address, int port, JLabel status) {
        this.host = address;
        this.port = port;
        this.statusText = status;
        clientThread = new ClientThread();
        thread = new Thread(clientThread);
    }

    class ClientThread implements Runnable {

        private Socket socket;
        private BufferedReader input;

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(host);
                socket = new Socket(serverAddr, port);
                serverSocket = socket;

                while (!Thread.currentThread().isInterrupted()) {

                    this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = input.readLine();

                    if (null == message || "Disconnect".contentEquals(message)) {
                        Thread.interrupted();
                        message = "Server Disconnected.";
                        //updateMessage(getTime() + " | Server : " + message);
                        statusText.setText(getTime() + " | Server : " + message);
                        break;
                    }

                    //updateMessage(getTime() + " | Server : " + message);
                    statusText.setText(getTime() + " | Server : " + message);
                    ExecuteCommand(message);
                }

            } catch (UnknownHostException e) {
            } catch (IOException e) {
            }

        }
        
        public void sendMessageToServer(String message, BufferedImage image)
        {
            new SendMessage(message, image).execute();
        }
        
        public class SendMessage extends SwingWorker<String, Void> {
            String message;
            BufferedImage image;
            
            public SendMessage(String message, BufferedImage image)
            {
                this.message = message;
                this.image = image;
            }
            
            @Override
            protected String doInBackground() throws Exception {
                
                if (message != null && !message.isEmpty()) {
                    sendMessage(message);
                    return "Message sent to Server";
                }
                
                if (image != null) {
                    sendMessage(image);
                    return "Image sent to Server";
                }
                
                return null;
            }

            private void sendMessage(String message) {
                try {
                    if (null != socket) {
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())),
                                true);
                        out.println(message);
                        //updateMessage("Sending: " + message);
                    }
                } catch (IOException e) {
                    //updateMessage("Sending failed :" + e.toString());
                }
            }
            
            private void sendMessage(BufferedImage image) {
                try {
                    if (null != socket) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        ImageIO.write(image, "PNG", byteArrayOutputStream);
                        byte[] imageByteArray = byteArrayOutputStream.toByteArray();
                        //byteArrayOutputStream.close();
                        OutputStream outputStream = socket.getOutputStream();
                        //outputStream.write(imageInByte, 0, imageInByte.length);
                        //outputStream.flush();
                        DataOutputStream dos = new DataOutputStream(outputStream);
                        dos.writeInt(imageByteArray.length);
                        dos.write(imageByteArray, 0, imageByteArray.length);

                        System.out.println("Image Sent");
                    }
                } catch (IOException e) {
                    //updateMessage("Sending failed :" + e.toString());                    
                }
            }
        }

        String getTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            return sdf.format(new Date());
        }

        private void ExecuteCommand(String command) {
            switch (command) {
                case "Shutdown": {
                    try {
                        SystemCommandHelper.Shutdown();
                    } catch (RuntimeException | IOException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
                case "ScreenShot": {
                    try {
                        SystemCommandHelper.TakeScreenShot();
                    } catch (AWTException | IOException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
                case "WebShot": {
                    try {
                        SystemCommandHelper.TakeWebShot();
                    } catch (IOException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            }
        }
    }
    }
