""" This is a simple python server that listens to TCP connections and
    forwards the data to a DMX interface (Enttec OpenDMX USB).
    It is designed to be as simple as possible and requires no 3rd party
    libraries.
"""
import time
import serial
import socket
import argparse

class DMXUniverse:
    def __init__(self, device : str, tcp_port : int):
        """
        Args:
            device: The device (TTY or COM port) that represents the Enttec OpenDMX
            tcp_port: The TCP port on which to listen for data
        """
        # Initialize the serial port
        self.port = None
        self.port = serial.Serial(port=device,
                                  baudrate=250000,
                                  parity='N',
                                  stopbits=2,
                                  bytesize=8,
                                  timeout=1)
        # DMX is fundamentally RS485. If we want to be able to transmit, we need to
        # make sure that the RTS line is low (this causes the driver to hold onto the line).
        self.port.setRTS(False)

        # Open the TCP socket and bind it to the specified port
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.bind(('', tcp_port))

    def __del__(self):
        if self.port:
            self.port.close()

    def run(self):
        """ Wait for packets and forward them to the DMX interface """
        print('Listening for connections on port', self.socket.getsockname()[1])
        self.socket.listen(1)
        while True:
            conn, addr = self.socket.accept()
            print('Connected by', addr)
            while True:
                try:
                    data = conn.recv(512)
                except:
                    pass
                if not data:
                    break
                # Send an ack back to the client
                try :
                    conn.sendall("A".encode('utf-8'))
                except:
                    pass
                # And now we can send the data to the DMX interface
                self.port.break_condition = True
                time.sleep(0.0001)
                self.port.break_condition = False
                self.port.write(bytearray([0]) + data)
                self.port.flush()
                # We need to wait until all the data has gone out on the wire.
                # Most OSes only manage their own buffers, they don't how many bytes
                # are left in the hardware buffers. The flush() call above only
                # guaranteed that the data has been sent to the UART, not that it
                # has been sent to the wire.
                # We need to sleep by the length of the packet to make sure that it went
                # out. This is a bit of a hack.
                time.sleep(600 * 10 / 250000)
            conn.close()



def run():
    """ Run the DMX universe """
    parser = argparse.ArgumentParser(
                    prog='DMX server',
                    description='This is a simple python server that listens to TCP connections and'
                                'forwards the data to a DMX interface (Enttec OpenDMX USB).')
    parser.add_argument('-d', '--device', type=str, default='/dev/ttyUSB0')
    parser.add_argument('-p', '--port', type=int, default=5419)
    args = parser.parse_args()

    universe = DMXUniverse(args.device, args.port)
    universe.run()

if __name__ == '__main__':
    run()
