package ephesoft.fuzzytest.impl;

import java.io.File;

import ephesoft.fuzzytest.AlgorithmProcessor;
import ephesoft.fuzzytest.SearchResult;
import ephesoft.fuzzytest.impl.lcs.Match;
import ephesoft.fuzzytest.impl.lcs.SdkFuzzyConfidenceServiceImpl;
import ephesoft.fuzzytest.util.Util;

public class LongestCommonSubsequenceProcessor implements AlgorithmProcessor{
	
	int algorithm;
	SdkFuzzyConfidenceServiceImpl proc;
	
	public LongestCommonSubsequenceProcessor() {
		algorithm = Util.FS_LONGEST_COMMON_SUBSEQUENCE;
		proc = new SdkFuzzyConfidenceServiceImpl();
	}

	@Override
	public void generateDictionary(File file) throws Exception {
		proc.loadDictionary(file);
	}

	@Override
	public SearchResult search(String input) {
		SearchResult res = new SearchResult();
		Match match = proc.match(input);
		res.distance = -1;
		res.confidence = match.fuzzyMatch;
		res.result = match.term;
		return res;
	}

	@Override
	public int getAlgorithm() {
		return algorithm;
	}
	
	@Override
	public long getDictionarySize() {
		return proc.getDictionarySize();
	}

}
