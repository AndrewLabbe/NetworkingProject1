//package Networking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
        // selfDatagramSocket.setReceiveBufferSize(786896 * 4);
        int bufferSize = selfDatagramSocket.getReceiveBufferSize();
        System.out.println("Send Buffer Size: " + bufferSize);
        loadExternalNodes();
    }

    private void loadExternalNodes() throws IOException {
        // load external nodes from file
        for (int i = 0; i < IPConfig.num_sockets(); i++) {
            // if the ip and port is itself, skip
            if (selfSocketInfo.equals(IPConfig.getNodeSocket(i))) {
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

                // System.out.println("Received message from client: " +
                // Arrays.toString(packet.getFileNames()));

                System.out.println("Received heartbeat from " + IPAddress.getHostAddress() + ":" + port);

                Thread.sleep(2000);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    private String[] getFileList() {
        return new String[] { "1", "2", "3" };
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
        if (args.length > 0) {
            myPort = Integer.parseInt(args[0]);
            System.out.println(myPort);
        }
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