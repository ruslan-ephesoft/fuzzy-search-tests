package ephesoft.fuzzytest.impl.symspell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymSpell {

	final int defaultMaxEditDistance = 3;
	final int defaultPrefixLength = 7;
	final int defaultCountThreshold = 1;
	final int defaultInitialCapacity = 16;
	final int defaultCompactLevel = 5;

    private int initialCapacity;
    private int maxDictionaryEditDistance;
    private int prefixLength; //prefix length  5..7
    private long countThreshold; //a treshold might be specifid, when a term occurs so frequently in the corpus that it is considered a valid word for spelling correction
    private long compactMask;
    private EditDistance.DistanceAlgorithm distanceAlgorithm = EditDistance.DistanceAlgorithm.DAMERAU;
    private int maxLength; //maximum dictionary term length
    private boolean caseInsensitive = true;
    private boolean processingLineAsWord = false;

	private Map<Integer, String[]> deletes;
    private Map<String, Long> words;
    private Map<String, Long> belowThresholdWords = new HashMap<>();
    
    public boolean isCaseInsensitive() {
		return caseInsensitive;
	}

	public void setCaseInsensitive(boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
	}

	public int getMaxDictionaryEditDistance() { 
    	return this.maxDictionaryEditDistance; 
    }

    public int getPrefixLength() { 
    	return this.prefixLength; 
    }

    public boolean isProcessingLineAsWord() {
		return processingLineAsWord;
	}

	public void setProcessingLineAsWord(boolean proseccLineAsWord) {
		this.processingLineAsWord = proseccLineAsWord;
	}

	public int getMaxLength() { 
    	return this.maxLength; 
    }

    public long getCountThreshold() { 
    	return this.countThreshold;  
    }

    public int getWordCount() { 
    	return this.words.size(); 
    }

    public int getEntryCount() {
    	return this.deletes.size(); 
    }

    public SymSpell() {
    	this.initialCapacity = this.defaultInitialCapacity;
        this.words = new HashMap<>(this.initialCapacity);
        this.maxDictionaryEditDistance = this.defaultMaxEditDistance;
        this.prefixLength = this.defaultPrefixLength;
        this.countThreshold = this.defaultCountThreshold;
        this.compactMask = (Long.MAX_VALUE >> (3 + defaultCompactLevel)) << 2;
    }
    
    public SymSpell(int initialCapacity, int maxDictionaryEditDistance, int prefixLength, int countThreshold, int compactLevel) {
        if (initialCapacity < 0) throw new IndexOutOfBoundsException(String.valueOf(initialCapacity));
        if (maxDictionaryEditDistance < 0) throw new IndexOutOfBoundsException(String.valueOf(maxDictionaryEditDistance));
        if (prefixLength < 1 || prefixLength <= maxDictionaryEditDistance) throw new IndexOutOfBoundsException(String.valueOf(prefixLength));
        if (countThreshold < 0) throw new IndexOutOfBoundsException(String.valueOf(countThreshold));
        if (compactLevel > 16) throw new IndexOutOfBoundsException(String.valueOf(compactLevel));

        this.initialCapacity = initialCapacity;
        this.words = new HashMap<>(initialCapacity);
        this.maxDictionaryEditDistance = maxDictionaryEditDistance;
        this.prefixLength = prefixLength;
        this.countThreshold = countThreshold;
        if (compactLevel > 16) compactLevel = 16;
        this.compactMask = (Long.MAX_VALUE >>> (3 + compactLevel)) << 2;
    }
    
    public boolean createDictionaryEntry(String key, long count) {
    	return createDictionaryEntry(key, count, null);
    }

    public boolean createDictionaryEntry(String keyOrig, long count, SuggestionStage staging) {
        if (count <= 0) {
            if (this.countThreshold > 0) {
            	return false; // no point doing anything if count is zero, as it can't change anything
            }
            count = 0;
        }
        
        String key = this.caseInsensitive ? keyOrig.toLowerCase() : keyOrig;
        boolean hasThreshold = belowThresholdWords.containsKey(key);
        long countPrevious = hasThreshold ? belowThresholdWords.get(key) : -1L;

        // look first in below threshold words, update count, and allow promotion to correct spelling word if count reaches threshold
        // threshold must be >1 for there to be the possibility of low threshold words
        if (countThreshold > 1 && hasThreshold)
        {
            // calculate new count for below threshold word
            count = (Long.MAX_VALUE - countPrevious > count) ? countPrevious + count : Long.MAX_VALUE;
            // has reached threshold - remove from below threshold collection (it will be added to correct words below)
            if (count >= countThreshold)
            {
                belowThresholdWords.remove(key);
            }
            else
            {
                belowThresholdWords.put(key, count);
                return false;
            }
        }
        else if (hasThreshold)
        {
            // just update count if it's an already added above threshold word
            count = (Long.MAX_VALUE - countPrevious > count) ? countPrevious + count : Long.MAX_VALUE;
            words.put(key, count);
            return false;
        }
        else if (count < countThreshold)
        {
            // new or existing below threshold word
            belowThresholdWords.put(key, count);
            return false;
        }

        // what we have at this point is a new, above threshold word 
        words.put(key, count);

        //edits/suggestions are created only once, no matter how often word occurs
        //edits/suggestions are created only as soon as the word occurs in the corpus, 
        //even if the same term existed before in the dictionary as an edit from another word
        if (key.length() > maxLength) maxLength = key.length();

        //create deletes
        Set<String> edits = editsPrefix(key);
        // if not staging suggestions, put directly into main data structure
        if (staging != null)
        {
            for (String delete: edits) {
            	staging.add(getStringHash(delete), key);
            }
        }
        else
        {
            if (deletes == null) {
            	this.deletes = new HashMap<>(initialCapacity); 
            }
            for (String delete: edits) {
				int deleteHash = getStringHash(delete);
                String[] suggestions = deletes.get(deleteHash);
                if (deletes.containsKey(deleteHash))
                {
                    String[] newSuggestions = new String[suggestions.length + 1];
                    System.arraycopy(suggestions, 0, newSuggestions, 0, suggestions.length);
                    suggestions = newSuggestions;
                    deletes.put(deleteHash, suggestions);
                }
                else
                {
                    suggestions = new String[1];
                    deletes.put(deleteHash, suggestions);
                }
                suggestions[suggestions.length - 1] = key;
            }
        }
        return true;
    }

    /// <summary>Load multiple dictionary entries from a file of word/frequency count pairs</summary>
    /// <remarks>Merges with any dictionary data already loaded.</remarks>
    /// <param name="corpus">The path+filename of the file.</param>
    /// <param name="termIndex">The column position of the word.</param>
    /// <param name="countIndex">The column position of the frequency count.</param>
    /// <returns>True if file loaded, or false if file not found.</returns>
    public boolean loadDictionary(String corpus, int termIndex, int countIndex) throws IOException {
    	File file = new File(corpus);
    	if (!file.exists()) {
    		return false;
    	}
    	SuggestionStage staging = new SuggestionStage(16384);
    	BufferedReader br = new BufferedReader(new FileReader(file));
    	String line;
    	//process a single line at a time only for memory efficiency
    	while ((line = br.readLine()) != null)
    	{
    		if(caseInsensitive) {
    			line = line.toLowerCase();
    		}
    		String[] lineParts = line.split("\\s+");
    		if (lineParts.length >= 2)
    		{
    			String key = lineParts[termIndex];
    			try {
    				long count = Long.parseLong(lineParts[countIndex]);
    				createDictionaryEntry(key, count, staging);
    			} catch(Exception exx) {}
    		}
    	}
    	br.close();
    	if (this.deletes == null) {
    		this.deletes = new HashMap<>(staging.deleteCount());
    	}
    	commitStaged(staging);
    	return true;
    }

    //create a frequency dictionary from a corpus (merges with any dictionary data already loaded) 
    /// <summary>Load multiple dictionary words from a file containing plain text.</summary>
    /// <param name="corpus">The path+filename of the file.</param>
    /// <returns>True if file loaded, or false if file not found.</returns>
    public boolean createDictionary(String corpus) throws IOException {
    	File file = new File(corpus);
    	if (!file.exists()) {
    		return false;
    	}
    	SuggestionStage staging = new SuggestionStage(16384);
    	BufferedReader br = new BufferedReader(new FileReader(file));
    	String line;
    	//process a single line at a time only for memory efficiency
    	while ((line = br.readLine()) != null)
    	{
    		if(caseInsensitive) {
    			line = line.toLowerCase();
    		}
    		for(String key: parseWords(line)) {
    			createDictionaryEntry(key, 1, staging);
    		}
    	}
    	br.close();
    	if (this.deletes == null) {
    		this.deletes = new HashMap<>(staging.deleteCount());
    	}
    	commitStaged(staging);
    	return true;
    }

    /// <summary>Remove all below threshold words from the dictionary.</summary>
    /// <remarks>This can be used to reduce memory consumption after populating the dictionary from
    /// a corpus using CreateDictionary.</remarks>
    public void purgeBelowThresholdWords()
    {
        belowThresholdWords = new HashMap<>();
    }

    /// <summary>Commit staged dictionary additions.</summary>
    /// <remarks>Used when you write your own process to load multiple words into the
    /// dictionary, and as part of that process, you first created a SuggestionsStage 
    /// object, and passed that to CreateDictionaryEntry calls.</remarks>
    /// <param name="staging">The SuggestionStage object storing the staged data.</param>
    public void commitStaged(SuggestionStage staging)
    {
        staging.commitTo(deletes);
    }

    /// <summary>Find suggested spellings for a given input word, using the maximum
    /// edit distance specified during construction of the SymSpell dictionary.</summary>
    /// <param name="input">The word being spell checked.</param>
    /// <param name="verbosity">The value controlling the quantity/closeness of the retuned suggestions.</param>
    /// <returns>A List of SuggestItem object representing suggested correct spellings for the input word, 
    /// sorted by edit distance, and secondarily by count frequency.</returns>
    public List<SuggestItem> lookup(String input, Verbosity verbosity)
    {
        return lookup(input, verbosity, this.maxDictionaryEditDistance);
    }

    /// <summary>Find suggested spellings for a given input word.</summary>
    /// <param name="input">The word being spell checked.</param>
    /// <param name="verbosity">The value controlling the quantity/closeness of the retuned suggestions.</param>
    /// <param name="maxEditDistance">The maximum edit distance between input and suggested words.</param>
    /// <returns>A List of SuggestItem object representing suggested correct spellings for the input word, 
    /// sorted by edit distance, and secondarily by count frequency.</returns>
    public List<SuggestItem> lookup(String inputOrig, Verbosity verbosity, int maxEditDistance)
    {
        //verbosity=Top: the suggestion with the highest term frequency of the suggestions of smallest edit distance found
        //verbosity=Closest: all suggestions of smallest edit distance found, the suggestions are ordered by term frequency 
        //verbosity=All: all suggestions <= maxEditDistance, the suggestions are ordered by edit distance, then by term frequency (slower, no early termination)

        // maxEditDistance used in Lookup can't be bigger than the maxDictionaryEditDistance
        // used to construct the underlying dictionary structure.
        if (maxEditDistance > maxDictionaryEditDistance) {
        	throw new IndexOutOfBoundsException(String.valueOf(maxEditDistance));
        }

        String input = this.caseInsensitive ? inputOrig.toLowerCase() : inputOrig;
        List<SuggestItem> suggestions = new ArrayList<>();
        int inputLen = input.length();
        // early exit - word is too big to possibly match any words
        if (inputLen - maxEditDistance > maxLength) {
        	return suggestions;
        }

        // deletes we've considered already
        Set<String> hashset1 = new HashSet<>();
        // suggestions we've considered already
        Set<String> hashset2 = new HashSet<>();

        // quick look for exact match
        boolean hasInput = words.containsKey(input);
        long suggestionCount = hasInput ? words.get(input) : 0;
        if (hasInput) {
            suggestions.add(new SuggestItem(input, 0, suggestionCount));
            // early exit - return exact match, unless caller wants all matches
            if (verbosity != Verbosity.ALL) return suggestions;
        }
        hashset2.add(input); // we considered the input already in the word.TryGetValue above

        int maxEditDistance2 = maxEditDistance;
        int candidatePointer = 0;
        List<String> candidates = new ArrayList<>();

        //add original prefix
        int inputPrefixLen = inputLen;
        if (inputPrefixLen > prefixLength) {
            inputPrefixLen = prefixLength;
            candidates.add(input.substring(0, inputPrefixLen));
        } else {
            candidates.add(input);
        }
        EditDistance distanceComparer = new EditDistance(input, this.distanceAlgorithm);
        while (candidatePointer < candidates.size()) {
            String candidate = candidates.get(candidatePointer++);
            int candidateLen = candidate.length();
            int lengthDiff = inputPrefixLen - candidateLen;

            //save some time - early termination
            //if canddate distance is already higher than suggestion distance, than there are no better suggestions to be expected
            if (lengthDiff > maxEditDistance2) {
                // skip to next candidate if Verbosity.All, look no further if Verbosity.Top or Closest 
                // (candidates are ordered by delete distance, so none are closer than current)
                if (verbosity == Verbosity.ALL) {
                	continue;
                }
                break;
            }

            //read candidate entry from dictionary
            int candidateHash = getStringHash(candidate);
            boolean hasCandidateHash = deletes.containsKey(candidateHash);
            if (hasCandidateHash)
            {
                String[] dictSuggestions = deletes.get(candidateHash);
                //iterate through suggestions (to other correct dictionary items) of delete item and add them to suggestion list
                for (int i = 0; i < dictSuggestions.length; i++)
                {
                    String suggestion = dictSuggestions[i];
                    int suggestionLen = suggestion.length();
                    if (suggestion.equals(input)) {
                    	continue;
                    }
                    if ((Math.abs(suggestionLen - inputLen) > maxEditDistance2) // input and sugg lengths diff > allowed/current best distance
                        || (suggestionLen < candidateLen) // sugg must be for a different delete string, in same bin only because of hash collision
                        || (suggestionLen == candidateLen && !suggestion.equals(candidate))) // if sugg len = delete len, then it either equals delete or is in same bin only because of hash collision
                    {    
                    	continue;
                    }
                    int suggPrefixLen = Math.min(suggestionLen, prefixLength);
                    if (suggPrefixLen > inputPrefixLen && (suggPrefixLen - candidateLen) > maxEditDistance2) {
                    	continue;
                    }

                    //True Damerau-Levenshtein Edit Distance: adjust distance, if both distances>0
                    //We allow simultaneous edits (deletes) of maxEditDistance on on both the dictionary and the input term. 
                    //For replaces and adjacent transposes the resulting edit distance stays <= maxEditDistance.
                    //For inserts and deletes the resulting edit distance might exceed maxEditDistance.
                    //To prevent suggestions of a higher edit distance, we need to calculate the resulting edit distance, if there are simultaneous edits on both sides.
                    //Example: (bank==bnak and bank==bink, but bank!=kanb and bank!=xban and bank!=baxn for maxEditDistance=1)
                    //Two deletes on each side of a pair makes them all equal, but the first two pairs have edit distance=1, the others edit distance=2.
                    int distance = 0;
                    int min = 0;
                    if (candidateLen == 0)
                    {
                    	//suggestions which have no common chars with input (inputLen<=maxEditDistance && suggestionLen<=maxEditDistance)
                    	distance = Math.max(inputLen, suggestionLen);
                    	if (distance > maxEditDistance2 || !hashset2.add(suggestion)) continue;
                    }
                    else if (suggestionLen == 1)
                    {
                    	if (input.indexOf(suggestion.charAt(0)) < 0) {
                    		distance = inputLen; 
                    	} else {
                    		distance = inputLen - 1;
                    	}
                    	if (distance > maxEditDistance2 || !hashset2.add(suggestion)) {
                    		continue;
                    	}
                    } else if (((prefixLength - maxEditDistance) == candidateLen)
                    		&& (((min = Math.min(inputLen, suggestionLen) - prefixLength) > 1)
                    				&& (!input.substring(inputLen + 1 - min).equals(suggestion.substring(suggestionLen + 1 - min))))
                    		|| ((min > 0) && (input.charAt(inputLen - min) != suggestion.charAt(suggestionLen - min))
                    				&& ((input.charAt(inputLen - min - 1) != suggestion.charAt(suggestionLen - min))
                    						|| (input.charAt(inputLen - min) != suggestion.charAt(suggestionLen - min - 1)))))
                    {
                    	continue;
                    }
                    else
                    {
                    	// DeleteInSuggestionPrefix is somewhat expensive, and only pays off when verbosity is Top or Closest.
                    	if ((verbosity != Verbosity.ALL && !deleteInSuggestionPrefix(candidate, candidateLen, suggestion, suggestionLen))
                    			|| !hashset2.add(suggestion)) continue;
                    	distance = distanceComparer.compare(suggestion, maxEditDistance2);
                    	if (distance < 0) continue;
                    }

                    //save some time
                    //do not process higher distances than those already found, if verbosity<All (note: maxEditDistance2 will always equal maxEditDistance when Verbosity.All)
                    if (distance <= maxEditDistance2)
                    {
                        suggestionCount = words.get(suggestion);
                        SuggestItem si = new SuggestItem(suggestion, distance, suggestionCount);
                        if (suggestions.size() > 0)
                        {
                            switch (verbosity)
                            {
                                case CLOSEST:
                                    {
                                        //we will calculate DamLev distance only to the smallest found distance so far
                                        if (distance < maxEditDistance2) {
                                        	suggestions.clear();
                                        }
                                        break;
                                    }
                                case TOP:
                                    {
                                        if (distance < maxEditDistance2 || suggestionCount > suggestions.get(0).count)
                                        {
                                            maxEditDistance2 = distance;
                                            suggestions.set(0, si);
                                        }
                                        continue;
                                    }
                            }
                        }
                        if (verbosity != Verbosity.ALL) {
                        	maxEditDistance2 = distance;
                        }
                        suggestions.add(si);
                    }
                }//end foreach
            }//end if         

            //add edits 
            //derive edits (deletes) from candidate (input) and add them to candidates list
            //this is a recursive process until the maximum edit distance has been reached
            if ((lengthDiff < maxEditDistance) && (candidateLen <= prefixLength))
            {
                //save some time
                //do not create edits with edit distance smaller than suggestions already found
                if (verbosity != Verbosity.ALL && lengthDiff >= maxEditDistance2) {
                	continue;
                }

                for (int i = 0; i < candidateLen; i++)
                {
                    String delete = candidate.substring(0, i) + candidate.substring(i + 1);

                    if (hashset1.add(delete)) { 
                    	candidates.add(delete); 
                    }
                }
            }
        }//end while

        //sort by ascending edit distance, then by descending word frequency
        if (suggestions.size() > 1) suggestions.sort((SuggestItem o1, SuggestItem o2) -> o1.compareTo(o2));
        return suggestions;
    }//end if         

    //check whether all delete chars are present in the suggestion prefix in correct order, otherwise this is just a hash collision
    private boolean deleteInSuggestionPrefix(String delete, int deleteLen, String suggestion, int suggestionLen)
    {
        if (deleteLen == 0) return true;
        if (prefixLength < suggestionLen) suggestionLen = prefixLength;
        int j = 0;
        for (int i = 0; i < deleteLen; i++) {
            char delChar = delete.charAt(i);
            while (j < suggestionLen && delChar != suggestion.charAt(j)) {
            	j++;
            }
            if (j == suggestionLen) return false;
        }
        return true;
    }

    //create a non-unique wordlist from sample text
    //language independent (e.g. works with Chinese characters)
    private String[] parseWords(String text)
    {
        if(this.isProcessingLineAsWord()) {
        	String[] ret = new String[1];
        	ret[0] = text;
        	return ret;
        }
    	// \w Alphanumeric characters (including non-latin characters, umlaut characters and digits) plus "_" 
        // \d Digits
        // Compatible with non-latin characters, does not split words at apostrophes
    	Pattern p = Pattern.compile("['’\\w&&[^_]]+");
    	Matcher m = p.matcher(text);
    	String[] matches = new String[0];
    	if(m.find()) {
    		int g = m.groupCount();
    		matches = new String[g];
    		for(int i = 0; i < g; i++) {
    			matches[i] = m.group(i + 1);
    		}
    	}
        return matches;
    }

    //inexpensive and language independent: only deletes, no transposes + replaces + inserts
    //replaces and inserts are expensive and language dependent (Chinese has 70,000 Unicode Han characters)
    private Set<String> edits(String word, int editDistance, Set<String> deleteWords)
    {
        editDistance++;
        if (word.length() > 1)
        {
            for (int i = 0; i < word.length(); i++)
            {
                String delete = word.substring(0, i) + word.substring(i + 1);
                if (deleteWords.add(delete))
                {
                    //recursion, if maximum edit distance not yet reached
                    if (editDistance < maxDictionaryEditDistance) edits(delete, editDistance, deleteWords);
                }
            }
        }
        return deleteWords;
    }

    private Set<String> editsPrefix(String key)
    {
        Set<String> hashSet = new HashSet<>();
        if (key.length() <= maxDictionaryEditDistance) hashSet.add("");
        if (key.length() > prefixLength) key = key.substring(0, prefixLength);
        hashSet.add(key);
        return edits(key, 0, hashSet);
    }

    private int getStringHash(String s)
    {

        int len = s.length();
        int lenMask = len;
        if (lenMask > 3) lenMask = 3;

        long hash = 2166136261L;
        for (int i = 0; i < len; i++)
        {
        	hash ^= s.charAt(i);
        	hash *= 16777619;
        }

        hash &= this.compactMask;
        hash |= lenMask;
        return (int)hash;
    }

	public long getDictionarySize() {
		long res = 0L;
		for(String key: words.keySet()) {
			res += (long)key.getBytes().length + 4;
		}
		for(String key: this.belowThresholdWords.keySet()) {
			res += (long)key.getBytes().length + 4;
		}
		for(Integer key: deletes.keySet()) {
			res += 4L;
			String[] vals = deletes.get(key);
			for(String val: vals) {
				res += (long)val.getBytes().length;
			}
		}
		return res;
	}
}
