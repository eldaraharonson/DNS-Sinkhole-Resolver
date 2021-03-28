Itay Gershon - 308493808 itay.gershon
Eldar Aharonson - 313371833 eldar.aharonson

SinkholeServer.java - Initializes the DNS server with the option of adding a blocklist file.
UDPServer.java - Activates the server and uses OOP principles to iteratively resolve the domain requests.

Bugs: When running the program with a domain name that is not in the blocklist we weren't 
able to print the corresponding IP address to the console however we were able to iteratively
find it.  