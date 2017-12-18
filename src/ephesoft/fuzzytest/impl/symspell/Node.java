package ephesoft.fuzzytest.impl.symspell;

public class Node {

	public String suggestion;
	public int next;

	public Node(String suggestion, int next) {
		this.suggestion = suggestion;
		this.next = next;
	}
	
}
