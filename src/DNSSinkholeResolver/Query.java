package DNSSinkholeResolver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

//Class to send query and receive response
public class Query {
    DatagramSocket socket;
    String[] rootServers;
    byte[] receiveData = new byte[1024];
    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

    public Query(DatagramSocket skt) {
        this.socket = skt;
        this.rootServers = getAllRootServers();
    }

    void getRequest() throws IOException {
        socket.receive(receivePacket);
    }

    public String extractDomainName(DatagramPacket query) {
        int position = 12;
        byte[] data = query.getData();
        byte numberOfBytesInDomainName = data[position];
        StringBuilder domainName = new StringBuilder();
        while (numberOfBytesInDomainName > 0) {
            for (int i = 1; i <= numberOfBytesInDomainName; i++) {
                char nextLetter = (char) data[position + i];
                domainName.append(nextLetter);
            }
            position += numberOfBytesInDomainName + 1;
            numberOfBytesInDomainName = data[position];
            domainName.append(".");
        }

        return domainName.substring(0, domainName.length() - 1);
    }

    public String getDomainName() {
        return extractDomainName(receivePacket);
    }

    public InetAddress getSourceAddress() {
        return receivePacket.getAddress();
    }

    int getDestPort() throws IOException {
        return receivePacket.getPort();
    }

    private String[] getAllRootServers() {
        //Initializes 13 root servers
        rootServers = new String[13];
        rootServers[0] = "a.root-servers.net";
        rootServers[1] = "b.root-servers.net";
        rootServers[2] = "c.root-servers.net";
        rootServers[3] = "d.root-servers.net";
        rootServers[4] = "e.root-servers.net";
        rootServers[5] = "f.root-servers.net";
        rootServers[6] = "g.root-servers.net";
        rootServers[7] = "h.root-servers.net";
        rootServers[8] = "i.root-servers.net";
        rootServers[9] = "j.root-servers.net";
        rootServers[10] = "k.root-servers.net";
        rootServers[11] = "l.root-servers.net";
        rootServers[12] = "m.root-servers.net";
        return rootServers;
    }

    public String getRandomRootServerHostName() {
        Random random = new Random();
        int randomInt = random.nextInt(13);
        return this.rootServers[randomInt];
    }
}
