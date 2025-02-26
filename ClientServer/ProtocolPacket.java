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

public class ProtocolPacket implements Serializable {

    public static final int HEADER_SIZE = 16;
    // Headers
    private byte version = 1;
    private byte type = 0;
    private long timestamp = 0; // automatically
    private int senderId = 0;

    // Body
    private String[] fileNames = null;

    private ProtocolPacket(int senderId, long timestamp, String[] fileNames) {
        this.version = 1;
        this.type = 1;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.fileNames = fileNames;
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
     * @param ip : where to send to
     * @param port : where to send to
     * @return
     * @throws UnknownHostException
     * @throws StreamCorruptedException
     */
    public static DatagramPacket generateDatagramPacket(int id, String[] fileNames, String ip, int port)
            throws UnknownHostException, StreamCorruptedException {
        // serialize
        ProtocolPacket packet = new ProtocolPacket(id, System.currentTimeMillis(), fileNames);
        byte[] data = ProtocolPacket.serialize(packet);
        return new DatagramPacket(data, data.length, InetAddress.getByName(ip), port);
    }

    public byte getVersion() {
        return version;
    }

    public byte getType() {
        return type;
    }

    public int getSenderId() {
        return senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String[] getFileNames() {
        return fileNames;
    }

    public void setFileNames(String[] fileNames) {
        this.fileNames = fileNames;
    }

}
