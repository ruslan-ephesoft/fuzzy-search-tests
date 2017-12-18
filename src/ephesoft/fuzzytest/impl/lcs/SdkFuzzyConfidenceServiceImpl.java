package ephesoft.fuzzytest.impl.lcs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import ephesoft.fuzzytest.util.Util;

/**
 * 
 * Default Implementation of the Fuzzy Confidence. It is based on the Longest common Subsequence Algorithm to find the fuzziness.
 * 
 * @author Ephesoft
 * @version 1.0.
 *
 */
public class SdkFuzzyConfidenceServiceImpl {
	
	public static final float FUZZY_CONFIDENCE_MAX= 1.0f;

	private static final long serialVersionUID = 1L;
	
	private List<String> dictionarySet;

	public void loadDictionary(File file) throws Exception {
		dictionarySet = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while((line = br.readLine()) != null) {
			dictionarySet.add(line.trim().toLowerCase());
		}
		br.close();
	}

	public void loadDictionary(String filePath) throws Exception {
		loadDictionary(new File(filePath));
	}
	
	/**
	 * Using Longest Common Subsequence algorithm to find the fuzziness of two {@link String} to judge how much they match with each
	 * other.
	 * 
	 * Fuzzy match <code> float <code>will be the quotient of count of total common characters and the length of the largest String
	 * 
	 * @param firstString {@link String} first string out of the two which needs to be matched.
	 * 
	 * @param secondString {@link String} second string out of the two which needs to be matched.
	 */
	public float getFuzzyConfidence(final String firstString, final String secondString) {
		float totalFuzzyConfidence = 0;
		if (!Util.isBlank(firstString) && !Util.isBlank(secondString)) {
			final int editDistance = editDistance(firstString.toLowerCase(), secondString.toLowerCase(), firstString.length(),
					secondString.length());
			final int maxLength = Math.max(firstString.length(), secondString.length());
			totalFuzzyConfidence = (editDistance * 1.0f) / maxLength;
		}
		return totalFuzzyConfidence;
	}

	private static int editDistance(final String firstString, final String secondString, final int m, final int n) {
		final int[][] arr = new int[m + 1][n + 1];
		for (int i = m - 1; i >= 0; i--) {
			for (int j = n - 1; j >= 0; j--) {
				if (firstString.charAt(i) == secondString.charAt(j)) {
					arr[i][j] = arr[i + 1][j + 1] + 1;
				} else {
					arr[i][j] = Math.max(arr[i + 1][j], arr[i][j + 1]);
				}
			}
		}
		return arr[0][0];
	}
	
	public Match match(final String stringToMatch) {
		Match res = new Match();
		res.fuzzyMatch = 0.0f;
		if (dictionarySet != null && dictionarySet.size() > 0) {
			// Iterating over dictionary set of tokens.
			for (final String token : dictionarySet) {
				final float matchPercent = getFuzzyConfidence(token, stringToMatch);
				if(matchPercent > res.fuzzyMatch) {
					res.fuzzyMatch = matchPercent;
					res.term = token;
				}

				// Break if matching confidence is equal to 1.0
				if (Float.compare(res.fuzzyMatch, FUZZY_CONFIDENCE_MAX) == 0) {
					break;
				}
			}
		}
		return res;
	}
	
	public long getDictionarySize() {
		long res = 0L;
		for(String item: dictionarySet) {
			res += (long)item.getBytes().length;
		}
		return res;
	}
}
