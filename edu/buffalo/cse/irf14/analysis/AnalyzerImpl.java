/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author nicarus
 *
 */
public class AnalyzerImpl implements Analyzer {
	private final TokenStream stream;
	private List<TokenFilter> list;
	protected AnalyzerImpl(TokenStream str, TokenFilter...filters) {
		stream = str;
		list = new ArrayList<TokenFilter>(Arrays.asList(filters));
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.irf14.analysis.Analyzer#increment()
	 */
	@Override
	public boolean increment() throws TokenizerException {
		if (stream.hasNext()) {
			stream.next(); //we have a token
			
			for (TokenFilter tf : list) {
				tf.increment();
			}
		}
		return stream.hasNext();
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.irf14.analysis.Analyzer#getStream()
	 */
	@Override
	public TokenStream getStream() {
		return stream;
	}

}
