import config.SocketInfo;

public class NodeStatus {
    // ToDo: May be good to implement timeout static var (ms)
    public static long TIMEOUT = 30000;

    public int nodeId;
    public long lastHeartbeat;
    public String[] fileList;

    public SocketInfo socketInfo;

    public NodeStatus(String ip, int port) {
        this.socketInfo = new SocketInfo(ip, port);
        this.lastHeartbeat = System.currentTimeMillis();
        this.fileList = new String[0];
    }

    /**
     * Update the status of a node based on the heartbeat packet
     * (Update the last heartbeat time and the file list of the node)
     * Set alive to true
     * @param packet
     * @throws Exception
     */
    public void updateStatus(ProtocolPacket packet) throws Exception {
        if(packet.getSenderId() != this.nodeId) {
            throw new Exception("Packet sender id does not match node id");
        }


        this.lastHeartbeat = packet.getTimestamp();
        this.fileList = packet.getFileNames();
    }

    /**
     * 
     * @param fileList : list of files namnes from node home dir
     * @param time : time of the heartbeat recieved
     */
    public void updateStatus(String[] fileList, long time) {
        this.fileList = fileList;
        this.lastHeartbeat = time;
    }


    /**
     * Check if node has sent a heartbeat within the last 30 seconds
     * @return
     */
    public boolean checkAlive() {
        return System.currentTimeMillis() - lastHeartbeat < 30000;
    }
}
