# Dynavine
## Input file format

The following files are required for the program to run:
* IP network topology
* OTN topology
* VN topology
* IP port information
* IP to OTN node mapping
* IP to OTN link mapping
* Location constraint for VN

### Topology File (Example: [src/Dataset/otn.topo](https://github.com/Clapperclaws/Dynavine/blob/master/src/Dataset/otn.topo))
A topology file contains the list of edges. Each line contains a description of
an edge in a comma separated value (CSV) format. Format of a line is as follows:
```
<SourceNodeId>,<DestinationNodeId>,<Cost>,<Bandwidth>
```
Where,

* SourceNodeId = 0-based node index of the source of the link
* DestinationNodeId = 0-based node index of the destination of the link
* Cost = Cost of provisioning unit bandwidth on this link. Cost is ignored for
virtual links.
* Bandwidth = Available bandwidth on a link. In case of virtual link, this field
  represents bandwidth requirement.

### IP Port Information (Example: [src/Dataset/ip-port](https://github.com/Clapperclaws/Dynavine/blob/master/src/Dataset/ip-port))
IP port information file contains as many lines as the number of IP nodes. Each
line is a set of comma spearated values and has the following format:
```
<ip_node_id>,<residual_port_count>,<port_capacity>
```
The first value represents the 0-based node id of an IP node, the second number 
represents the number of ports available on the IP node, and the third number 
represents the capacity of the ports on that IP node.

### IP to OTN Node Mapping (Example: [src/Dataset/ip.nmap](https://github.com/Clapperclaws/Dynavine/blob/master/src/Dataset/otn.topo))
This file contains as many lines as the number of IP nodes. Each line contains
two values, separated by a comma. 
```
<ip_node_id>,<otn_node_id>
```
The first value represents the zero based node
id of an IP node and the second value represents the zero based node id of an
OTN node where the IP node is attached.

### IP to OTN Link Mapping (Example: [src/Dataset/ip.emap](https://github.com/Clapperclaws/Dynavine/blob/master/src/Dataset/otn.topo))
Each line in this file is a set of comma separated values in the following
format:
```
<iplink_src>,<iplink_dst>,<otnlink_src>,<otnlink_dst>
```
The first two value in a line represents a tuple corresponding to an IP link. 
The second two value represents a tuple corresponding to an OTN link. If an IP 
link is mapped on an OTN path of length three then there will be three lines 
that altogether describe the IP link to OTN link mapping.

### Location Constraint File (Example: [src/Dataset/vnloc](https://github.com/Clapperclaws/Dynavine/blob/master/src/Dataset/otn.topo))
A location constraint file contains as many lines as the number of virtual
nodes. Each line is a comma separated list of values. The first value indicates
the id of a virtual node followed by the ids of IP nodes where this virtual node 
can be mapped.
