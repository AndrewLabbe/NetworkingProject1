import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;

import config.IPConfig;
import config.SocketInfo;

public class ClientNode {
    DatagramSocket selfDatagramSocket = null;
    SecureRandom secureRandom = new SecureRandom();
    ArrayList<NodeStatus> connectedNodes = new ArrayList<NodeStatus>();

    SocketInfo serverSocketInfo;
    SocketInfo selfSocketInfo;
    int nodeId = 0;

    /**
     * Constructor for p2pNode
     *
     * @param myPort : port number to listen on for incoming packets
     * @throws Exception
     */
    public ClientNode(int myPort) throws Exception {
        // self info
        selfSocketInfo = new SocketInfo(getSelfIP(), myPort);
        selfDatagramSocket = new DatagramSocket(myPort);

        // server socket info
        serverSocketInfo = IPConfig.getServerSocket();

        for (int i = 0; i < IPConfig.num_sockets(); i++) {
            SocketInfo socket = IPConfig.getClientSocket(i);
            if (selfSocketInfo.equals(socket)) {
                nodeId = i;
                break;
            }
        }
    }

    /**
     * Listen for incoming packets, parse, and update info on connected nodes
     */
    public void createAndListenSocket() {
        try {
            byte[] incomingData = new byte[1024];

            while (true) {
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                // accept packet
                selfDatagramSocket.receive(incomingPacket);

                // decode packet
                ProtocolPacket packet = ProtocolPacket.deserializePacket(incomingPacket.getData());

                // update node status
                // connectedNodes.get(packet.getSenderId()).updateStatus(packet.getFileNames(),
                // packet.getTimestamp());

                // System.out.println(packet.getType());
                if (packet.getType() == 1) // check that it is a server packet
                    connectedNodes = packet.getConnectedNodes();

                InetAddress IPAddress = incomingPacket.getAddress();
                int port = incomingPacket.getPort();
                // System.out.println("Received client info from " + IPAddress.getHostAddress() + ":" + port);

                Thread.sleep(1000);
            }
        } catch (Exception e) {
            throw new RuntimeException("Listening process was interupted", e);
        }
    }

    /**
     * Send a heartbeat to a specfic node, identified by the NodeStatus object
     *
     * @param info
     */
    public void sendHeartbeat() {
        String ip = serverSocketInfo.getIp();
        int port = serverSocketInfo.getPort();

        try {
            DatagramPacket packet = ProtocolPacket.generateClientDatagramPacket(
                    new NodeStatus(this.nodeId, getFileList(), ip, port),
                    ip, port);
            // System.out.println("Sending heartbeat to " + ip + ":" + port);
            selfDatagramSocket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the list of files of node's home directory
     * 
     * @return
     */

    private static String[] getFileList() {
        String directory = System.getProperty("user.dir");
        File homeFolder = new File(directory + "/Home/");
        if(!homeFolder.exists()){
            return null;
        }
        File[] files = homeFolder.listFiles();
        int numFiles = files.length;
        String[] fileList = new String[numFiles];
        for (int i = 0; i < numFiles; i++) {
            fileList[i] = files[i].getName();
            // System.out.println(fileList[i]);
        }
        return fileList;
        
    }

    /**
     * Get the IP address of the current machine
     *
     * @return
     * @throws Exception
     */
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

    /**
     * Prints the file lists of connnected nodes and is alive or dead
     */
    public void printNodeStatus() {
        System.out.println("----------------------");
        System.out.println("Node status as of: " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        if(connectedNodes.size() == 0){
            System.out.println("No node status received from server yet.");
            return;
        }
        for (NodeStatus node : connectedNodes) {
            if (node.getNodeId() == nodeId)
                continue; // if is self continue

            if (!node.hasUpdated) {
                System.out.printf("Node %d: has not sent a heartbeat yet", node.getNodeId());
                System.out.println();
                continue;
            }
            String isAlive = "offline";
            if (node.checkAlive())
                isAlive = "online";

            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(node.getLastHeartbeat()),
                    ZoneId.systemDefault());
            String timeStamp = dateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            float timeSince = ((System.currentTimeMillis() - node.getLastHeartbeat()) / 1000.0f);

            String fileList;

            if(node.getFileList() == null){
                fileList = "Home folder not found";
            }
            else{
                fileList = Arrays.toString(node.getFileList());
            }

            System.out.printf("Node %d (%s:%d): is %s, last heartbeat %s (%f s) and has files: %s", node.getNodeId(), node.socketInfo.getIp(), 
                node.socketInfo.getPort(), isAlive, timeStamp, timeSince, fileList);
            System.out.println();
        }
    }

    /**
     * Runs a loop to send heartbeats to all connected nodes every 0-30 seconds
     * (randomized each time)
     * 
     * @throws Exception
     */
    public void createHeartbeatProcess() {
        try {
            while (true) {
                // Send heartbeat to server
                sendHeartbeat();
                long sleepTime = 2000;
                // long sleepTime = secureRandom.nextInt(30001);
                Thread.sleep(sleepTime);
            }
        } catch (Exception e) {
            throw new RuntimeException("heartbeat proccess was interupted", e);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting P2P Node on port 9876");
        int myPort = 9876;

        // optional: specify port number as argument
        if (args.length > 0) {
            myPort = Integer.parseInt(args[0]);
            System.out.println(myPort);
        }
        // int myPort = 9877;
        ClientNode client;
        try {
            client = new ClientNode(myPort);
        } catch (Exception e) {
            throw e;
        }

        // listening thread
        Thread readThread = new Thread() {
            public void run() {
                client.createAndListenSocket();
            }
        };

        // sending thread
        Thread sendThread = new Thread() {
            public void run() {
                client.createHeartbeatProcess();
            }
        };

        Thread updateThread = new Thread() {
            public void run() {
                try {
                    while (true) {
                        // sleep for 15 seconds
                        Thread.sleep(5 * 1000);
                        client.printNodeStatus();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // start the threads
        readThread.start();
        sendThread.start();
        updateThread.start();

        // wait for the threads to finish
        try {
            readThread.join();
            sendThread.join();
            updateThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}