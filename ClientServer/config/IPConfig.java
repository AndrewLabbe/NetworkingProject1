package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class IPConfig {

    private static File configFile = new File("clientserver.properties");
    private static Properties config = null;

    private static SocketInfo[] clientSocketInfos = null;
    private static SocketInfo serverSocketInfo = null;

    // "static class" equiv, prevents instantiation
    private IPConfig() {
    };

    /**
     * 
     * @return num of ip port pairs loaded from config
     * @throws IOException
     */
    public static int num_sockets() throws IOException {
        if (clientSocketInfos == null) {
            try {
                loadSockets();
            } catch (IOException e) {
                throw e;
            }
        }
        return clientSocketInfos.length;
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
            serverSocketInfo = new SocketInfo(config.getProperty("server.ip"),
                    Integer.parseInt(config.getProperty("server.port")));
        } catch (IOException e) {
            throw new IOException("Failed to load config file" + e.getMessage());
        }
        int index = 0;
        ArrayList<SocketInfo> sockets = new ArrayList<>();
        System.out.println("Loading sockets");
        do {
            try {
                String clientKey = "client" + index + ".";
                sockets.add(new SocketInfo(config.getProperty(clientKey + "ip"),
                        Integer.parseInt(config.getProperty(clientKey + "port"))));
                index++;
                // when error, there is no more sockets in config
            } catch (Exception e) {
                break;
            }
        } while (true);

        if (sockets.size() == 0) {
            throw new IOException("No sockets found in config file");
        }
        clientSocketInfos = sockets.toArray(new SocketInfo[sockets.size()]);
    }

    /**
     * 
     * @param index
     * @return SocketInfo object with ip and port
     * @throws IOException
     */
    public static SocketInfo getClientSocket(int index) throws IOException {
        if (clientSocketInfos == null) {
            try {
                loadSockets();
            } catch (IOException e) {
                throw e;
            }
        }
        return clientSocketInfos[index];
    }

    public static SocketInfo getServerSocket() throws IOException {
        if (serverSocketInfo == null) {
            try {
                loadSockets();
            } catch (IOException e) {
                throw e;
            }
        }
        return serverSocketInfo;
    }

    // public static void main(String[] args) {
    //     System.out.println("Testing IPConfig...");
    //     try {
    //         System.out.println(
    //                 "Server: " + IPConfig.getServerSocket().getIp() + ":" + IPConfig.getServerSocket().getPort());
    //         for (int i = 0; i < IPConfig.num_sockets(); i++) {
    //             System.out.println("Client" + i + ": " + IPConfig.getclientsocket(i).getIp() + ":"
    //                     + IPConfig.getclientsocket(i).getPort());
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
}
