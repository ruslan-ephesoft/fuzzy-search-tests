package ephesoft.fuzzytest.impl.symspell.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import ephesoft.fuzzytest.impl.symspell.*;
import ephesoft.fuzzytest.impl.symspell.EditDistance.DistanceAlgorithm;

public class SymSpellTests {
    
	public static void main(String[] args) {
		SymSpellTests test = new SymSpellTests();

		//test.wordsWithSharedPrefixShouldRetainCounts();
		//test.addAdditionalCountsShouldNotAddWordAgain();
		//test.addAdditionalCountsShouldIncreaseCount();
		//test.addAdditionalCountsShouldNotOverflow();
		
		test.verbosityShouldControlLookupResults();
		//test.lookupShouldReturnMostFrequent();
		
		//test.lookupShouldFindExactMatch();
		//test.lookupShouldNotReturnNonWordDelete();
		//test.lookupShouldNotReturnLowCountWord();
		//test.lookupShouldNotReturnLowCountWordThatsAlsoDeleteWord();
		//test.LookupShouldReplicateNoisyResults();

		
		
/*		EditDistance d = new EditDistance("steem", EditDistance.DistanceAlgorithm.DAMERAU);
		int i = d.damerauLevenshteinDistance("steems", 4);
		System.out.println("i="+i);
*/	}
	
	public void wordsWithSharedPrefixShouldRetainCounts() {
        System.out.println("wordsWithSharedPrefixShouldRetainCounts");
    	SymSpell symSpell = new SymSpell(16, 1, 3, 1, 5);
        symSpell.createDictionaryEntry("pipe", 5);
        symSpell.createDictionaryEntry("pips", 10);
        List<SuggestItem> result = symSpell.lookup("pipe", Verbosity.ALL, 1);
        System.out.println("Lookup pipe");
        System.out.println("result.size()="+result.size());
        for(int i = 0; i < result.size(); i++) {
        	System.out.println("resut[" + i + "].term=" + result.get(i).term);
        	System.out.println("resut[" + i + "].count=" + result.get(i).count);
        }
        result = symSpell.lookup("pips", Verbosity.ALL, 1);
        System.out.println("Lookup pips");
        System.out.println("result.size()="+result.size());
        for(int i = 0; i < result.size(); i++) {
        	System.out.println("resut[" + i + "].term=" + result.get(i).term);
        	System.out.println("resut[" + i + "].count=" + result.get(i).count);
        }
        result = symSpell.lookup("pip", Verbosity.ALL, 1);
        System.out.println("Lookup pip");
        System.out.println("result.size()="+result.size());
        for(int i = 0; i < result.size(); i++) {
        	System.out.println("resut[" + i + "].term=" + result.get(i).term);
        	System.out.println("resut[" + i + "].count=" + result.get(i).count);
        }
    }

    public void addAdditionalCountsShouldNotAddWordAgain()
    {
    	SymSpell symSpell = new SymSpell();
        String word = "hello";
        symSpell.createDictionaryEntry(word, 11);
        //Assert.AreEqual(1, symSpell.WordCount);
        System.out.println("symSpell.WordCount="+symSpell.getWordCount());
        symSpell.createDictionaryEntry(word, 3);
        //Assert.AreEqual(1, symSpell.WordCount);
        System.out.println("symSpell.WordCount="+symSpell.getWordCount());
    }
    
    public void addAdditionalCountsShouldIncreaseCount()
    {
    	SymSpell symSpell = new SymSpell();
        String word = "hello";
        symSpell.createDictionaryEntry(word, 11);
        List<SuggestItem> result = symSpell.lookup(word, Verbosity.TOP);
        long count = 0;
        if (result.size() == 1) count = result.get(0).count;
        //Assert.AreEqual(11, count);
        System.out.println("count="+count);
        symSpell.createDictionaryEntry(word, 3);
        result = symSpell.lookup(word, Verbosity.TOP);
        count = 0;
        if (result.size() == 1) count = result.get(0).count;
        //Assert.AreEqual(11 + 3, count);
        System.out.println("count="+count);
    }
    
    public void addAdditionalCountsShouldNotOverflow()
    {
    	SymSpell symSpell = new SymSpell();
        String word = "hello";
        symSpell.createDictionaryEntry(word, Long.MAX_VALUE - 10);
        List<SuggestItem> result = symSpell.lookup(word, Verbosity.TOP);
        long count = 0;
        if (result.size() == 1) count = result.get(0).count;
        //Assert.AreEqual(long.MaxValue - 10, count);
        System.out.println("Long.MAX_VALUE - 10="+(Long.MAX_VALUE - 10)+"; count="+count);
        symSpell.createDictionaryEntry(word, Long.MAX_VALUE);
        result = symSpell.lookup(word, Verbosity.TOP);
        count = 0;
        if (result.size() == 1) count = result.get(0).count;
        //Assert.AreEqual(long.MaxValue, count);
        System.out.println("Long.MAX_VALUE="+Long.MAX_VALUE+"; count="+count);
    }
    
