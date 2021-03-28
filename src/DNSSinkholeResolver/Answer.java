package DNSSinkholeResolver;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class Answer {
    //Class to analyse the response
    private static final int MAX_REQUESTS = 15;
    InetAddress nextAddress = null;

    DatagramPacket analyseAnswers(DatagramPacket DNSResponse, Client client) throws Exception {
        while (true) {
            int requestsNumber = 0;
            while (finalAddressNotFound(DNSResponse) && requestsNumber <= MAX_REQUESTS) {
                try {
                    nextAddress = getFirstAuthority(DNSResponse);
                }
                catch (Exception e) {
                    System.err.println("Couldn't get address from response's RData");
                }

                try {
                    client.sendRequest(nextAddress);
                    DNSResponse = client.getResponse();
                }
                catch (Exception e) {
                    System.err.println("Failed to send/receive packet while sending to querying servers");
                    break;
                }

                requestsNumber++;
            }
            // If the domain doesn't exist
            if (!(isNumOfAnswersIsZero(DNSResponse) && isResponseCodeNOERROR(DNSResponse))) {
                return DNSResponse;
            }
        }
    }

    // Returns the first authorative IP address found in the response packet from the server
    private InetAddress getFirstAuthority(DatagramPacket datagramPacket) throws Exception {
        String address = getAddressFromData(datagramPacket, getRdataOfFirstServer(datagramPacket));
        return InetAddress.getByName(address);
    }

    // Returns the domain name of the packet
    private String getAddressFromData(DatagramPacket datagramPacket, int startPosition) {
        byte[] data = datagramPacket.getData();
        StringBuilder address = new StringBuilder();
        int position = startPosition;
        byte numBytesToRead = data[position];
        while (numBytesToRead != 0) {
            if ((numBytesToRead & -64) == -64) {
                position = (numBytesToRead & 63) << 8 | data[position + 1];
                numBytesToRead = data[position];
                continue;
            }
            for (int i = 1; i <= numBytesToRead; i++) {
                address.append((char) data[position + i]);
            }

            position += numBytesToRead + 1;
            numBytesToRead = data[position];
            address.append(".");
        }

        return address.substring(0, address.length() - 1);
    }

    private int getRdataOfFirstServer(DatagramPacket datagramPacket) {
        byte[] responseData = datagramPacket.getData();
        int iterator = 12; //Header size;

        // Skip DNSSinkholeResolver.Query fields
        while (responseData[iterator] != 0) {
            iterator++;
        }
        iterator += 5;

        // while still on RR format
        while (responseData[iterator] != 0) {
            iterator++;
        }

        iterator += 10; //  11 bytes are separating between the last byte of the name to the begin of the RData
        return iterator;
    }

    private boolean isResponseCodeNOERROR(DatagramPacket datagramPacket) {
        byte[] data = datagramPacket.getData();
        //Check if the error code is 0 as expected
        return (data[3] & 15) == 0;
    }

    // Returns whether the final IP address has been found
    private boolean finalAddressNotFound(DatagramPacket datagramPacket) {
        return isResponseCodeNOERROR(datagramPacket) &&
                isNumOfAnswersIsZero(datagramPacket) &&
                isNumOfAuthoritiesDifferentThanZero(datagramPacket);
    }


    private boolean isNumOfAnswersIsZero(DatagramPacket datagramPacket) {
        byte[] data = datagramPacket.getData();
        return (data[6] << 8 | data[7]) == 0;
    }

    // Returns if at least one authorative server has been returned
    private boolean isNumOfAuthoritiesDifferentThanZero(DatagramPacket datagramPacket) {
        byte[] data = datagramPacket.getData();
        return (data[8] << 8 | data[9]) > 0;
    }
}
