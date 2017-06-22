import java.util.Comparator;

public class Pair implements Comparable<Pair>{
	
	private int first;
	private int second;
	
	public Pair(int first, int second){
		this.first = first;
		this.second = second;
	}

	public int getFirst() {
		return first;
	}

	public void setFirst(int first) {
		this.first = first;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}

	public int compareTo(Pair p) {
		// TODO Auto-generated method stub
		return ((Integer)p.second).compareTo((Integer)second);
	}	
}
