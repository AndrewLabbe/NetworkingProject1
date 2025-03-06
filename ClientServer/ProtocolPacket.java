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

    // Header size = 10 bytes given the three header fields below
    public static final int HEADER_SIZE = 10;
    // Headers
    private byte version = 1;
    // 0 = client 1 = server
    private byte type = 0;
    private long sentTimestamp = 0; // automatically

    // Body/data
    private ArrayList<NodeStatus> connectedNodes = new ArrayList<NodeStatus>();

    // Packet constructor defining headers
    private ProtocolPacket(ArrayList<NodeStatus> connectedNodes, byte type) {
        this.version = 1;
        this.type = type;
        this.connectedNodes = connectedNodes;
    }

    // Turn Packet object into bytes
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

    // Given byte array, turn it back into protocol packet object
    public static ProtocolPacket deserializePacket(byte[] bytes)
            throws ClassNotFoundException, IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

        ObjectInput in = new ObjectInputStream(bis);
        return (ProtocolPacket) in.readObject();
    }

    /**
     * 
     * @param node      : Node status being transmitted
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

    /**
     * 
     * @param nodes     : Node status(s) being transmitted
     * @param ip        : where to send to
     * @param port      : where to send to
     * @return
     * @throws UnknownHostException
     * @throws StreamCorruptedException
     */
    public static DatagramPacket generateServerDatagramPacket(ArrayList<NodeStatus> nodes, String ip, int port)
            throws UnknownHostException, StreamCorruptedException {
        ProtocolPacket packet = new ProtocolPacket(nodes, (byte) 1);
        byte[] data = ProtocolPacket.serialize(packet);
        return new DatagramPacket(data, data.length, InetAddress.getByName(ip), port);
    }

    // Getters and Setters
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
