package test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.Peer2PokerClient;
import client.Peer2PokerClient.ServerType;

public class Peer2PokerTest {

    public static final Logger logger = LoggerFactory.getLogger(Peer2PokerTest.class);
	
    public static void main(String[] args) {
        logger.info("Starting Peer2Poker");
        
        Peer2PokerClient client = new Peer2PokerClient(ServerType.NOSERVER);
        client.getServerSocket(9001);
        
    }
}
