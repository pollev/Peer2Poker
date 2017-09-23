package client.techniques;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.Peer2PokerClient;
import client.Peer2PokerClient.ServerType;

public class DirectConnection {
    
    
    public static final Logger logger = LoggerFactory.getLogger(DirectConnection.class);
    
    private DirectConnection(){
        // static class, prevent instantiation
    }
    
    /**
     * Test if a direct connection is possible by connecting to our external ip address
     * This test is not very trustworthy under the NOSERVER setting, but it is all we can do.
     * <p>
     * If there is a SETUPSERVER or RELAYSERVER set, this method becomes reliable because it can do a real check
     * @return
     *      false if a direct connection to ourself could not be established
     *      true if a direct connection to ourself could be established
     */
    public static boolean isDirectConnectionPossible(Peer2PokerClient client, int port){
        boolean directConnectionPossible = false;
        if (client.getServerType() == ServerType.NOSERVER){
            logger.warn("server setting is \'NOSERVER\', direct connection test can be unreliable under this setting");
            directConnectionPossible = directConnectionSelfTest(port);
        } else {
            
        }
        
        return directConnectionPossible;
    }
    
    /**
     * Test a direct connection to ourself using our external ip address
     * <p>
     * We test this by first figuring out our external ip address from a webserver.
     * Next we try to connect to this ip address on the given port.
     * <p>
     * This method is unreliable for various reasons, for example, a device (such as a router)
     * can potentially intercept an internal connection to its own remote ip address and forward it back
     * even if it would have blocked the same connection if it was coming from the outside.
     * <p>
     * Another reason is because the servers where we request our external ip address could be inaccessible
     * for any reason
     * @return
     *      false if a direct connection to ourself could not be established
     *      true if a direct connection to ourself could be established
     */
    private static boolean directConnectionSelfTest(int port){
        boolean directConnectionPossible = false;
        String externalIp = null;
        try {
            // Fetch external ip address
            externalIp = getExternalIp();
        } catch (IOException e1) {
            logger.error("could not figure out external ip address: " + e1.getMessage());
            directConnectionPossible = false;
            return directConnectionPossible;
        }
        
        try {
            // Make a test server
            final ServerSocket welcomeSocket = new ServerSocket(port);
            new Thread(() -> startTestListener(welcomeSocket)).start();
            // make a test client
            Socket clientSocket = new Socket();
            clientSocket.setSoTimeout(1000);
            try{
                clientSocket.connect(new InetSocketAddress(externalIp, port), 1000);
                directConnectionPossible = true;
            }catch(SocketTimeoutException e){
                welcomeSocket.close();
                directConnectionPossible = false;
            }finally {
                clientSocket.close();
            }
        } catch (IOException e2) {
            logger.error("io error while doing direct connection selftest: " + e2.getMessage());
            directConnectionPossible = false;
        }
        
        
        return directConnectionPossible;
    }
    
    /**
     * This method gets called in its own thread to start a server socket for testing if a direct connection is possible
     * 
     * @param welcomeSocket
     *          The welcome socket to start
     */
    private static void startTestListener(ServerSocket welcomeSocket){
        try {
            welcomeSocket.accept();
        } catch (IOException e) {
            // Socket closed is the expected exception
            if(!e.getMessage().equalsIgnoreCase("socket closed")){
                logger.error("test server io exception during self test: " + e.getMessage());
            }
        }
    }
    
    private static String getExternalIp() throws IOException{
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                        whatismyip.openStream()));

        String ip = in.readLine(); //you get the IP as a String
        return ip;
    }
}
