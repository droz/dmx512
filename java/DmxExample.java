/* This example shows how to send data to a DMX512 universe, through
 * a simple TCP server.
 */

import java.io.IOException;
import java.net.*;

public class DmxExample {
    /*
     * This is the actual useful fun code
     * 
     * Each DMX universe has 512 channels.
     * Each channel is a byte, and it can have different meanings, depending on the device (light, dimmer, gobo, etc...).
     * For example, a channel can be the brightness of a light, or the position of a moving head.
     * Usually a device will use a few channels, starting at a given offset.
     * If you have multiple lights, you just need to fill the corresponding channels in the data array and send
     * everyting at the same time.
     * The following example is for a light that uses 11 channels, starting at channel 1 (index 0 in the array).
     */

    public static void main(String[] args) {
        // Connect to the server.
        // The raspberry pi is called "dmxhub.local", and the DMX server is on port 5419
        //  (the port number is like a secondary address that is used to distinguish different servers on the same machine)
        if (!connect("dmxhub.local", 5419)) {
            return;
        }
        // Allocate the data array (always 512 bytes, that's why its called "DMX512")
        byte[] data = new byte[512];

        // Set the first 11 channels to some values
        data[0]  = (byte) 0;   // Pan
        data[1]  = (byte) 0;   // Tilt
        data[2]  = (byte) 0;   // Pan/Tilt speed (0 fastest -> 255 slowest)
        data[3]  = (byte) 255;  // Global dimmer (0 off -> 255 full brighness)
        data[4]  = (byte) 0;   // Strobe (0 off, 1 slow -> 255 fast)
        data[5]  = (byte) 0;   // Red
        data[6]  = (byte) 0;   // Green
        data[7]  = (byte) 0;   // Blue
        data[8]  = (byte) 0;   // White
        data[9]  = (byte) 0;   // Orange
        data[10] = (byte) 0;   // UV (black light)

        // Now we loop while sending data to the server, incrementing values everytime to make stuff move.
        while(true) { 
            if (!sendData(data)) {
                return;
            }
            data[0]++;
            data[1]++;
            data[5]++;
            data[7]--;
        }
    }










    /*
     * START of client code
     * ( No need to understand the details of this part,
     *   this is the code that interacts with the server )
     */

    // The socket we use to connect to the server
    public static Socket socket;

    // This should be called first to connect to the server.
    // Args:
    //  hostname: The hostname of the server to connect to.
    //  port: The port of the server to connect to.
    // Returns true if the connection was successful.
    public static Boolean connect(String hostname, int port) {
        try {
            socket = new Socket(hostname, port);
        } catch (ConnectException e) {
            System.out.println("Could not connect to server " + hostname + ":" + port + ". Is the server running?");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Connected to server " + hostname + ":" + port);
        return true;
    }
    // This is the method you should call to send data to the server.
    // Args:
    //  data: A byte array that should be 512 bytes long. It is exactly what
    //        gets sent to the DMX universe.
    // Returns true if the data was sent successfully.
    public static Boolean sendData(byte[] data) {
        if (socket == null) {
            System.out.println("Connection is not established yet. Call connect() first.");
            return false;
        }
        if (data.length != 512) {
            System.out.println("The data to send should be 512 bytes long.");
            return false;
        }
        try {
            socket.getOutputStream().write(data);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        // Wait for the acknowledgment from the server
        try {
            socket.getInputStream().read();
        } catch (SocketException e) {
            System.out.println("Connection to server lost. Is the server still running?");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*
     * END of client code
     */
}
