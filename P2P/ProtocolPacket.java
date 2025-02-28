import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ProtocolPacket implements Serializable {

    public static final int HEADER_SIZE = 16;
    // Headers
    private byte version = 1;
    // 0 = client 1 = server
    private byte type = 0;
    private long sentTimestamp = 0; // automatically

    // Body
    // private String[] fileNames = null;
    private ArrayList<NodeStatus> connectedNodes = new ArrayList<NodeStatus>();

    // private ProtocolPacket(int senderId, long timestamp, String[] fileNames) {
    private ProtocolPacket(ArrayList<NodeStatus> connectedNodes, byte type) {
        this.version = 1;
        this.type = type;
        this.connectedNodes = connectedNodes;
        // this.fileNames = fileNames;
        // this.senderId = senderId;
        // this.timestamp = timestamp;
    }

    private static byte[] serialize(final Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            out.flush();
            return bos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static ProtocolPacket deserializePacket(byte[] bytes)
            throws ClassNotFoundException, IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

        ObjectInput in = new ObjectInputStream(bis);
        return (ProtocolPacket) in.readObject();
    }

    /**
     * 
     * @param id
     * @param fileNames
     * @param ip        : where to send to
     * @param port      : where to send to
     * @return
     * @throws UnknownHostException
     * @throws StreamCorruptedException
     */

    public static DatagramPacket generateClientDatagramPacket(NodeStatus node, String ip, int port)
            throws UnknownHostException, StreamCorruptedException {
        ArrayList<NodeStatus> list = new ArrayList<>();
        list.add(node);
        ProtocolPacket packet = new ProtocolPacket(list, (byte) 0);
        byte[] data = ProtocolPacket.serialize(packet);
        return new DatagramPacket(data, data.length, InetAddress.getByName(ip), port);
    }

    public static DatagramPacket generateServerDatagramPacket(ArrayList<NodeStatus> nodes, String ip, int port)
            throws UnknownHostException, StreamCorruptedException {
        ProtocolPacket packet = new ProtocolPacket(nodes, (byte) 1);
        byte[] data = ProtocolPacket.serialize(packet);
        return new DatagramPacket(data, data.length, InetAddress.getByName(ip), port);
    }

    public byte getVersion() {
        return version;
    }

    public byte getType() {
        return type;
    }

    public long getTimestamp() {
        return sentTimestamp;
    }

    public ArrayList<NodeStatus> getConnectedNodes() {
        return connectedNodes;
    }

    public NodeStatus getNode(int index) {
        return connectedNodes.get(index);
    }

}
