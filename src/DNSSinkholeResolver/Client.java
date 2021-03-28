package DNSSinkholeResolver;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Client {
    DatagramSocket socket;
    private static final int DNS_PORT = 53;
    public Client() {
        try {
            this.socket = new DatagramSocket(DNS_PORT);
        }
        catch (SocketException e) {
            System.err.println("Error creating socket");
        }
    }

    void sendRequest(InetAddress nextAddress) throws IOException {
        //Sends request to a predefined root server
        String domain = UDPServer.getDomainName();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        //Constructing the query
        dataOutputStream.writeShort(0x1234);
        dataOutputStream.writeShort(0x0000);
        dataOutputStream.writeShort(0x0001);
        dataOutputStream.writeShort(0x0000);
        dataOutputStream.writeShort(0x0000);
        dataOutputStream.writeShort(0x0000);

        String[] domainParts = domain.split("\\.");		//Splits given domian wrt '.'
        for (int i = 0; i < domainParts.length; i++) {
            byte[] domainBytes = domainParts[i].getBytes("UTF-8");
            dataOutputStream.writeByte(domainBytes.length);
            dataOutputStream.write(domainBytes);
        }

        dataOutputStream.writeByte(0x00);
        dataOutputStream.writeShort(0x0001);
        dataOutputStream.writeShort(0x0001);

        byte[] dnsFrameByteArray = byteArrayOutputStream.toByteArray();
        DatagramPacket datagramPacket = new DatagramPacket(dnsFrameByteArray, dnsFrameByteArray.length, nextAddress, DNS_PORT);
        socket.send(datagramPacket);	//Send the request to obtained IP address
    }

    public DatagramPacket getResponse() throws IOException {
        byte[] receivedData = new byte[1024];
        DatagramPacket DNSResponse = new DatagramPacket(receivedData, receivedData.length);
        socket.receive(DNSResponse);
        return DNSResponse;
    }
}
