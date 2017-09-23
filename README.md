# Peer2Poker

Peer2Poker is a java library for setting up peer-to-peer connections.
The goal of this library is to provide a robust and simple way to setup a connection between two clients that may or may not be behind a NAT. This library does not provide any novel techniques, but bundles them to provide a robust and simple implementation that returns a simple java socket that you can use as normal.

These days, setting up a connection between two clients can be a real pain. Unlike web servers, clients are usually not on a network that is setup to allow incoming connections to be established. The usual solutions for this problem are:
	* requiring clients to configure their network to provide port forwarding and modifying their firewall
	* using an intermediary server both clients connect to that receives data from one client and forwards it to the other
Both solutions have obvious drawbacks:
	* clients often do not posses the required knowledge or permissions to implement these settings
	* You require a dedicated server to handle all the communication between clients. The bandwith costs can become quite substantial

This library aims to alleviate those problems or at least significantly reduce the strain on a dedicated server by using existing techniques for NAT traversal.

It should be noted that a dedicated server is still required for some of these techniques, but only to set up the connections between clients, and not to handle all trafic between them. (The Peer2Poker library can be used to run such a server)

NAT traversal techniques
---------------------

This library will attempt to use the following set of techniques (in order) to setup a connection between two clients. It will start with the most normal techniques such as automatic port mapping protocols (no server needed), move over to some basic techniques and more advanced techniques requiring a sever to setup the connection. And optionally fall back on using the server as an actual relay if all else fails.

* _TODO_ Direct connection (no server)
* _TODO_ Port mapping protocols (no server) [Using https://github.com/offbynull/portmapper or https://github.com/4thline/cling]
	* UPnP-IGD
	* NAT-PMP
	* PCP
* _TODO_ Reverse connection (setup server)
* _TODO_ TCP/UDP hole punching (setup server) [STUN or ICE protocol or do it myself]
* _TODO_ (Fallback relay server [TURN OR ICE protocol or do it myself])