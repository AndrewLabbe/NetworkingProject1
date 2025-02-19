# Networking Project 1

## Plan P2P

- [ ] Create Node Class
  - [ ] Send Updates
  - [ ] Have a file structure
- [ ] Connect multiple nodes

## Project Goal

The goal is to design and implement a simple **application layer protocol** over **UDP** to facilitate High Availability Cluster (HAC). HAC has a set of mechanisms to detect failovers, network/node failure etc. in order to re-route the traffic to the available systems. In this project your task is to design and implement a protocol to maintain the up-node (alive or dead) information throughout the cluster. Additionally, each node will have a complete up-to-date map of the file listing of a designated “/home/” directory for all the other nodes of the cluster.

## Guidelines

Your designed protocol should perform the following functions:

a) To detect node failure periodically (\*\*also the Server failure in case of Client-Server mode)

b) To be able to detect when the failed node comes back to life

c) To display the up-to-date file listing of “/home/” directory of all the live nodes

- Should be compatible with both p2p and server-client
- Consistent protocol structure

### P2P Mode (UDP)

- Each node communicates to all other nodes
  - All nodes should send updates to all other nodes
- Each node sends its availability & file listing map to all other nodes (assume that the IP addresses of all the nodes are available in a config file)
  - Random intervals between 0-30 seconds (use SecureRandom class)
  - In the output panel, the availability of all the nodes in the network is displayed (along with file listing) in the form of a list
  - Assume that each node is aware that there are ‘n-1’ other nodes in the network, whose ip addresses (and port numbers) are available in the config file.

### Server-Client Mode (UDP)

- All nodes (clients) send update to a central "server" node
- Client update server, Server updates nodes
- Client will generate its availability (and file listing map) packet after a random interval of time, which will be randomly selected between 0 seconds to 30 seconds
- Server will listen to the availability of all the clients and will generate the packet with all clients’ availability (and file listing map) and will forward it to all the clients.
- Server considers a client dead, if it does not hear anything from any client till 30 seconds. However, a client can anytime come back and send its availability to Server.

[Optional step: Attempt only if you are positive that you can do this!!!] It is possible that a Server will go down, in such an event one of the Clients will assume the role of the Server, and it is known as automatic failover. Your design should consider an approach about how the automatic failover would happen. When the Server that went down comes back online, it assumes the role of a Client to start with.

HINT: A typical protocol packet contains version, length, flags, reserved (for future) and other major important fields/sections that contain protocol specific information.

## Implementation Guidelines from Doc

- You would be designing the HAC protocol and implement it over UDP in P2P and Client-Server mode. - Your implementation would be tested in one mode at a time i.e. either P2P or Client-Server. - Create two Java projects: one for P2P implementation and another for Client-Server. - _If you choose Java, you are required to use **DatagramPacket** and **DatagramSocket** class present in Java library for UDP communication._ - Irrespective of your mode, the protocol structure (format) will remain the same. - For testing purposes, it is recommended that you use at least 3 computers, however the development could be done using “localhost” or loopback ip-address(127.0.0.1). If you do not have access to 3 computers, you may use free AWS cloud for additional nodes. -
  https://docs.oracle.com/javase/tutorial/networking/overview/networking.html

If in any case you need more clarification or information, do not hesitate to contact me through email.

## Setup From Doc

1.  Your cluster needs at least 6 total nodes. You may have 6 nodes by running 2 VMs on each laptop. You can use a hypervisor like VirtualBox.

2.  In Peer-2-Peer mode each node will act as an independent data node (totaling to 6 nodes). In Client-Server, one node will act as a server and the remaining 5 nodes will act as client data nodes.
3.  You must use UDP protocol for your network implementation.
4.  During the final demo, you will demonstrate your implementation with the test cases that will be provided to you. Your project will be evaluated on the basis of clarity of design, team dynamics, correctness of cluster implementation and accuracy of your final results.

5.  You will submit a project report and a zip of your entire source code. Your report must contain the design of your P2P and Client-Se
