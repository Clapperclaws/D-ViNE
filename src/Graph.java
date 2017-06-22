import java.util.ArrayList;

/*Generic Graph that will be used to represent virtual, IP, and OTN graphs*/
public class Graph {
	// Every node in the graph is associated with a list of endpoints
	private ArrayList<ArrayList<EndPoint>> adjList;
	
	private int[] ports;
	private int[] portCapacity;
	
	//Default constructor
	public Graph(int N){	
		adjList = new ArrayList<ArrayList<EndPoint>>();
		for(int i=0;i<N;i++){
			adjList.add(i,new ArrayList<EndPoint>());
		}
		ports = new int[N];
		portCapacity = new int[N];
	}
	
	//Copy Constructor
	public Graph(Graph g){
		adjList = new ArrayList<ArrayList<EndPoint>>();
		for(int i=0;i<g.adjList.size();i++){
			adjList.add(i,new ArrayList<EndPoint>());
			for(int j=0;j<g.adjList.get(i).size();j++){
				EndPoint p = new EndPoint(g.adjList.get(i).get(j));
				adjList.get(i).add(p);
			}
		}
		ports = new int[adjList.size()];
		portCapacity = new int[adjList.size()];
		for(int i=0;i<ports.length;i++){
			ports[i] = g.ports[i];
			portCapacity[i] = g.portCapacity[i];
		}	
	}
	
	//Merge Constructor
	public Graph(Graph g1, Graph g2){
		//Create Collapsed Graph
		 this(g1.getAdjList().size()+g2.getAdjList().size());
		
		//Begin by adding the first graph Links
		for(int i=0;i<g1.getAdjList().size();i++){
			for(int j=0;j<g1.getAdjList().get(i).size();j++){
				adjList.get(i).add(new EndPoint(g1.getAdjList().get(i).get(j)));
			}
			portCapacity[i] = g1.getPortCapacity()[i];
			ports[i] = g1.getPortCapacity()[i];		
		}
		
		//Continue by adding the Second Graph Links
		for(int i=0;i<g2.getAdjList().size();i++){
			for(int j=0;j<g2.getAdjList().get(i).size();j++){
				EndPoint ep = new EndPoint(g2.getAdjList().get(i).get(j));
				ep.setNodeId(ep.getNodeId()+g1.getAdjList().size());
				adjList.get(g1.getAdjList().size()+i).add(ep);
			}
		}
	}

	//Add a single end point to the list of end points for a given node.
	public void addEndPoint(int nodeId, EndPoint endPnt){	
		adjList.get(nodeId).add(endPnt);
	}
	
	//Add a list of end points for a given node
	public void addEndPointList(int nodeId, ArrayList<EndPoint> endPnts){
		if(adjList.contains(nodeId))//Increment/Aggregate
			adjList.get(nodeId).addAll(endPnts);
		else
			adjList.add(nodeId, endPnts); //Add A New Node
	}
	
	//Get all end points of a given node
	public ArrayList<EndPoint> getAllEndPoints(int nodeId){
		return adjList.get(nodeId);
	}
	
	//Get the bandwidth of an incident link
	public int getBW(int source, int destination){
		
		ArrayList<EndPoint> endPoints = adjList.get(source);
		for(int i=0;i<endPoints.size();i++){
			if(endPoints.get(i).getNodeId() == destination)
				return endPoints.get(i).getBw();
		}
		return -1;
	}
	
	//Get the Weight of an incident link
	public int getCost(int source, int destination){
		
		ArrayList<EndPoint> endPoints = adjList.get(source);
		for(int i=0;i<endPoints.size();i++){
			if(endPoints.get(i).getNodeId() == destination)
				return endPoints.get(i).getCost();
		}
		return -1;
	}
	
	//Get the complete adjacency list
	public ArrayList<ArrayList<EndPoint>> getAdjList(){
		return adjList;
	}
	
	// Returns the number of nodes in the graph.
	public int getNodeCount() {
	    return adjList.size();
	}
	
	public int[] getPorts() {
		return ports;
	}

	public void setPorts(int[] ports) {
		this.ports = ports;
	}
	
	public void setPort(int index, int value) {
		this.ports[index] = value;
	}

	public int[] getPortCapacity() {
		return portCapacity;
	}

	public void setPortCapacity(int[] portCapacity) {
		this.portCapacity = portCapacity;
	}
	
	//Returns the nodes of type "Meta" that are adjacent to a given node
	public ArrayList<EndPoint> getAdjNodesByType(int node, EndPoint.type t){
		
		ArrayList<EndPoint> metaNodes = new ArrayList<EndPoint>();
		for(int i=0;i<adjList.get(node).size();i++){
			if(adjList.get(node).get(i).getT().equals(t))
				metaNodes.add(adjList.get(node).get(i));
		}
		
		return metaNodes;
	}
			
	public int getAdjBW(int nodeId){
		int bw = 0;
		for(int i=0;i<getAllEndPoints(nodeId).size();i++)
			bw += getAllEndPoints(nodeId).get(i).getBw();
		
		return bw;
	}
	
	public int getAdjBWByTpe(int nodeId, EndPoint.type t){
		int bw = 0;
		for(int i=0;i<getAllEndPoints(nodeId).size();i++){
			if(getAllEndPoints(nodeId).get(i).getT().equals(t))
				bw += getAllEndPoints(nodeId).get(i).getBw();
		}
		
		return bw;
	}
	
	//This function returns the # of IP links between the same source & destination nodes
	public int findTupleOrder(int src, int dst){
		int order = 0;
		for(int i=0;i< adjList.get(src).size();i++){
			if(adjList.get(src).get(i).getNodeId() == dst)
				order++;
		}
		return order;
	}
	
	//This function returns the index of the adjacent node
	public int getNodeIndex(int nodeId, int adjNodeId, int order){
		for(int i=0;i<adjList.get(nodeId).size();i++){
			if((adjList.get(nodeId).get(i).getNodeId() == adjNodeId)&&
			   (adjList.get(nodeId).get(i).getOrder()  == order))
				return i;
		}
		return -1;//Two nodes are not neighbors
	}
	
	
	//Print the complete Adjacency List
	public String toString(){
		String content = "Adjacency List:\n";
		for(int i=0;i<adjList.size();i++)
			content += "- Node "+i+" is attached to: \n"+adjList.get(i).toString()+"\n";
		
		for(int i=0;i<ports.length;i++)
			content+= "Node "+i+" has "+ ports[i]+" ports, each of capacity "+portCapacity[i]+"\n";
		
		return content;
	}
	
}
