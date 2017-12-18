package ephesoft.fuzzytest;

import java.io.File;

import ephesoft.fuzzytest.util.Util;

public class FuzzySearchTestsMain {

	public static void main(String[] args) {
/*		String[] tests = {"wasington", "miscisippi", "bevledere", "goosfellow", "combinede"};
		String filePath = "C:\\eclipse-workspace\\FuzzySearch\\resources\\partialUSCity.txt";
*/
/*		String[] tests = {"detajucntion", "sprifield", "fenchtoun", "newbecford", "melwill"};
		String filePath = "C:\\eclipse-workspace\\FuzzySearch\\resources\\usCity.txt";
*/
/*		String[] tests = { "abtenarco", "kasercole", "lisanda", "vanatwerpen", "zergusen"};
		String filePath = "C:\\eclipse-workspace\\FuzzySearch\\resources\\name.txt";
*/

/*		String[] tests = {"inkresingly", "depndablity", "exibitionists", "cocnreteness", "acoutrment"};
		String filePath = "C:\\eclipse-workspace\\FuzzySearch\\resources\\dictionary_en_82_765.txt";
*/
		String[] tests = {"govemnent", "trihloraketic", "forefaser's", "unaculturted", "citoarchitechtonics"};
		String filePath = "C:\\eclipse-workspace\\FuzzySearch\\resources\\dictionary_en_500_000.txt";
		
		AlgorithmProcessor lcsProc = AlgorithmFactory.getAlgorithmProcessor(Util.FS_LONGEST_COMMON_SUBSEQUENCE);
		System.out.println("LONGEST_COMMON_SUBSEQUENCE");
		try {
			System.out.println("Creating dictionary...");
			long t = System.currentTimeMillis();
			lcsProc.generateDictionary(new File(filePath));
			System.out.println("Dictionary created in (milliseconds):" + (System.currentTimeMillis() - t));
			System.out.println("Dictionary size=" + lcsProc.getDictionarySize());
			SearchResult[] results = new SearchResult[tests.length];
			t = System.currentTimeMillis();
			for(int i = 0; i < tests.length; i++) {
				results[i] = lcsProc.search(tests[i]);
			}
			System.out.println("Searching took (milliseconds):" + (System.currentTimeMillis() - t));
			for(int i = 0; i < tests.length; i++) {
				System.out.println("INPUT: "+tests[i]+"; OUPUT:"+results[i].result+"; CONFIDENCE:"+results[i].confidence+"; DISTANCE:"+results[i].distance);
			}
		} catch(Exception exx) {
			exx.printStackTrace();
		}

		AlgorithmProcessor bkProc = AlgorithmFactory.getAlgorithmProcessor(Util.FS_BK_TREE);
		System.out.println("BK_TREE");
		try {
			System.out.println("Creating dictionary...");
			long t = System.currentTimeMillis();
			bkProc.generateDictionary(new File(filePath));
			System.out.println("Dictionary created in (milliseconds):" + (System.currentTimeMillis() - t));
			System.out.println("Dictionary size=" + lcsProc.getDictionarySize());
			SearchResult[] results = new SearchResult[tests.length];
			t = System.currentTimeMillis();
			for(int i = 0; i < tests.length; i++) {
				results[i] = bkProc.search(tests[i]);
			}
			System.out.println("Searching took (milliseconds):" + (System.currentTimeMillis() - t));
			for(int i = 0; i < tests.length; i++) {
				System.out.println("INPUT: "+tests[i]+"; OUPUT:"+results[i].result+"; CONFIDENCE:"+results[i].confidence+"; DISTANCE:"+results[i].distance);
			}
		} catch(Exception exx) { 
			exx.printStackTrace();
		}

		AlgorithmProcessor symSpellProc = AlgorithmFactory.getAlgorithmProcessor(Util.FS_SYMSPELL);
		System.out.println("SYMSPELL");
		try {
			System.out.println("Creating dictionary...");
			long t = System.currentTimeMillis();
			symSpellProc.generateDictionary(new File(filePath));
			System.out.println("Dictionary created in (milliseconds):" + (System.currentTimeMillis() - t));
			System.out.println("Dictionary size=" + symSpellProc.getDictionarySize());
			SearchResult[] results = new SearchResult[tests.length];
			t = System.currentTimeMillis();
			for(int i = 0; i < tests.length; i++) {
				results[i] = symSpellProc.search(tests[i]);
			}
			System.out.println("Searching took (milliseconds):" + (System.currentTimeMillis() - t));
			for(int i = 0; i < tests.length; i++) {
				System.out.println("INPUT: "+tests[i]+"; OUPUT:"+results[i].result+"; CONFIDENCE:"+results[i].confidence+"; DISTANCE:"+results[i].distance);
			}
		} catch(Exception exx) {
			exx.printStackTrace();
		}
	}
}
