
/* Tuple represents a link; i.e., the index of the source node and destination node of a link*/
public class Tuple {
	
	private int order;
	private int source; // Index of the link's source node
	private int destination; //Index of the link's destination node
	
	//Default Constructor
	public Tuple(){
		order  = -1;
		source = -1;
		destination = -1;
	}
	
	//Initializing Constructor
	public Tuple(int order, int source, int destination){
		this.order  = order;
		this.source = source;
		this.destination = destination;
	}
		
	//Return the order of the tuple
	public int getOrder() {
		return order;
	}

	//Set the order of the tuple
	public void setOrder(int order) {
		this.order = order;
	}

	//Return the index of the source node
	public int getSource() {
		return source;
	}
	
	//Set the index of the source node
	public void setSource(int source) {
		this.source = source;
	}
	
	//Return the index of the destination node
	public int getDestination() {
		return destination;
	}
	
	//Set the index of the destination node
	public void setDestination(int destination) {
		this.destination = destination;
	}
	
	//Print the tuple
	public String toString(){
		return "("+source+","+destination+")";
	}
	
	public boolean equals(Tuple t){
		if((t.order == this.order) && (t.destination == this.destination) && (t.source == this.source))
			return true;
		if((t.order == this.order) && (t.destination == this.source) && (t.source == this.destination ))
			return true;
	
		return false;
	}

}
