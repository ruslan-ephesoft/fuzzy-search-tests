package ephesoft.fuzzytest;

import ephesoft.fuzzytest.util.Util;
import ephesoft.fuzzytest.impl.*;

public class AlgorithmFactory {
	
	public static AlgorithmProcessor getAlgorithmProcessor(int algorithm) { 
		if(algorithm == Util.FS_LONGEST_COMMON_SUBSEQUENCE) {
			return new LongestCommonSubsequenceProcessor();
		} else if(algorithm == Util.FS_BK_TREE) {
			return new BKTreeProcessor();
		} else if(algorithm == Util.FS_SYMSPELL) {
			return new SymSpellProcessor();
		} else {
			return null;
		}
	}

}
