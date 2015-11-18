package edu.buffalo.cse.irf14.analysis;

import java.util.Arrays;

public class NumericFilter extends TokenFilter {
	public NumericFilter(TokenStream stream) {
		super(stream);
	}

	@Override
	public boolean increment() throws TokenizerException {
		if (stream != null) {
			Token t = getToken();
			
			if (t != null && t.hasNumber() && !t.isDate()) {
				removeNumbers(t);
			}
			
			return stream.hasNext();
		}
		return false;
	}

	private void removeNumbers(Token t) {
		char[] tbuff = t.getTermBuffer();
		int len = tbuff.length, idx = 0;
		char[] res = new char[len];
		
		for (char c : tbuff) {
			if (Character.isDigit(c) || c == ',' || c == '.') {
				continue;
			} else {
				res[idx++] = c;
			}
		}
		
		if (idx >= 1) {
			res = Arrays.copyOfRange(res, 0, idx);
			t.setTermBuffer(res);
		} else {
			stream.remove();
		}
	}
}
