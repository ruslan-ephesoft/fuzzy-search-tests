package ephesoft.fuzzytest.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Set;

import ephesoft.fuzzytest.impl.bktree.*;
import ephesoft.fuzzytest.impl.bktree.BkTreeSearcher.Match;
import ephesoft.fuzzytest.AlgorithmProcessor;
import ephesoft.fuzzytest.SearchResult;
import ephesoft.fuzzytest.util.Util;

public class BKTreeProcessor  implements AlgorithmProcessor{

	int algorithm;
	MutableBkTree<CharSequence> bkTree;
	BkTreeSearcher<CharSequence> searcher;
	
	public BKTreeProcessor() {
		algorithm = Util.FS_BK_TREE;
		bkTree = new MutableBkTree<>(new DamerauLevenshteinDistance());
	}

	@Override
	public void generateDictionary(File file) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while((line = br.readLine()) != null) {
			bkTree.add(line.trim().toLowerCase());
		}
		br.close();
		searcher = new BkTreeSearcher<>(bkTree);
	}

	@Override
	public SearchResult search(String input) {
		Set<Match<? extends CharSequence>> result = searcher.search(input, 3);
		if(result != null && result.size() > 0) {
			SearchResult res = new SearchResult();
			res.distance = 1000;
			for(Match<? extends CharSequence> match: result) {
				if(match.getDistance() < res.distance) {
					res.distance = match.getDistance();
					res.result = match.getMatch().toString();
				}
			}
			res.confidence = 1.f - ((float)res.distance)/((float)input.length());
			return res;
		}
		return null;
	}

	@Override
	public int getAlgorithm() {
		return algorithm;
	}

	@Override
	public long getDictionarySize() {
		return 0L;
	}

}
