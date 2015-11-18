/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author nicarus
 * 
 */
public class AccentFilter extends TokenFilter {
	private static Map<String, String> accentMap;
	private final TokenStream stream;

	public AccentFilter(TokenStream stream) {
		super(stream);
		this.stream = stream;

		if (accentMap == null) {
			initAccentMap();
		}
	}

	private static void initAccentMap() {
		accentMap = new HashMap<String, String>();
		accentMap.put("\u00C0", "A");
		accentMap.put("\u00C1", "A");
		accentMap.put("\u00C2", "A");
		accentMap.put("\u00C3", "A");
		accentMap.put("\u00C4", "A");
		accentMap.put("\u00C5", "A");
		accentMap.put("\u00C6", "AE");
		accentMap.put("\u00C7", "C");
		accentMap.put("\u00C8", "E");
		accentMap.put("\u00C9", "E");
		accentMap.put("\u00CA", "E");
		accentMap.put("\u00CB", "E");
		accentMap.put("\u00CC", "I");
		accentMap.put("\u00CD", "I");
		accentMap.put("\u00CE", "I");
		accentMap.put("\u00CF", "I");
		accentMap.put("\u0132", "IJ");
		accentMap.put("\u00D0", "D");
		accentMap.put("\u00D1", "N");
		accentMap.put("\u00D2", "O");
		accentMap.put("\u00D3", "O");
		accentMap.put("\u00D4", "O");
		accentMap.put("\u00D5", "O");
		accentMap.put("\u00D6", "O");
		accentMap.put("\u00D8", "O");
		accentMap.put("\u0152", "OE");
		accentMap.put("\u00DE", "TH");
		accentMap.put("\u00D9", "U");
		accentMap.put("\u00DA", "U");
		accentMap.put("\u00DB", "U");
		accentMap.put("\u00DC", "U");
		accentMap.put("\u00DD", "Y");
		accentMap.put("\u0178", "Y");
		accentMap.put("\u00E0", "a");
		accentMap.put("\u00E1", "a");
		accentMap.put("\u00E2", "a");
		accentMap.put("\u00E3", "a");
		accentMap.put("\u00E4", "a");
		accentMap.put("\u00E5", "a");
		accentMap.put("\u00E6", "ae");
		accentMap.put("\u00E7", "c");
		accentMap.put("\u00E8", "e");
		accentMap.put("\u00E9", "e");
		accentMap.put("\u00EA", "e");
		accentMap.put("\u00EB", "e");
		accentMap.put("\u00EC", "i");
		accentMap.put("\u00ED", "i");
		accentMap.put("\u00EE", "i");
		accentMap.put("\u00EF", "i");
		accentMap.put("\u0133", "ij");
		accentMap.put("\u00F0", "d");
		accentMap.put("\u00F1", "n");
		accentMap.put("\u00F2", "o");
		accentMap.put("\u00F3", "o");
		accentMap.put("\u00F4", "o");
		accentMap.put("\u00F5", "o");
		accentMap.put("\u00F6", "o");
		accentMap.put("\u00F8", "o");
		accentMap.put("\u0153", "oe");
		accentMap.put("\u00DF", "ss");
		accentMap.put("\u00FE", "th");
		accentMap.put("\u00F9", "u");
		accentMap.put("\u00FA", "u");
		accentMap.put("\u00FB", "u");
		accentMap.put("\u00FC", "u");
		accentMap.put("\u00FD", "y");
		accentMap.put("\u00FF", "y");
		accentMap.put("\uFB00", "ff");
		accentMap.put("\uFB01", "fi");
		accentMap.put("\uFB02", "fl");
		accentMap.put("\uFB03", "ffi");
		accentMap.put("\uFB04", "ffl");
		accentMap.put("\uFB05", "ft");
		accentMap.put("\uFB06", "st");
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
			
			if (t != null && t.hasNonASCII()) {
				removeAccents(t);
			}

			return stream.hasNext();
		}
		return false;
	}

	private void removeAccents(Token t) {
		String str = t.getTermText();
		boolean changed = false;
		String key;
		for (Entry<String, String> etr : accentMap.entrySet()) {
			key = etr.getKey();

			if (str.indexOf(key) != -1) {
				str = str.replaceAll(key, etr.getValue());
				changed = true;
			}
		}

		if (changed) {
			t.setTermText(str);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.buffalo.cse.irf14.analysis.Analyzer#getStream()
	 */
	@Override
	public TokenStream getStream() {
		return stream;
	}

}
