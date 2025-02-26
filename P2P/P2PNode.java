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
    SecureRandom secureRandom = new SecureRandom();
    ArrayList<NodeStatus> connectedNodes = new ArrayList<NodeStatus>();

    SocketInfo selfSocketInfo;
    int nodeId = 0;

    /**
     * Constructor for p2pNode
     * @param myPort : port number to listen on for incoming packets
     * @throws Exception
     */
    public p2pNode(int myPort) throws Exception {
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
                // ToDo: do we need?
                if (packet == null) {
                    System.out.println("is null");
                    continue;
                }
                
                // ToDo: Implement the logic to update based on recieved packet
                InetAddress IPAddress = incomingPacket.getAddress();
                int port = incomingPacket.getPort();
                System.out.println("Received heartbeat from " + IPAddress.getHostAddress() + ":" + port);


                // ToDo: What should be our proper wait (is 2 seconds good or shorter?)
                Thread.sleep(2000);
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

    // ToDo: Implement Real Logic
    /**
     * Get the list of files of node's home directory
     * @return
     */
    private String[] getFileList() {
        return new String[] { "f1", "f2", "f3" };
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
        // ToDo: Implement printing node status per documentation
        // ToDo: The implementation of tracking if node is alive can be built directly into the NodeStatus Class as checking isAlive thru a method can programmatically check if the node is alive based on timeout
    }

    /**
     * Runs a loop to send heartbeats to all connected nodes every 0-30 seconds (randomized each time)
          * @throws Exception 
          */
    public void createHeartbeatProcess() {
        try {
            while (true) {
                // Send heartbeat to each node
                for (NodeStatus node : connectedNodes) sendHeartbeatTo(node);
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
        p2pNode server;
        try {
            server = new p2pNode(myPort);
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

        // ToDo: Implement thread for updating the node status (Said in class ~15 seconds)

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
