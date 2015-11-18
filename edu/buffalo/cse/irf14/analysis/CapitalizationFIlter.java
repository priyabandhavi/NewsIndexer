/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import edu.buffalo.cse.irf14.analysis.TokenType.TYPENAMES;

/**
 * @author nicarus
 * 
 */
public class CapitalizationFIlter extends TokenFilter {
	TYPENAMES[] pattMerge = new TYPENAMES[] { TYPENAMES.INITCAP,
			TYPENAMES.INITCAP };
	TYPENAMES[] initLower = new TYPENAMES[] { TYPENAMES.SENTBND,
			TYPENAMES.INITCAP };
	TYPENAMES[] firstWord = new TYPENAMES[] { TYPENAMES.INITCAP };
	boolean foundAllCaps = false;

	/**
	 * @param stream
	 */
	public CapitalizationFIlter(TokenStream stream) {
		super(stream);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.buffalo.cse.irf14.analysis.Analyzer#increment()
	 */
	@Override
	public boolean increment() throws TokenizerException {
		if (stream != null) {
			Token t = getToken();

			if (t != null) {
				//if (t.hasAlpha()) {
					if (t.hasAllCaps()) {
						if (stream.evaluatePattern(64, '+', 128)
								|| foundAllCaps) {
							t.setTermText(t.getNormalizedText());
							if (!foundAllCaps)
								foundAllCaps = true;
							else if (t.isSentenceBoundary())
								foundAllCaps = false;
						}
					}

					if (t.hasTokenType() && !foundAllCaps) {
						if (stream.evaluatePattern(pattMerge)) {
							String str = "";
							for (int i = 0; i < 1; i++) {
								str += t.toString() + " ";
								stream.remove();
								t = stream.next();
							}

							str += t.toString();
							t.setTermText(str);
						} else if (stream.evaluatePattern(initLower)) {
							t = stream.next();
							t.setTermText(t.getNormalizedText());
						} else if (stream.evaluatePattern(firstWord)
								&& stream.getPrevious() == null) {
							t.setTermText(t.getNormalizedText());
						}
					}

				//}
			}

			return stream.hasNext();
		}
		return false;
	}

}
