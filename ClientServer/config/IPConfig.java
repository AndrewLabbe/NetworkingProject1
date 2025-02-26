package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class IPConfig {

    private static File configFile = new File("config.properties");
    private static Properties config = null;

    private static SocketInfo[] socketInfos = null;

    // "static class" equiv, prevents instantiation
    private IPConfig() {
    };

    /**
     * 
     * @return num of ip port pairs loaded from config
     * @throws IOException
     */
    public static int num_sockets() throws IOException {
        if (socketInfos == null) {
            try {
                loadSockets();
            } catch (IOException e) {
                throw e;
            }
        }
        return socketInfos.length;
    }

    /**
     * Loads from config ip port pairs to socketInfos
     *
     * @throws IOException
     */
    // ToDo : support for server vs client
    private static void loadSockets() throws IOException {
        if (config != null)
            return;
        try {
            config = new Properties();
            FileInputStream propsInput = new FileInputStream(configFile);
            config.load(propsInput);
        } catch (IOException e) {
            throw new IOException("Failed to load config file" + e.getMessage());
        }
        int index = 0;
        ArrayList<SocketInfo> sockets = new ArrayList<>();
        System.out.println("Loading sockets");
        do {
            try {
                String serverKey = "server" + index + ".";
                sockets.add(new SocketInfo(config.getProperty(serverKey + "ip"),
                        Integer.parseInt(config.getProperty(serverKey + "port"))));
                index++;
                // when error, there is no more sockets in config
            } catch (Exception e) {
                break;
            }
        } while (true);

        if (sockets.size() == 0) {
            throw new IOException("No sockets found in config file");
        }
        socketInfos = sockets.toArray(new SocketInfo[sockets.size()]);
    }

    /**
     * 
     * @param index
     * @return SocketInfo object with ip and port
     * @throws IOException
     */
    public static SocketInfo getNodeSocket(int index) throws IOException {
        if (socketInfos == null) {
            try {
                loadSockets();
            } catch (IOException e) {
                throw e;
            }
        }
        return socketInfos[index];
    }

    public static void main(String[] args) {
        System.out.println("Testing IPConfig...");
        try {
            for (int i = 0; i < IPConfig.num_sockets(); i++) {
                System.out.println(IPConfig.getNodeSocket(i).getIp() + ":" + IPConfig.getNodeSocket(i).getPort());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
