import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ProtocolPacket implements Serializable {

    private byte version = 0;
    private byte type = 0;
    private int senderId = 0;
    private long timestamp = 0;

    private String[] fileNames = new String[] {};

    private ProtocolPacket(int senderId, long timestamp, String[] fileNames) {
        this.version = 1;
        this.type = 1;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.fileNames = fileNames;
    }

    private ProtocolPacket() {

    }

    public static DatagramPacket generateDatagramPacket(int id, String[] fileNames, String ip, int port)
            throws UnknownHostException {
        // serialize
        ProtocolPacket packet = new ProtocolPacket(id, System.currentTimeMillis(), fileNames);
        byte[] data = packet.serializeToBytes();
        return new DatagramPacket(data, data.length, InetAddress.getByName(ip), port);
    }

    // Method to serialize an object to a byte array
    private byte[] serializeToBytes() {
        byte[] byteArray = null;
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
            out.writeObject(this);
            byteArray = byteOut.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    public static ProtocolPacket deserializePacket(byte[] byteArray) {
        ProtocolPacket protocolPacket = null;
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(byteArray);
                ObjectInputStream in = new ObjectInputStream(byteIn)) {
            protocolPacket = (ProtocolPacket) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return protocolPacket;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
