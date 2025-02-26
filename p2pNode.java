//package Networking;

import java.io.File;
import java.io.FileNotFoundException;
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
    ArrayList<NodeStatus> connectedNodes = new ArrayList<NodeStatus>();

    String homeDirectory;

    SocketInfo selfSocketInfo;
    int nodeId = 0;

    public p2pNode(int myPort) throws Exception {
        selfSocketInfo = new SocketInfo(getSelfIP(), myPort);
        selfDatagramSocket = new DatagramSocket(myPort);
        homeDirectory = "/Home/";
        int bufferSize = selfDatagramSocket.getSendBufferSize();
        System.out.println("Send Buffer Size: " + bufferSize);
        loadExternalNodes();
    }

    private void loadExternalNodes() throws IOException {
        // load external nodes from file
        for (int i = 0; i < IPConfig.num_sockets(); i++) {
            // if the ip and port is itself, skip
            if (selfSocketInfo.isEqual(IPConfig.getNodeSocket(i))) {
                System.out.println("Skipping self: " + i);
                nodeId = i;
                continue;
            }
            SocketInfo socket = IPConfig.getNodeSocket(i);
            connectedNodes.add(new NodeStatus(socket.getIp(), socket.getPort()));
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

                // String message = new String(incomingPacket.getData());
                ProtocolPacket packet = ProtocolPacket.deserializePacket(incomingPacket.getData());
                InetAddress IPAddress = incomingPacket.getAddress();
                int port = incomingPacket.getPort();
                if (packet == null) {
                    System.out.println("is null");
                    continue;
                }

                System.out.println("Received message from client: " + packet.getSenderId());

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

    public void sendHeartbeatTo(NodeStatus info) {
        String ip = info.socketInfo.getIp();
        int port = info.socketInfo.getPort();

        try {
            DatagramPacket packet = ProtocolPacket.generateDatagramPacket(this.nodeId, getFileList(), ip, port);
            // packet = new DatagramPacket("Hello".getBytes(), "Hello".getBytes().length,
            // InetAddress.getByName(ip), port);
            // System.out.println("size " + packet.getLength());
            System.out.println("Sending heartbeat to " + ip + ":" + port);
            selfDatagramSocket.send(packet);
            getFileList();

            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String[] getFileList() {
        String directory = System.getProperty("user.dir");
        File homeFolder = new File(directory + "/Home/");
        File[] files = homeFolder.listFiles();
        int numFiles = files.length;
        String[] fileList = new String[numFiles];
        for(int i = 0; i < numFiles; i++){
            fileList[i] = files[i].getName();
            System.out.println(fileList[i]);
        }
        return fileList;
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

    public void openSendingThread() {
        try {
            while (true) {
                for (NodeStatus node : connectedNodes) {
                    sendHeartbeatTo(node);
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {
        int myPort = 9876;
        // int myPort = 9877;
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
                server.openSendingThread();
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