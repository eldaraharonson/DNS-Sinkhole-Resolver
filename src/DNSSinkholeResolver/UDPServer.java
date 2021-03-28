package DNSSinkholeResolver;

import java.io.*;
import java.net.*;
import java.util.HashSet;

public class UDPServer {
    private static String domainName;
    private static final int PORT = 5300;
    public DatagramSocket socket;
    InetAddress sourceAddress;
    DatagramPacket DNSResponse = null;
    HashSet<String> blockList = null;
    Client client;
    DatagramPacket queryAnswerPacket;

    public UDPServer() throws Exception {
        this.socket = new DatagramSocket(PORT);
        this.client = new Client();
    }

    // Overloaded constructor with option of blockList
    public UDPServer(HashSet<String> blockListDomains) throws Exception {
        this.socket = new DatagramSocket(PORT);
        this.client = new Client();
        this.blockList = blockListDomains;
    }

    public void run() throws Exception {
        Query query = new Query(socket);

        while (true) {
            try {
                query.getRequest(); //Get DNS request from client
                domainName = query.getDomainName();
                if (this.blockList != null) {
                    // checks if the domain sent is in the blockList
                    if (this.blockList.contains(domainName)) {
                        sendNameError(query.receivePacket);
                    }
                }
                sourceAddress = query.getSourceAddress();
            }
            catch (Exception e) {
                System.err.println("Couldn't receive dig request");
            }
            try {
                InetAddress RandomRootServer = InetAddress.getByName(query.getRandomRootServerHostName());
                client.sendRequest(RandomRootServer); //Forward the request to one of the root servers
                DNSResponse = client.getResponse();
                Answer ans = new Answer();
                queryAnswerPacket = ans.analyseAnswers(DNSResponse, client);
                socket.send(buildUserPacket(queryAnswerPacket, query));
            }
            catch (Exception e) {
                System.err.println("Couldn't send request and get response from Random root server");
            }
        }
    }

    public static String getDomainName() {
        return domainName;
    }

    // Sends back a "Name Error" response
    private void sendNameError(DatagramPacket packet) throws Exception {
        byte[] data = packet.getData();
        data[2] = (byte) ((data[2] & -5) | 0b10000000);
        data[3] = (byte) (((data[3] | 0b10000000) & 0b11110000) | 3);
        DatagramPacket packetToSend = new DatagramPacket(data, packet.getLength(), packet.getAddress(), packet.getPort());
        this.socket.send(packetToSend);
    }

    // Creates the packet that will be sent back to the client
    private DatagramPacket buildUserPacket(DatagramPacket datagramPacket, Query query) throws Exception {
        byte[] data = datagramPacket.getData();
        data[2] = (byte) ((data[2] & -5) | 0b10000000);
        data[3] = (byte) (data[3] | 0b10000000);
        int destPort = query.getDestPort();
        InetAddress destAddress = query.getSourceAddress();
        DatagramPacket packet = new DatagramPacket(data, datagramPacket.getLength(), destAddress, destPort);

        return packet;
    }
}




