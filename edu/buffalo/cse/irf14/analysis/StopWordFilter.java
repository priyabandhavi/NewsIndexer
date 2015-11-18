/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author nikhillo
 *
 */
public class StopWordFilter extends TokenFilter {
	private static final Set<String> wordSet = new HashSet<String>(Arrays.asList(new String[] {"a", "an", "and", "are", "as", "at", "be", "but",
		"by", "do", "for", "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that",
		"the", "their", "then", "there", "these", "they", "this", "to", "was", "will", "with"}));
	
	public StopWordFilter(TokenStream stream) {
		super(stream);
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.irf14.analysis.Analyzer#increment()
	 */
	@Override
	public boolean increment() throws TokenizerException {
		if (stream != null) {
			Token t = getToken();
			
			if (t != null && wordSet.contains(t.getNormalizedText())) {
				stream.remove();
			}
			
			return stream.hasNext();
		}
		
		return false;

	}

}
