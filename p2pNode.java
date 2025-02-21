//package Networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.SecureRandom;

public class p2pNode {
    DatagramSocket mySocket = null;
    SecureRandom random = new SecureRandom();
    int externalPort;

    public p2pNode(int myPort, int externalPort) throws SocketException {
        mySocket = new DatagramSocket(myPort);
        this.externalPort = externalPort;
    }

    public void createAndListenSocket() {
        try {
            byte[] incomingData = new byte[1024];

            while (true) {
                DatagramPacket incomingPacket = new DatagramPacket(incomingData,
                        incomingData.length);
                mySocket.receive(incomingPacket);
                String message = new String(incomingPacket.getData());
                InetAddress IPAddress = incomingPacket.getAddress();
                int port = incomingPacket.getPort();

                System.out.println("Received message from client: " + message);
                System.out.println("Recieved IP:" + IPAddress.getHostAddress());
                System.out.println("Recieved port:" + port);

                String reply = "Thank you for the message";
                byte[] data = reply.getBytes();

                DatagramPacket replyPacket = new DatagramPacket(data, data.length, IPAddress, port);

                mySocket.send(replyPacket);
                Thread.sleep(2000);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message, String ipAddress, int port) {
        try {
            InetAddress IPAddress = InetAddress.getByName(ipAddress);
            byte[] data = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, port);
            mySocket.send(sendPacket);
            System.out.println("Message sent from client");
            // random time between 0 and 30000 ms (0 to 30 sec)
            // int wait = random.nextInt(0, 30001);
            int wait = 2000;
            System.out.println("Waiting for " + wait + " ms");
            Thread.sleep(wait);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws SocketException {
        int myPort = 9877;
        int externalPort = 9876;
        p2pNode server;
        try {
            server = new p2pNode(myPort, externalPort);
        } catch (SocketException e) {
            throw e;
        }

        Thread readThread = new Thread() {
            public void run() {
                server.createAndListenSocket();
            }
        };
        Thread sendThread = new Thread() {
            public void run() {
                while (true) {
                    server.sendMessage("Hello", "localhost", externalPort);
                }
            }
        };

        // start the threads
        readThread.start();
        sendThread.start();

        // wait for the threads to finish
        try {
            readThread.join();
            sendThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
