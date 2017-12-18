package ephesoft.fuzzytest.impl;

import java.io.File;
import java.util.List;

import ephesoft.fuzzytest.impl.symspell.*;

import ephesoft.fuzzytest.AlgorithmProcessor;
import ephesoft.fuzzytest.SearchResult;
import ephesoft.fuzzytest.util.Util;

public class SymSpellProcessor  implements AlgorithmProcessor{

	int algorithm;
	SymSpell symSpell;
	
	public SymSpellProcessor() {
		algorithm = Util.FS_SYMSPELL;
		symSpell = new SymSpell();
		symSpell.setProcessingLineAsWord(true);
	}

	@Override
	public void generateDictionary(File file) throws Exception {
		if(!symSpell.createDictionary(file.getAbsolutePath())) {
			throw new RuntimeException("File " + file.getAbsolutePath() +  " doesn't exist!");
		}
	}

	@Override
	public SearchResult search(String input) {
		List<SuggestItem> result = symSpell.lookup(input, Verbosity.TOP);
		if(result != null && result.size() > 0) {
			SearchResult res = new SearchResult();
			SuggestItem item = result.get(0);
			res.result = item.term;
			res.distance = item.distance;
			res.confidence = 1.f - ((float)item.distance)/((float)input.length());
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
		return symSpell.getDictionarySize();
	}
}
