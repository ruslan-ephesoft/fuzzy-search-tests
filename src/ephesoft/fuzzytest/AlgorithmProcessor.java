package ephesoft.fuzzytest;

import java.io.File;
import java.io.IOException;

public interface AlgorithmProcessor {
	
	public void generateDictionary(File file) throws Exception;
	
	public SearchResult search(String input);
	
	public int getAlgorithm();
	
	public long getDictionarySize();

}
