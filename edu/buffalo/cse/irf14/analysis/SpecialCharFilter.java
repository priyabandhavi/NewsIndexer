package edu.buffalo.cse.irf14.analysis;

import java.util.Arrays;

public class SpecialCharFilter extends TokenFilter {
	public SpecialCharFilter(TokenStream stream) {
		super(stream);
	}

	@Override
	public boolean increment() throws TokenizerException {
		if (stream != null) {
			Token t = getToken();
			
			if (t != null && t.hasSymbol()) {
				processSymbols(t);
			}
			return stream.hasNext();
		}
		return false;
	}

	private void processSymbols(Token t) {
		char[] buff = t.getTermBuffer(), nbuff = new char[buff.length];
		int idx = 0;
		boolean keepHyphens = t.hasNumber();
		
		for (char c : buff) {
			if ((int) c >= 32 && (int)c <= 126) { //ascii only
				if (Character.isLetterOrDigit(c)) {
					nbuff[idx++] = c;
				} else {
					switch (c) {
					case '.':
					case '?':
					case '!':
					case '\'':
						nbuff[idx++] = c;
						break;
					case '-':
						if (keepHyphens)
							nbuff[idx++] = c;
					}
				}
			} else {
				nbuff[idx++] = c;
			}
		}
		
		
		t.setTermBuffer(Arrays.copyOfRange(nbuff, 0, idx));
		
	}

}
