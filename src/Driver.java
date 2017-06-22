import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class Driver {

	private static ArrayList<Tuple> ipLinks;  // List of IP Links
	private static ArrayList<Tuple> otnLinks; // List of OTN Links
	private static ArrayList<Tuple> vnLinks;  // List of VN Links

	public static void main(String [] args) throws IOException{
		
		//1-Initialize OTN Graph
		otnLinks = new ArrayList<Tuple>();
		Graph otn = ReadTopology("./src/Dataset/otn.topo", EndPoint.type.otn,otnLinks);
		System.out.println("******* OTN Graph ******* \n"+otn); // Print graph for testing
	
		//2-Initialize IP Graph & populate ipLinks
		ipLinks = new ArrayList<Tuple>();
		Graph ip  = ReadTopology("./src/Dataset/ip.topo", EndPoint.type.ip,ipLinks);
		System.out.println("******* IP Graph ******* \n"+ip); // Print graph for testing
		//Read IP Ports and Port Capacities
		ReadPortAttributes("./src/Dataset/ip-port",ip);
		for(int i=0;i<ip.getPorts().length;i++)
			System.out.println("Node "+i+" has "+ip.getPorts()[i]+" ports each of capacity "+ip.getPortCapacity()[i]);
		
		//3- Map IP to OTN
		OverlayMapping ipOtn = ReadOverlayMapping(ip.getAdjList().size(), "./src/DataSet/ip.nmap", "./src/DataSet/ip.emap");
		System.out.println("******* IP to OTN Overlay Mapping: ******* \n"+ipOtn);
		
		//4- Initialize VN Graph
		vnLinks = new ArrayList<Tuple>();
		Graph vn  = ReadTopology("./src/Dataset/vn.topo", EndPoint.type.virtual,vnLinks);
		System.out.println("******* VN Graph ******* \n"+vn); // Print graph for testing	
		
		//5- Initialize Location Constraints (Set all IP nodes as candidates for each VN node).
		ArrayList<Integer> locationConstraints[] = ReadLocationConstraints("./src/Dataset/vnloc", vn.getAdjList().size());
		for(int i=0;i<locationConstraints.length;i++){
			System.out.println("Location Constraints of node "+i);
			for(int j=0;j<locationConstraints[i].size();j++)
				System.out.print(locationConstraints[i].get(j)+",");
			System.out.println();
		}
			
		//6- Get Initial Solution
		 Benchmark bm = new Benchmark(ip, otn, ipOtn);
		 bm.executeBenchmark(vn, locationConstraints,0.5,0.5);
	}
	
	public static String ReadFromFile(String filename) throws IOException{
		String content = "";
		BufferedReader br = new BufferedReader(new FileReader(filename));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	        	content += line+"\n";
	        	line = br.readLine();
	        }
	    }finally {
	    	if (br != null)
	    		br.close();
	    }
	    return content;
	}
	
	
	/* This function reads the port info for the IP */
	public static void ReadPortAttributes(String filename, Graph ip) throws IOException{
		String content = ReadFromFile(filename);
		Scanner scanner = new Scanner(content);
		
		while(scanner.hasNextLine()){
			String[] line = scanner.nextLine().split(",");
			ip.getPorts()[Integer.parseInt(line[0])] = Integer.parseInt(line[1]);
			ip.getPortCapacity()[Integer.parseInt(line[0])] = Integer.parseInt(line[2]);
		}
	}
	
	/* This function reads the location constraints */
	public static ArrayList<Integer>[]  ReadLocationConstraints(String fileName, int n) throws IOException{
		ArrayList<Integer>[] loc = new ArrayList[n];
		
		String content = ReadFromFile(fileName);
		Scanner scanner = new Scanner(content);
		while(scanner.hasNextLine()){
			String[] line = scanner.nextLine().split(",");
			loc[Integer.parseInt(line[0])] = new ArrayList<Integer>();
			for(int i=1;i<line.length;i++)
				loc[Integer.parseInt(line[0])].add(Integer.parseInt(line[i]));
		}	
		return loc;
	}
	
	/* This function reads the IP-OTN OverlayMapping Solution*/
	public static OverlayMapping ReadOverlayMapping(int n, String nodeMappingFile, String linkMappingFile) throws IOException{
		
		//Create OverlayMapping Solution
		OverlayMapping ipOtn = new OverlayMapping(n);
		
		//Read Node Mapping Solution
		Scanner scanner = new Scanner(ReadFromFile(nodeMappingFile));
		while(scanner.hasNextLine()){
			String[] line = scanner.nextLine().split(",");
			ipOtn.nodeMapping[Integer.parseInt(line[0])] = Integer.parseInt(line[1]);
		}
		
		//Read Link Mapping Solution
		scanner = new Scanner(ReadFromFile(linkMappingFile));
		while(scanner.hasNextLine()){
			String[] splitLine = scanner.nextLine().split(",");
			Tuple ipLink = new Tuple(0,Integer.parseInt(splitLine[0]), Integer.parseInt(splitLine[1]));
			
			for(int i=0;i<ipLinks.size();i++){
				if(ipLinks.get(i).equals(ipLink))
					ipOtn.setLinkMapping(ipLinks.get(i),new Tuple(0,Integer.parseInt(splitLine[2]), Integer.parseInt(splitLine[3])));            
			}	
		}
		return ipOtn;
	}
	
	
	/*This function reads a graph from file*/
	public static Graph ReadTopology(String filename, EndPoint.type graphType, ArrayList<Tuple> links) throws IOException{
	
		Scanner scanner = new Scanner(ReadFromFile(filename));
		Graph g= new Graph(Integer.parseInt(scanner.nextLine())); //Initialize Graph with the number of Nodes

		while (scanner.hasNextLine()) {
		  String line = scanner.nextLine();
            if(line != null){
            	//Split the line to get 0: index of the first node; 1: index of the second node; 2: bandwidth
            	String[] splitLine = line.split(",");
            	//Create Tuple for this link
            	links.add(new Tuple(0,Integer.parseInt(splitLine[0]),Integer.parseInt(splitLine[1])));
            	//Create two end points
            	EndPoint ep1 = new EndPoint(Integer.parseInt(splitLine[1]),Integer.parseInt(splitLine[2]),Integer.parseInt(splitLine[3]),graphType,0);
            	g.addEndPoint(Integer.parseInt(splitLine[0]), ep1);
               	EndPoint ep2 = new EndPoint(Integer.parseInt(splitLine[0]),Integer.parseInt(splitLine[2]),Integer.parseInt(splitLine[3]),graphType,0);
            	g.addEndPoint(Integer.parseInt(splitLine[1]), ep2);
            }
        }       
	    return g;
	}
	
	
}
