package test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.SynchronousQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.Peer2PokerClient;
import client.Peer2PokerClient.ServerType;

public class Peer2PokerTest {

    public static final Logger logger = LoggerFactory.getLogger(Peer2PokerTest.class);
	
    public static void main(String[] args) throws InterruptedException, IOException {
        logger.info("Starting Peer2Poker");
        
        Peer2PokerClient p2pclient = new Peer2PokerClient(ServerType.NOSERVER);
        ServerSocket server = p2pclient.getServerSocket(22428);
        System.out.println("server socket is running on port " + server.getLocalPort());
        new Thread(() -> {
            try {
                runServer(server);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }).start();
        while(true){
            long time = System.currentTimeMillis();
            
            Socket clientSocket = new Socket("pollenet.ddns.net", server.getLocalPort());
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String sentence = "hello there";
            outToServer.writeBytes(sentence + '\n');
            String modifiedSentence = inFromServer.readLine();
            logger.info("CLIENT Received: " + modifiedSentence);
            clientSocket.close();
            
            System.out.println((System.currentTimeMillis() - time));
            if(System.currentTimeMillis() - time > 20000){
                logger.error("SYSTEM TOOK TOO LONG TO RESPOND " + (System.currentTimeMillis() - time));
            }
            Thread.sleep(10000);
        }
    }
    
    
    public static void runServer(ServerSocket server) throws IOException, InterruptedException{
        while (true) {
            Socket connectionSocket = server.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            String input = inFromClient.readLine();
            System.out.println("SERVER Received: " + input);
            Thread.sleep(10000);
            
            
            String output = input.toUpperCase() + '\n';
            outToClient.writeBytes(output);
            
            
           }
    }
}
