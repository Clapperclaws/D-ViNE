import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Benchmark {
	
	Graph collapsedGraph;
	int ipNodesSize  = 0;
	int otnNodesSize = 0;

	public Benchmark(Graph ip, Graph otn, OverlayMapping ipOtn){
	
		ipNodesSize  = ip.getNodeCount();
		otnNodesSize = otn.getNodeCount();
		
		//1- Create Collapsed Graph
		 collapsedGraph = new Graph(ip,otn);
		//Create new IP Links that connects the IP node to the OTN node - Cost of 1 and unlimited capacity
		for(int i=0;i<ip.getAdjList().size();i++){
			collapsedGraph.addEndPoint(i,new EndPoint(ip.getAdjList().size()+ipOtn.getNodeMapping(i),1,ip.getPortCapacity()[i],EndPoint.type.otn,0));
			collapsedGraph.addEndPoint(ip.getAdjList().size()+ipOtn.getNodeMapping(i),new EndPoint(i,1,ip.getPortCapacity()[i],EndPoint.type.ip,0));		
		}
		// System.out.println("Collapsed Graph: \n"+collapsedGraph);
	}
	
	/*
	 * Execute the Benchmark algorithm. alfa and beta are weight functions for 
	 * balancing the relative influence of the residual IP and OTN capacity
	 */
	
	public Solutions executeBenchmark(Graph vn, ArrayList<Integer>[] locationConstraints, double alfa, double beta){
	   	
		Solutions sol = new Solutions(vn.getAdjList().size(), ipNodesSize);
		
		//1- Perform Node Embedding
		int[] nodeEmbedding = new int[vn.getAdjList().size()];
		
		//1- Compute H(ns) for every node
		int[] costArray = new int[ipNodesSize];
		for(int i=0;i<ipNodesSize;i++){
			int sumAdjIpBW  = collapsedGraph.getAdjBWByTpe(i, EndPoint.type.ip);
			int sumAdjOtnBW = collapsedGraph.getAdjBWByTpe(i, EndPoint.type.otn);
			costArray[i] = (int)((alfa * sumAdjIpBW) + (beta * sumAdjOtnBW));
		}
		
		//For Testing Purposes
		/*System.out.println("Cost Array");
		for(int i=0;i<ipNodesSize;i++){
			System.out.println(i+","+costArray[i]);
		}*/
		
		//2- Sort the array in decreasing order of Costs
		int[] pNodesOrder = sortArrayDescendingOrder(costArray);
		// System.out.println("Physical Nodes Sorted in Decreasing Order of Cost Array Values");
		/*for(int i=0;i<pNodesOrder.length;i++){
			System.out.print(pNodesOrder[i]+",");
		}
		System.out.println();*/
				
		//3- Sort the VNs in decreasing order of sum of adj BW demands
		int[] vnBWDemands = new int[vn.getNodeCount()];
		for(int i=0;i<vnBWDemands.length;i++)
			vnBWDemands[i] = vn.getAdjBW(i);
		int[] vNodesOrder = sortArrayDescendingOrder(vnBWDemands);
		
		// System.out.println("VN Nodes Sorted in Decreasing Order of Sum Adj Bw");
		/*for(int i=0;i<vNodesOrder.length;i++){
			System.out.print(vNodesOrder[i]+",");
		}*/
		// System.out.println();
		
		//4- Perform one-to-one embedding
		a: for(int i=0;i<vNodesOrder.length;i++){
			for(int j=0;j<pNodesOrder.length;j++){
				if(locationConstraints[vNodesOrder[i]].contains(pNodesOrder[j]) 
						&& !sol.vnIp.isOccupied(pNodesOrder[j])){
					// System.out.println(vNodesOrder[i]+"->"+pNodesOrder[j]);
					sol.vnIp.setNodeMappingSolution(vNodesOrder[i],pNodesOrder[j]);
				 continue a;
				}
			}
			// System.out.println("Failed to find a Feasible Node Embedding Solution!");
			return null;
		}
		
		//2- Perform Link Embedding
		for(int i=0;i<vn.getNodeCount();i++){			
			for(int j=i;j<vn.getNodeCount();j++){
				Dijkstra dj = new Dijkstra(collapsedGraph);
				
				if(i==j)
					continue;
				
				int bwDemand = vn.getBW(i, j, 0); //Does this VLink Exists?
				if(bwDemand == -1)//it means this link doesn't exist
					continue;
				
				// System.out.println("Embedding Virtual Link: ("+i+","+j+")");
				//Get The Node Embedding of "i"
				int srcIP = sol.vnIp.getNodeMapping(i);
				int dstIP = sol.vnIp.getNodeMapping(j);
				
				//Perform Dijkstra
				ArrayList<Tuple> t = dj.getPath(srcIP,dstIP, bwDemand);
				if(t == null)
					return null;
				// System.out.println(t);
				//Update Network Capacity
				//updateResidualCapacity(t, bwDemand);
				
				//Aggergate into final solution
				boolean ret = aggregateFinalSolution(new Tuple(0,i,j), t, bwDemand, sol);		
        if (!ret)
          return null;
			}
		}
		//3- Add Link Mapping to the Overlay Mapping Sol
		//omSol.linkMapping = linkEmbeddingSolution;
		// System.out.println("Overlay Mapping Solution: \n"+sol);
		return sol;
	}
	
	public void updateResidualCapacity(ArrayList<Tuple> path, int bw){
	    //Update Network Capacity
		for(int k=0;k<path.size();k++){
			int src = path.get(k).getSource(); // Get the first edge of the link
			int dst = path.get(k).getDestination(); // Get the second edge of the link
			int dstIndex = collapsedGraph.getNodeIndex(src, dst, path.get(k).getOrder()); //Find the index of the second edge
			int srcIndex = collapsedGraph.getNodeIndex(dst, src, path.get(k).getOrder()); //Find the index of the first edge
			
			//Update BW Capacity for the first edge
			collapsedGraph.getAdjList().get(src).get(dstIndex).
			setBw(collapsedGraph.getAdjList().get(src).get(dstIndex).getBw()-bw);
	
			//Update BW Capacity for the second edge
			collapsedGraph.getAdjList().get(dst).get(srcIndex).
			setBw(collapsedGraph.getAdjList().get(dst).get(srcIndex).getBw()-bw);
		}
	}
	
    // This function iteratively aggregates the final Solution after every
    // iteration.
    public boolean aggregateFinalSolution(Tuple vLink, ArrayList<Tuple> path, int bw, Solutions sol) {    	            
        //Initialize a Path entry for the VLink
        sol.vnIp.linkMapping.put(vLink, new ArrayList<Tuple>());
        
        //Initialize Potential Variables that will be used to populate New IP Link Path
        ArrayList<Tuple> newIpLinkPath = new ArrayList<Tuple>();
        int srcIP = -1;
        int dstIP = -1;
        for(int i=0;i<path.size();i++){
        	
        	// Examine  Link
            int src = path.get(i).getSource();
            int dst = path.get(i).getDestination();
            
            //Case of IP -> IP
            if((src >=0 && src <ipNodesSize) && (dst >=0 && dst <ipNodesSize)){
            	//Add to VLink Path in VN->IP overlay solution
            	 sol.vnIp.linkMapping.get(vLink).add(path.get(i));
            }
          
            // Case of IP -> OTN
            if ((src >=0 && src <ipNodesSize)&&
            		dst >= ipNodesSize && dst < (otnNodesSize + ipNodesSize)) {
            	 // First check if both source and destination IP nodes have 
               // at least one port available or not.
               if(collapsedGraph.getPorts()[src] <= 1)
                 return false;

            	  // Add the OTN dst as the Node embedding of the IP src.
                sol.ipOtn.nodeMapping[src] = dst;
                srcIP = src;
                
                //Initialize a new path in the IP->OTN Solution
                newIpLinkPath = new ArrayList<Tuple>();
            }
            
            // Case of New OTN -> OTN
            if ((src >= ipNodesSize && src < (otnNodesSize + ipNodesSize))&&
            		dst >= ipNodesSize && dst < (otnNodesSize + ipNodesSize)) {
            	
            	// Add link to IP Path
            	newIpLinkPath.add(path.get(i));
            }
            
            // Case of OTN -> IP
            if ((dst >=0 && dst <ipNodesSize)&&
            		src >= ipNodesSize && src < (otnNodesSize + ipNodesSize)) {
            	
               if(collapsedGraph.getPorts()[dst] <= 1)
                 return false;
            	// Add the OTN src as the Node embedding of the IP dst.
                sol.ipOtn.nodeMapping[dst] = src;
                dstIP = dst;
          
                //Find the order of the new IP Link
                int tupleOrder = collapsedGraph.findTupleOrder(srcIP, dstIP);
                
                //Create new Tup for the IP Link
                Tuple ipTup = new Tuple(tupleOrder,srcIP,dstIP);
                
                //Add the path in the IP->OTN Overlay Solution
                sol.ipOtn.linkMapping.put(ipTup,newIpLinkPath);
             
                //Add the IP Link in the VN->IP Overlay Solution
                sol.vnIp.linkMapping.get(vLink).add(ipTup);                  
                
                // Get Bandwidth Capacity of new IP Link
                int newIPLinkCap = Math.min(
                        collapsedGraph.getPortCapacity()[srcIP],
                        collapsedGraph.getPortCapacity()[dstIP]);

                // Add ipSrc as neighbor of ipDst
                collapsedGraph.addEndPoint(srcIP, new EndPoint(dstIP, 1,
                        newIPLinkCap, EndPoint.type.ip, tupleOrder));

                // Add ipDst as neighbor of ipSrc 
                collapsedGraph.addEndPoint(dstIP, new EndPoint(srcIP, 1,
                        newIPLinkCap, EndPoint.type.ip, tupleOrder));
            
                // Add IP Link to the list of new IP Links
                sol.newIpLinks.add(ipTup);
                
                //Update IP Ports 
                collapsedGraph.setPort(srcIP, collapsedGraph.getPorts()[srcIP]-1);
                collapsedGraph.setPort(dstIP, collapsedGraph.getPorts()[dstIP]-1);
                
                //Update OTN Links Capacity
                updateResidualCapacity(newIpLinkPath, newIPLinkCap); 
            }       
        }
              
       //Update IP Links Capacity
       updateResidualCapacity(sol.vnIp.linkMapping.get(vLink), bw); 
       return true;
       // System.out.println("Test Collapsed Graph for Residual Capacity \n"+collapsedGraph);
    }

	public int[] sortArrayDescendingOrder(int [] array){
		ArrayList<Pair> list = new ArrayList<Pair>();
		for(int i=0;i<array.length;i++)
			list.add(new Pair(i,array[i]));
		
		Collections.sort(list);
		int[] order = new int[array.length];
		for(int i=0;i<list.size();i++)
			order[i] = list.get(i).getFirst();
		return order;
	}
	
}

