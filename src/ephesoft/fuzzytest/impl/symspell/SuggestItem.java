package ephesoft.fuzzytest.impl.symspell;

public class SuggestItem implements Comparable<SuggestItem>{

	public String term = "";
	public int distance = 0;
	public long count = 0L;
	
	public SuggestItem(String term, int distance, long count) {
		this.term = term;
		this.distance = distance;
		this.count = count;
	}
	
	public int getHashCode() {
		return this.term != null ? this.term.hashCode() : 0;
	}
	
	@Override
	public int compareTo(SuggestItem obj) {
		if(this.distance == obj.distance) {
			return Long.compare(obj.count, this.count);
		}
		return Integer.compare(this.distance, obj.distance);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj != null) {
			if(this.term != null) {
				return this.term.equals(((SuggestItem)obj).term);
			} else {
				return (((SuggestItem)obj).term != null);
			}
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "{" + term + ", " + distance + ", " + count + "}";
	}
}
