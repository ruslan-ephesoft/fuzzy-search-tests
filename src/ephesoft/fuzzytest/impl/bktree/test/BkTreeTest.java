package ephesoft.fuzzytest.impl.bktree.test;

import java.util.Set;

import ephesoft.fuzzytest.impl.bktree.*;

public class BkTreeTest {
	
	public static void main(String[] args) {
		BkTreeTest test = new BkTreeTest();
		//test.testString();
		test.testStringDL();
	}
	
	public void testString() {
		Metric<String> hammingDistance = new Metric<String>() {
		    @Override
		    public int distance(String x, String y) {

		        if (x.length() != y.length())
		            throw new IllegalArgumentException();

		        int distance = 0;

		        for (int i = 0; i < x.length(); i++)
		            if (x.charAt(i) != y.charAt(i))
		                distance++;

		        return distance;
		    }
		};

		MutableBkTree<String> bkTree = new MutableBkTree<>(hammingDistance);
		bkTree.addAll("berets", "carrot", "egrets", "marmot", "packet", "pilots", "purist");

		BkTreeSearcher<String> searcher = new BkTreeSearcher<>(bkTree);

		Set<BkTreeSearcher.Match<? extends String>> matches = searcher.search("parrot", 2);

		for (BkTreeSearcher.Match<? extends String> match : matches)
		    System.out.println(String.format(
		        "%s (distance %d)",
		        match.getMatch(),
		        match.getDistance()
		    ));
		
	}

	public void testStringDL() {
		MutableBkTree<CharSequence> bkTree = new MutableBkTree<>(new DamerauLevenshteinDistance());
		bkTree.addAll("berets", "carrot", "egrets", "marmot", "packet", "pilots", "purist");

		BkTreeSearcher<CharSequence> searcher = new BkTreeSearcher<>(bkTree);

		Set<BkTreeSearcher.Match<? extends CharSequence>> matches = searcher.search("parrot", 2);

		for (BkTreeSearcher.Match<? extends CharSequence> match : matches)
		    System.out.println(String.format(
		        "%s (distance %d)",
		        match.getMatch(),
		        match.getDistance()
		    ));
		
	}

}
