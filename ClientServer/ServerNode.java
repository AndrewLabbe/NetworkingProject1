import java.io.IOException;
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

public class ServerNode {
    // Fields to store self info regarding socket, random interval of time for heartbeat sending and list of connected nodes in the network
    DatagramSocket selfDatagramSocket = null;
    SecureRandom secureRandom = new SecureRandom();
    ArrayList<NodeStatus> connectedNodes = new ArrayList<NodeStatus>();

    SocketInfo selfSocketInfo;

    /**
     * Constructor for p2pNode
     *
     * @param myPort : port number to listen on for incoming packets
     * @throws Exception
     */
    public ServerNode(int myPort) throws Exception {
        // Populating self socket info (IP/Port) and creating DatagramSocket for connection purposes
        selfSocketInfo = new SocketInfo(getSelfIP(), myPort);
        selfDatagramSocket = new DatagramSocket(myPort);

        // Load connected nodes from config
        loadClients();
    }

    /**
     * Load external nodes to send heartbeats to from config file
     *
     * @throws IOException
     */
    // ToDo: config file needs to be at root to run using the play button, but
    // should be in p2p folder when turned into jar
    private void loadClients() throws IOException {
        // load external nodes from file
        for (int i = 0; i < IPConfig.num_sockets(); i++) {
            SocketInfo socket = IPConfig.getClientSocket(i);
            NodeStatus status = new NodeStatus(i, socket.getIp(), socket.getPort());
            status.setHasUpdated(false);
            connectedNodes.add(status);
        }
    }

    /**
     * Listen for incoming packets, parse, and update info on connected nodes
     */
    public void createAndListenSocket() {
        try {
            byte[] incomingData = new byte[1024];

            while (true) {
                // create datagram packet using incoming data as paramater
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                // accept packet
                selfDatagramSocket.receive(incomingPacket);

                // decode packet
                ProtocolPacket packet = ProtocolPacket.deserializePacket(incomingPacket.getData());

                // check that it is a client packet
                if (packet.getType() == 0) { 
                    NodeStatus node = packet.getNode(0);
                    connectedNodes.get(node.getNodeId()).updateStatus(node.getFileList(), node.getLastHeartbeat());
                }

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
    public void sendClientInfo() {
        for (NodeStatus node : connectedNodes) {
            // Pulling self IP/Port
            String ip = node.socketInfo.getIp();
            int port = node.socketInfo.getPort();

            // Forming the packet with respective info/data to be sent to clients
            try {
                DatagramPacket packet = ProtocolPacket.generateServerDatagramPacket(connectedNodes, ip, port);
                selfDatagramSocket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

        // Iterate though all connectedNode list to print out respective node status(s)
        // Used to print out node information regarding if alive, last heartbeat sent and file listing(s)
        for (NodeStatus node : connectedNodes) {
            String isAlive = "offline";
            if (!node.hasUpdated) {
                System.out.printf("Node %d: has not sent a heartbeat yet", node.getNodeId());
                System.out.println();
                continue;
            }
            if (node.checkAlive())
                isAlive = "online";
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(node.getLastHeartbeat()),
                    ZoneId.systemDefault());
            String timeStamp = dateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            float timeSince = ((System.currentTimeMillis() - node.getLastHeartbeat()) / 1000.0f);

            String fileList;

            // If home folder/directory does not currently exist
            if (node.getFileList() == null) {
                fileList = "Home folder not found";
            } else {
                fileList = Arrays.toString(node.getFileList());
            }

            // Actual print statement
            System.out.printf("Node %d (%s:%d): is %s, last heartbeat %s (%f s) and has files: %s", node.getNodeId(),
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
                // Send node info to each client
                sendClientInfo();
                long sleepTime = 2000;
                // long sleepTime = secureRandom.nextInt(30001);
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
            System.out.println("(Ex: java -jar P2PNode.jar 9876)");
        }
        System.out.println("Starting P2PNode on port " + myPort);
        // int myPort = 9877;
        ServerNode server;
        try {
            server = new ServerNode(myPort);
        } catch (Exception e) {
            throw e;
        }

        // listening thread
        Thread readThread = new Thread() {
            public void run() {
                server.createAndListenSocket();
            }
        };

        // sending thread
        Thread sendThread = new Thread() {
            public void run() {
                server.createHeartbeatProcess();
            }
        };

        // Refresh terminal every 20 seconds to update node status(s)
        Thread updateThread = new Thread() {
            public void run() {
                try {
                    while (true) {
                        // sleep for 15 seconds
                        Thread.sleep(5 * 1000);
                        server.printNodeStatus();
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