//package Networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.ArrayList;

import config.IPConfig;
import config.SocketInfo;

public class p2pNode {
    DatagramSocket selfDatagramSocket = null;
    SecureRandom random = new SecureRandom();
    ArrayList<NodeInfo> connectedNodes = new ArrayList<NodeInfo>();

    SocketInfo selfSocketInfo;

    public p2pNode(int myPort) throws Exception {
        selfSocketInfo = new SocketInfo(getSelfIP(), myPort);
        selfDatagramSocket = new DatagramSocket(myPort);

        loadExternalNodes();
    }

    private void loadExternalNodes() throws IOException {
        // load external nodes from file
        for (int i = 0; i < IPConfig.num_sockets(); i++) {
            // if the ip and port is itself, skip
            if (selfSocketInfo.isEqual(IPConfig.getNodeSocket(i))) {
                System.out.println("Skipping self: " + i);
                continue;
            }
            SocketInfo socket = IPConfig.getNodeSocket(i);
            connectedNodes.add(new NodeInfo(socket.getIp(), socket.getPort()));
        }
    }

    public void createAndListenSocket() {
        try {
            byte[] incomingData = new byte[1024];

            while (true) {
                // ProtocolPacket packet = new ProtocolPacket(new DatagramPacket(incomingData,
                // 0))
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                selfDatagramSocket.receive(incomingPacket);
                String message = new String(incomingPacket.getData());
                InetAddress IPAddress = incomingPacket.getAddress();
                int port = incomingPacket.getPort();

                System.out.println("Received message from client: " + message);
                System.out.println("Recieved IP:" + IPAddress.getHostAddress());
                System.out.println("Recieved port:" + port);

                String reply = "Thank you for the message";
                byte[] data = reply.getBytes();

                DatagramPacket replyPacket = new DatagramPacket(data, data.length, IPAddress, port);

                selfDatagramSocket.send(replyPacket);
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

    public ArrayList<String> returnFileDirectory() {
        return null;
    }

    public void sendMessage(String message, String ipAddress, int port) {
        try {
            InetAddress IPAddress = InetAddress.getByName(ipAddress);
            byte[] data = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, port);
            selfDatagramSocket.send(sendPacket);
            System.out.println("Message sent from client");
            // random time between 0 and 30000 ms (0 to 30 sec)
            // send heartbeat
            int wait = random.nextInt(0, 30001);
            System.out.println("Waiting for " + wait + " ms");
            Thread.sleep(wait);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateNode(int index){
        NodeInfo info = connectedNodes.get(index);
        // 
    }

    public void sendNodeInfo(NodeInfo info){
        String iP = info.socketInfo.getIp();
        int port = info.socketInfo.getPort();
    }

    public static String getSelfIP() throws Exception {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String ip = socket.getLocalAddress().getHostAddress();
            System.out.println(ip);
            return ip;
        } catch (Exception e) {
            throw e;
        }
    }

    public void heartbeat() {

    }

    public void openSendingThread(){
        try {
            while(true){
                for(NodeInfo node : connectedNodes){
                    
                }
            }
        }
        catch (Exception e){
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {
        int myPort = 9876;
<<<<<<< Updated upstream
=======
        // int myPort = 9877;

        if (args.length > 0) {
            myPort = Integer.parseInt(args[0]);

        }
>>>>>>> Stashed changes
        p2pNode server;
        try {
            server = new p2pNode(myPort);
        } catch (Exception e) {
            throw e;
        }

        Thread readThread = new Thread() {
            public void run() {
                server.createAndListenSocket();
            }
        };
        Thread sendThread = new Thread() {
            public void run() {
                // while (true) {
                //     server.sendMessage("Chris", "10.141.39.117", 9876);
                //     server.sendMessage("Chris", "10.115.110.178", 9876);
                // }

                // Server.openSendingThread;
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
