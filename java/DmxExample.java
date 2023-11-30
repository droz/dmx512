/* This example shows how to send data to a DMX512 universe, through
 * a simple TCP server.
 */

import java.io.IOException;
import java.net.*;

public class DmxExample {

    /*
     * START of client code
     * ( No need to understand the details of this )
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
            socket = new Socket("localhost", 5419);
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
    
    /*
     * Now is the actual useful fun code
     */

    public static void main(String[] args) {
        if (!connect("localhost", 5419)) {
            return;
        }
        byte[] data = new byte[512];
        data[0]  = (byte) 0;   // Pan
        data[1]  = (byte) 0;   // Tilt
        data[2]  = (byte) 0;   // Pan/Tilt speed (0 fastest -> 255 slowest)
        data[3]  = (byte) 32;  // Global dimmer (0 off -> 255 full brighness)
        data[4]  = (byte) 0;   // Strobe (0 off, 1 slow -> 255 fast)
        data[5]  = (byte) 0;   // Red
        data[6]  = (byte) 0;   // Green
        data[7]  = (byte) 0;   // Blue
        data[8]  = (byte) 0;   // White
        data[9]  = (byte) 0;   // Orange
        data[10] = (byte) 0;   // UV (black light)

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

}
