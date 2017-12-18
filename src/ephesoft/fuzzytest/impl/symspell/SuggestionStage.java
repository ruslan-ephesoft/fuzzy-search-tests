package ephesoft.fuzzytest.impl.symspell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SuggestionStage {
	
	private Map<Integer, Entry> deletes;
	private ChunkArray nodes;

	public SuggestionStage(int initialCapacity) {
		deletes = new HashMap<>(initialCapacity);
		nodes = new ChunkArray(initialCapacity * 2);
	}
	
	public Map<Integer, Entry> getDeletes() {
		return deletes;
	}

	public void setDeletes(Map<Integer, Entry> deletes) {
		this.deletes = deletes;
	}

	public ChunkArray getNodes() {
		return nodes;
	}

	public void setNodes(ChunkArray nodes) {
		this.nodes = nodes;
	}

	public int deleteCount() {
		return deletes.size();
	}
	
	public int nodeCount() {
		return nodes.getCount();
	}
	
	public void clear() {
		deletes.clear();
		nodes.clear();
	}
	
	void add(int deleteHash, String suggestion) {
		Entry entry = deletes.get(deleteHash);
		if(entry == null) {
			entry = new Entry(0, -1);
		}
		int next = entry.first;
		entry.count++;
		entry.first = nodes.getCount();
		deletes.put(deleteHash, entry);
		nodes.add(new Node(suggestion, next));
	}
	
	void commitTo(Map<Integer, String[]> permanentDeletes) {
		Set<Integer> keys = deletes.keySet();
		for(Integer key: keys) {
			int i;
			String[] suggestions = permanentDeletes.get(key);
			if(suggestions != null) {
				i = suggestions.length;
				String[] newSuggestions = new String[suggestions.length + deletes.get(key).count];
				System.arraycopy(suggestions, 0, newSuggestions, 0, suggestions.length);
				permanentDeletes.put(key, newSuggestions);
			} else {
				i = 0;
				suggestions = new String[deletes.get(key).count];
				permanentDeletes.put(key, suggestions);
			}
			int next = deletes.get(key).first;
			while(next >= 0) {
				Node node = nodes.getNode(next);
				suggestions[i] = node.suggestion;
				next = node.next;
				i++;
			}
		}
	}
}
