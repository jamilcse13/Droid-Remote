package droidserver;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class UI {
    
    static Client client = null;
    
    public static void main(String args[]) {

        final JFrame frame;
        Container content;
        JLabel ipLabel, portLabel;
        final JLabel output = new JLabel("");
        //final Server server = new Server(output);
        Client tmpClient = null;
        final JList<String> iplist;
        final JScrollPane ipscrollpane;
        JButton refreshlist, connectButton, stop;
        final JTextField ipInput;
        final JTextField portInput;
        frame = new JFrame("DroidRemote Client");
        frame.setSize(300, 150);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        content = frame.getContentPane();
        content.setLayout(new FlowLayout());
        content.setBackground(Color.WHITE);
        //ipLabel = new JLabel("List of Ip Addresses: ");
        String[] ipdata = listipaddress();
        iplist = new JList<String>(ipdata);
        iplist.setVisibleRowCount(5);
        iplist.setSelectedIndex(0);
        ipscrollpane = new JScrollPane(iplist);
        ipscrollpane.setPreferredSize(new Dimension(250, 100));
        refreshlist = new JButton("Refresh List");
        refreshlist.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                String[] ipnewdata = listipaddress();
                iplist.setListData(ipnewdata);
                ipscrollpane.revalidate();
                ipscrollpane.repaint();

            }
        });
        refreshlist.setPreferredSize(new Dimension(250, 30));
        ipLabel = new JLabel("IP Address: ");
        ipInput = new JTextField(15);
        ipInput.setText("192.168.1.2");
        portLabel = new JLabel("Port Number: ");
        portInput = new JTextField(10);
        portInput.setText("4007");
        connectButton = new JButton("Connect");
        stop = new JButton("Stop");
        connectButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                String ipAddress;
                int port;
                try {
                    ipAddress = ipInput.getText();
                    port = Integer.parseInt(portInput.getText());
                    //server.run(port);
                    client = new Client(ipAddress, port, output);
                    //tmpClient = client;
                    //client.execute();
                    client.thread.start();
                } catch (NumberFormatException e1) {
                    // TODO Auto-generated catch block
                    output.setText("Use Only Integers for Port Number");
                }
            }
        });
        // unnecessary
        stop.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                //server.stopserver();
                if (client != null) {

                    client.clientThread.sendMessageToServer("Message from Client", null);
                }
            }
        });

        content.add(ipLabel);
//		content.add(ipscrollpane);
//		content.add(refreshlist);
        content.add(ipInput);
        content.add(portLabel);
        content.add(portInput);
        content.add(connectButton);
        //content.add(stop);
        content.add(output);
        frame.setVisible(true);
//		while (true) {
//			server.getinput();
//		}
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // unnecessary
    static String[] listipaddress() {
        List<String> ip_list = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> e = NetworkInterface
                    .getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) e.nextElement();
                Enumeration<InetAddress> e2 = ni.getInetAddresses();
                while (e2.hasMoreElements()) {
                    InetAddress ip = (InetAddress) e2.nextElement();
                    if (ip.toString().contains(".")) {
                        ip_list.add(ip.toString());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        String[] iplist = new String[ip_list.size()];
        iplist = ip_list.toArray(iplist);
        return iplist;
    }
}
