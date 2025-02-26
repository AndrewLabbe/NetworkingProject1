import java.io.File;
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

public class P2PNode {
    DatagramSocket selfDatagramSocket = null;
    SecureRandom secureRandom = new SecureRandom();
    ArrayList<NodeStatus> connectedNodes = new ArrayList<NodeStatus>();

    SocketInfo selfSocketInfo;
    int nodeId = 0;

    /**
     * Constructor for p2pNode
     * @param myPort : port number to listen on for incoming packets
     * @throws Exception
     */
    public P2PNode(int myPort) throws Exception {
        //
        selfSocketInfo = new SocketInfo(getSelfIP(), myPort);
        selfDatagramSocket = new DatagramSocket(myPort);


        // Load connected nodes from config
        loadExternalNodes();
    }

    /**
     * Load external nodes to send heartbeats to from config file
     * @throws IOException
     */
    // ToDo: config file needs to be at root to run using the play button, but should be in p2p folder when turned into jar
    private void loadExternalNodes() throws IOException {
        // load external nodes from file
        for (int i = 0; i < IPConfig.num_sockets(); i++) {
            // if the ip and port is itself, skip
            if (selfSocketInfo.equals(IPConfig.getNodeSocket(i))) {
                System.out.println("Setting id: " + i);
                nodeId = i;
            }
            SocketInfo socket = IPConfig.getNodeSocket(i);
            connectedNodes.add(new NodeStatus(i, socket.getIp(), socket.getPort()));
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
                connectedNodes.get(packet.getSenderId()).updateStatus(packet.getFileNames(), packet.getTimestamp());

                InetAddress IPAddress = incomingPacket.getAddress();
                int port = incomingPacket.getPort();
                System.out.println("Received heartbeat from " + IPAddress.getHostAddress() + ":" + port);

                Thread.sleep(1000);
            }
        } catch (Exception e) {
            throw new RuntimeException("Listening process was interupted", e);
        }
    }

    /**
     * Send a heartbeat to a specfic node, identified by the NodeStatus object
     * @param info
     */
    public void sendHeartbeatTo(NodeStatus info) {
        String ip = info.socketInfo.getIp();
        int port = info.socketInfo.getPort();

        try {
            DatagramPacket packet = ProtocolPacket.generateDatagramPacket(this.nodeId, getFileList(), ip, port);
            System.out.println("Sending heartbeat to " + ip + ":" + port);
            selfDatagramSocket.send(packet);

            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the list of files of node's home directory
     * @return
     */

    private static String[] getFileList() {
        String directory = System.getProperty("user.dir");
        File homeFolder = new File(directory + "/Home/");
        File[] files = homeFolder.listFiles();
        int numFiles = files.length;
        String[] fileList = new String[numFiles];
        for(int i = 0; i < numFiles; i++){
            fileList[i] = files[i].getName();
            // System.out.println(fileList[i]);
        }
        return fileList;
    }
    /**
     * Get the IP address of the current machine
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
        for (NodeStatus node : connectedNodes) {
            if(node.getNodeId() == nodeId) continue; // if is self continue
            // System.out.println("Node " + node.getNodeId() + " is alive: " + node.checkAlive());
            // System.out.println("Node " + node.getNodeId() + " last heartbeat: " + node.getLastHeartbeat());
            // System.out.println("Node " + node.getNodeId() + " has files: " + Arrays.toString(node.getFileList()));

            String isAlive = "offline";
            if(node.checkAlive())
                isAlive = "online";

            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(node.getLastHeartbeat()), ZoneId.systemDefault());
            String timeStamp = dateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            float timeSince = ((System.currentTimeMillis() - node.getLastHeartbeat()) / 1000.0f);

            System.out.printf("Node %d: is %s, last heartbeat %s (%f s) and has files: %s", node.getNodeId(), isAlive, timeStamp, timeSince, Arrays.toString(node.getFileList()));
            System.out.println();
        }
    }

    /**
     * Runs a loop to send heartbeats to all connected nodes every 0-30 seconds (randomized each time)
          * @throws Exception 
          */
    public void createHeartbeatProcess() {
        try {
            while (true) {
                // Send heartbeat to each node
                for (NodeStatus node : connectedNodes) {
                    if(node.getNodeId() == nodeId) continue; // if is self continue
                    sendHeartbeatTo(node);
                }
                // ToDo: Randomize send time (0-30 seconds)
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
        P2PNode server;
        try {
            server = new P2PNode(myPort);
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

        Thread updateThread = new Thread() {
            public void run() {
                try {
                while(true) {
                // sleep for 15 seconds
                Thread.sleep(5*1000);
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