    public void verbosityShouldControlLookupResults()
    {
    	SymSpell symSpell = new SymSpell();
        symSpell.createDictionaryEntry("steam", 1);
        symSpell.createDictionaryEntry("steams", 2);
        symSpell.createDictionaryEntry("steem", 3);
        List<SuggestItem> result = symSpell.lookup("steems", Verbosity.TOP, 2);
        //Assert.AreEqual(1, result.size());
        System.out.println("result.size()="+result.size());
        result = symSpell.lookup("steems", Verbosity.CLOSEST, 2);
        //Assert.AreEqual(2, result.size());
        System.out.println("result.size()="+result.size());
        result = symSpell.lookup("steems", Verbosity.ALL, 2);
        //Assert.AreEqual(3, result.size());
        System.out.println("result.size()="+result.size());
    }
    
    public void lookupShouldReturnMostFrequent()
    {
    	SymSpell symSpell = new SymSpell();
        symSpell.createDictionaryEntry("steama", 4);
        symSpell.createDictionaryEntry("steamb", 6);
        symSpell.createDictionaryEntry("steamc", 2);
        List<SuggestItem> result = symSpell.lookup("steam", Verbosity.TOP, 2);
        //Assert.AreEqual(1, result.Count);
        System.out.println("result.size()="+result.size());
        //Assert.AreEqual("steamb", result[0].term);
        System.out.println("result.get(0).term="+result.get(0).term);
        //Assert.AreEqual(6, result[0].count);
        System.out.println("result.get(0).count="+result.get(0).count);    }
    

    public void lookupShouldFindExactMatch()
    {
    	SymSpell symSpell = new SymSpell();
        symSpell.createDictionaryEntry("steama", 4);
        symSpell.createDictionaryEntry("steamb", 6);
        symSpell.createDictionaryEntry("steamc", 2);
        List<SuggestItem> result = symSpell.lookup("steama", Verbosity.TOP, 2);
        //Assert.AreEqual(1, result.Count);
        System.out.println("result.size()="+result.size());
        //Assert.AreEqual("steama", result[0].term);
        System.out.println("result.get(0).term="+result.get(0).term);
    }
    
    public void lookupShouldNotReturnNonWordDelete()
    {
    	SymSpell symSpell = new SymSpell(16, 2, 7, 10, 5);
        symSpell.createDictionaryEntry("pawn", 10);
        List<SuggestItem> result = symSpell.lookup("paw", Verbosity.TOP, 0);
        //Assert.AreEqual(0, result.Count);
        System.out.println("result.size()="+result.size());
        result = symSpell.lookup("awn", Verbosity.TOP, 0);
        //Assert.AreEqual(0, result.Count);
        System.out.println("result.size()="+result.size());
    }
    
    public void lookupShouldNotReturnLowCountWord()
    {
    	SymSpell symSpell = new SymSpell(16, 2, 7, 10, 5);
        symSpell.createDictionaryEntry("pawn", 1);
        List<SuggestItem> result = symSpell.lookup("pawn", Verbosity.TOP, 0);
        //Assert.AreEqual(0, result.Count);
        System.out.println("result.size()="+result.size());
    }
    
    public void lookupShouldNotReturnLowCountWordThatsAlsoDeleteWord()
    {
    	SymSpell symSpell = new SymSpell(16, 2, 7, 10, 5);
        symSpell.createDictionaryEntry("flame", 20);
        symSpell.createDictionaryEntry("flam", 1);
        List<SuggestItem> result = symSpell.lookup("flam", Verbosity.TOP, 0);
        //Assert.AreEqual(0, result.Count);
        System.out.println("result.size()="+result.size());
    }
    
    public void LookupShouldReplicateNoisyResults()
    {
        final int editDistanceMax = 2;
        final int prefixLength = 7;
        final Verbosity verbosity = Verbosity.CLOSEST;
        SymSpell symSpell = new SymSpell(83000, editDistanceMax, prefixLength, 1, 5);
        String path = "C:\\eclipse-workspace\\FuzzySearch\\resources\\frequency_dictionary_en_82_765.txt";    //for spelling correction (genuine English words)


        //load 1000 terms with random spelling errors
        int resultSum = 0;
        try {
            symSpell.loadDictionary(path, 0, 1);
        	BufferedReader br = new BufferedReader(new FileReader(new File(path)));
            String line;
            while ((line = br.readLine()) != null)
            {
                String[] lineParts = line.split("\\s+");
                if (lineParts.length >= 2)
                {
                    resultSum += symSpell.lookup(lineParts[0], verbosity, symSpell.getMaxDictionaryEditDistance() ).size();
                }
            }
            br.close();
        	
        }catch(Exception exx) {
        	exx.printStackTrace();
        }

        //Assert.AreEqual( 4945 , resultSum);
        System.out.println("resultSum="+resultSum);
    }
}
