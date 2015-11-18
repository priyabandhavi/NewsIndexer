/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.Arrays;

/**
 * @author nicarus
 *
 */
public class SymbolFilter extends TokenFilter {
	public SymbolFilter(TokenStream stream) {
		super(stream);
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.irf14.analysis.Analyzer#increment()
	 */
	@Override
	public boolean increment() throws TokenizerException {
		if (stream != null) {
			Token t = getToken();
			
			if (t != null) {
				if (t.hasPunct()) {
					boolean retained = handlePunctuation(t);
					if (!retained)
						return stream.hasNext();
				}
				
				if (t.hasApos()) {
					handleApostrophe(t);
				}
				
				if (t.hasSymbol()) {
					handleHyphen(t);
				}
			}
		
			return stream.hasNext();
		}
		return false;
	}

	private void handleHyphen(Token t) {
		String txt = t.getTermText();
		
		while (txt.indexOf('-') != -1) {
			//has hyphen
			if (!t.hasNumber()) {//has a number?
				//so this needs to be replaced
				txt = txt.replaceAll("-", " ");
				txt = txt.replaceAll("[\\s]{2,}", " ");
			} else {
				break; //do nothing!
			}
		}
		
		txt = txt.trim();
		if (txt.isEmpty()) {
			stream.remove();
		} else {
			t.setTermText(txt);
		}
		
		
		
	}

	private void handleApostrophe(Token t) {
		String txt = t.getTermText();
		
		int pos = txt.lastIndexOf('\''), len = txt.length();
		
		while (pos != -1) {
			if (pos == len - 1) {
				txt = txt.substring(0, pos); //s'
			} else {
				String suffix = txt.substring(pos + 1);
				String replacement, prefix;
				if ("s".equals(suffix)) {
					replacement = "";
				}else if ("t".equals(suffix)) {
					pos--;
					replacement = " not"; //there's also can't => can not
				} else if ("ve".equals(suffix)) {
					replacement = " have";
				} else if ("ll".equals(suffix)) {
					replacement = " will";
				} else if ("m".equals(suffix)) {
					replacement = " am";
				} else if ("am".equals(suffix)) {
					replacement = "dam";
				} else if ("clock".equals(suffix)) {
					replacement = "f the clock";
				} else if ("re".equals(suffix)) {
					replacement = " are";
				} else if("all".equals(suffix)) {
					replacement = "ou all";
				} else if ("d".equals(suffix)) {
					replacement = " would"; //TODO:Check forum response
				} else if ("em".equals(suffix)) {
					replacement = "them";
				} else {
					replacement = suffix;
				}
				
				prefix = txt.substring(0, pos);
				prefix = handlePrefixes(prefix);
				txt = prefix + replacement;
			}
			
			pos = txt.lastIndexOf('\'');
			len = txt.length();
		}
		
		
		t.setTermText(txt);
		
	}

	private String handlePrefixes(String prefix) {
		if ("wo".equals(prefix)) {
			return "will";
		} else if ("sha".equals(prefix)) {
			return "shall";
		}
		else
			return prefix;
	}

	private boolean handlePunctuation(Token t) {
		char[] tbuff = t.getTermBuffer();
		int len = tbuff.length;
		
		if (len > 1) {
			char cEnd = tbuff[len - 1];
			boolean changed = false;
			
			while (cEnd == '?' || cEnd == '!' || cEnd == '.') {
				tbuff = Arrays.copyOfRange(tbuff, 0, len - 1);
				len = tbuff.length;
				changed = true;
				if (len > 1)
					cEnd = tbuff[len - 1];
				else
					break;
				
			}
			
			if (changed) {
				t.setTermBuffer(tbuff); 
				t.markSentenceBoundary();
			}
		} else if (len == 1) {
			stream.remove();
			return false;
		}
		
		return true;
	}

}
