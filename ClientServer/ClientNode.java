import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;

import config.IPConfig;
import config.SocketInfo;

public class ClientNode {
    // Fields to store self info regarding socket, random interval of time for
    // heartbeat sending and list of connected nodes in the network
    DatagramSocket selfDatagramSocket = null;
    SecureRandom secureRandom = new SecureRandom();
    ArrayList<NodeStatus> connectedNodes = new ArrayList<NodeStatus>();

    // Contains socket information regarding server and self, alongside nodeId
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
                try {
                    DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                    // accept packet
                    selfDatagramSocket.receive(incomingPacket);

                    // decode packet
                    try {
                        ProtocolPacket packet = ProtocolPacket.deserializePacket(incomingPacket.getData());

                        if (packet.getType() == 1) // check that it is a server packet
                            connectedNodes = packet.getConnectedNodes();

                    } catch (Exception e) {
                        System.out.println("Error with deserialization");
                    }
                    Thread.sleep(10);
                } catch (Exception e) {
                    System.out.println("Cannot reach network");
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Listening process was interupted", e);

        }
    }

    /**
     * Send a heartbeat to a specfic node, identified by the NodeStatus object
     */
    public void sendHeartbeat() {
        String ip = serverSocketInfo.getIp();
        int port = serverSocketInfo.getPort();

        // Create datagram packet using the filelist of itself, alongside its nodeId
        try {
            NodeStatus update = new NodeStatus(this.nodeId, getFileList(), ip, port);

            DatagramPacket packet = ProtocolPacket.generateClientDatagramPacket(update, ip, port);

            selfDatagramSocket.send(packet);

        } catch (Exception e) {
            System.out.println("Cannot reach server, packet skipped");
        }
    }

    /**
     * Get the list of files of node's home directory
     *
     * @return fileList
     */
    private static String[] getFileList() {
        String directory = System.getProperty("user.dir");
        File homeFolder = new File(directory + "/home/");

        // If does not exist, return null for computation purposes in printing node
        // status
        if (!homeFolder.exists()) {
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
        System.out.println("Node status as of: "
                + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));

        // Case if no nodes currently connected on the network
        if (connectedNodes.size() == 0) {
            System.out.println("No node status received from server yet.");
            return;
        }

        // Iterate though each node connected to print status and other related
        // information
        for (NodeStatus node : connectedNodes) {
            if (node.getNodeId() == nodeId)
                continue; // if is self continue

            // Node has not yet sent heartbeat
            if (!node.hasUpdated) {
                // System.out.printf("Node %d: has not sent a heartbeat yet", node.getNodeId())
                System.out.printf("Node %d (%s:%d): has not sent a heartbeat yet", node.getNodeId(),
                        node.socketInfo.getIp(), node.socketInfo.getPort());

                System.out.println();
                continue;
            }
            String isAlive = "offline";
            if (node.checkAlive())
                isAlive = "online";

            // Timestamp
            long currentTime = System.currentTimeMillis();
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(node.getLastHeartbeat()),
                    ZoneId.systemDefault());
            String timeStamp = dateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            int timeSince = ((int) (currentTime - node.getLastHeartbeat()) / 1000);

            String fileList;

            // If file listing does not exist
            if (node.getFileList() == null) {
                fileList = "Home folder not found";
            } else {
                fileList = Arrays.toString(node.getFileList());
            }

            // Main print
            System.out.printf("Node %d (%s:%d): is %s, last heartbeat %s (%d s) and has files: %s", node.getNodeId(),
                    node.socketInfo.getIp(),
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
                // long sleepTime = 2000;
                long sleepTime = secureRandom.nextInt(30001);
                Thread.sleep(sleepTime);
            }
        } catch (Exception e) {
            throw new RuntimeException("heartbeat proccess was interupted", e);
        }
    }

    public static void main(String[] args) throws Exception {
        int myPort = 9876;

        // optional: specify port number as argument
        if (args.length > 0) {
            myPort = Integer.parseInt(args[0]);
            System.out.println(myPort);
        } else {
            System.out.println("No port specified, using default port 9876");
            System.out.println("(Ex: java -jar ClientNode.jar 9876)");
        }
        System.out.println("Starting ClientNode on port " + myPort);
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
                        client.printNodeStatus();
                        // sleep for 20 seconds
                        Thread.sleep(20 * 1000);
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