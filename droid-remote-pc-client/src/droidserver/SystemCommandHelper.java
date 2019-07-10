/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package droidserver;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.imageio.ImageIO;
import com.github.sarxos.webcam.Webcam;

/**
 *
 * @author Abtahi
 */
public class SystemCommandHelper {

    public static void Shutdown() throws RuntimeException, IOException {
        String shutdownCommand = "";
        String operatingSystem = System.getProperty("os.name");
        System.out.println(operatingSystem);
        switch (operatingSystem) {
            case "Linux":
            case "Mac OS X":
                shutdownCommand = "shutdown -h now";
                break;
            case "Windows":
            case "Windows 10":
                shutdownCommand = "shutdown.exe -s -t 0";
                break;
            default:
                throw new RuntimeException("Unsupported OS Runtime");
        }
        System.out.println("Shutting down PC");
        Runtime.getRuntime().exec(shutdownCommand);
        System.exit(0);
    }

    public static void TakeScreenShot() throws AWTException, IOException {
        System.out.println("Taking ScreenShot");
        BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        ImageIO.write(image, "PNG", new File("d:\\" + getFormattedTime() + ".png"));

//        if (UI.client.serverSocket != null) {
//            System.out.println("Sending Screenshot");
//            ImageIO.write(image, "PNG", UI.client.serverSocket.getOutputStream());
//        }
        // sending screenshot to server
        UI.client.clientThread.sendMessageToServer(null, image);
    }

    public static void TakeWebShot() throws IOException {
        Webcam webCam = Webcam.getDefault();
        if (webCam != null) {
            webCam.open();
            System.out.println("Taking WebShot");
            BufferedImage image = webCam.getImage();
            ImageIO.write(image, "PNG", new File("d:\\" + getFormattedTime() + ".png"));
            webCam.close();
            System.out.println("ScreenShot Captured");

            // sending screenshot to server
            UI.client.clientThread.sendMessageToServer(null, image);
        } else {
            System.out.println("WebCam Not Found");
        }
    }

    private static String getFormattedTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd hh mm ss");
        Calendar calendar = Calendar.getInstance();
        String formattedTime = formatter.format(calendar.getTime());
        return formattedTime;
    }
}
