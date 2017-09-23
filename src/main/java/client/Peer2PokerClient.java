package client;

import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.techniques.DirectConnection;

public class Peer2PokerClient {
    
    /**
     * Enum of server types
     */
    public enum ServerType {
        NOSERVER, SETUPSERVER, RELAYSERVER;
    }
    
    public static final Logger logger = LoggerFactory.getLogger(Peer2PokerClient.class);
    private final ServerType serverType;
    
    /**
     * Create a new Peer2PokerClient instance
     * <p>
     * The type of server must be specified. The server type determines which NAT traversal techniques may be used.
     * <p>
     * NOSERVER will only use techniques that do not rely on a central server <br>
     * SETUPSERVER will only use techniques that do not rely on a central server or that only require a server that helps establish direct connections <br>
     * RELAYSERVER will use all techniques and fall back to relaying all messages between clients if no other technique succeeds <br>
     * 
     * @param serverType
     *          The ServerType for this Peer2PokerClient instance
     */
    public Peer2PokerClient(ServerType serverType){
        this.serverType = serverType;
    }
    
    /**
     * Get a new client socket for the given ip and port
     * <p>
     * This method will try a subset of NAT traversal techniques (subset depends on the Peer2PokerClient ServerType)
     * to establish a connection to the given ip address on the given port.
     * <p>
     * The returned Socket is a normal java Socket in every way and can be used as normal.
     * However, the actual ip and port might not be the one specified.
     * This can happen if for example the actual connection is going through the relay server.
     * If this is the case, it will be handled by Peer2Poker in a transparent way. You can use the socket as if the connection was direct.
     * 
     * @param ip
     *          The ip address to connect to
     * @param port
     *          The port to connect on
     * @return
     *          A socket with a (possibly indirect or modified) connection to the given ip and port
     */
    public Socket getClientSocket(String ip, int port){
        Socket socket = null;
        
        
        return socket;
    }
    
    /**
     * Get a new server socket for the given port
     * <p>
     * This method will return a ServerSocket implementation that can be used as a normal java server socket.
     * Depending on the NAT traversal technique you will receive an actual normal java server socket or a modified one.
     * <p>
     * The returned Socket is a java ServerSocket that can be used as normal.
     * However, the actual port might not be the one specified.
     * This can happen if for example the actual connection is going through the relay server.
     * If this is the case, it will be handled by Peer2Poker in a transparent way. You can use the socket as if the connection was direct.
     * <p>
     * When you call accept() on the ServerSocket, depending on the ServerType of this Peer2PokerClient and the success or failure of some NAT traversal techniques
     * a connection will be made to the setupserver/relayserver to prepare for incoming connections 
     * 
     * @param port
     *          The port to accept connections on
     * @return
     */
    public ServerSocket getServerSocket(int port){
        ServerSocket serverSocket = null;
        // First we need to find out if we are already remotely reachable
        logger.info("ATTEMPTING TECHNIQUE: Direct connection");
        if(DirectConnection.isDirectConnectionPossible(this, port)){
            logger.info("RESULT: Direct connection is possible, using regular server socket");
        }else{
            logger.info("RESULT: Direct connection not possible");
        }
        // If this fails, we try to forward the port on the NAT device
        
        return serverSocket;
    }

    /**
     * Getter for the server type
     * 
     * @return
     *      The ServerType for this Peer2PokerClient
     */
    public ServerType getServerType() {
        return this.serverType;
    }

}
