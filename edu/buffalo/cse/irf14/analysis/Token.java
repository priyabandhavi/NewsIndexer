/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import edu.buffalo.cse.irf14.analysis.TokenType.TYPENAMES;

/**
 * @author nikhillo
 * This class represents the smallest indexable unit of text.
 * At the very least it is backed by a string representation that
 * can be interchangeably used with the backing char array.
 * 
 * You are encouraged to add more data structures and fields as you deem fit. 
 */
public class Token {
	private static Set<TokenType> set = new HashSet<TokenType>();
	
	//The backing string representation -- can contain extraneous information
	private String termText;
	private String normText;
	//The char array backing termText
	private char[] termBuffer;
	/*
	 * We support following boolean features - 8 bits
	 * hasAlpha - A -       10000000      
	 * hasAllCaps - C -     01000000
	 * hasNumber - N -      00100000      
	 * hasPunct (.?!) - P - 00010000
	 * hasApos (') - O -    00001000 
	 * hasSymbol - S -      00000100
	 * hasNonASCII - E -    00000010
	 * Reserve - R -        00000001
	 * 	ACNPOSER
	 */
	private int ftrMap = 0;
	
	//enum ordinals to the rescue :)
	private int tokenTypeMap = 0;
	
	protected Token(String txt) {
		if (set.isEmpty()) 
			initTokenTypes();
		
		setTermText(txt);
		
		TYPENAMES tn;
		int idx, mask;
		for (TokenType tt : set) {
			tn = tt.eval(this);
			
			if (tn != null) {
				idx = tn.ordinal();
				mask = 1;
				mask = (mask << idx);
				tokenTypeMap |= mask;
			}
		}
	}
	
	/**
	 * Method to set the termText to given text.
	 * This is a sample implementation and you CAN change this
	 * to suit your class definition and data structure needs.
	 * @param text
	 */
	protected void setTermText(String text) {
		termText = text;
		
		if (termText != null && !termText.isEmpty()) {
			termBuffer = termText.toCharArray();
			normText = termText.toLowerCase(Locale.ENGLISH);
			analyzeText();
		} else {
			termBuffer = null;
			normText = "";
		}
	}
	
	private void analyzeText() {
		boolean allCaps = true;
		int idx = 0, len = termBuffer.length;
		for (char c : termBuffer) {
			if ((int) c >= 32 && (int)c <= 126)  { //ascii
				if (Character.isLetterOrDigit(c)) {
					if (Character.isLetter(c)) {
						ftrMap |= 128;
						
						if (!Character.isUpperCase(c))
							allCaps = false;
					} else { //is digit!
						allCaps = false;
						ftrMap |= 32;
					}
				} else { //is punct, apos or symbol
					allCaps = false;
					if (c == '\'') {
						ftrMap |= 8;
					} else if (c == '?' || c == '!' || c == '.') {
						ftrMap |= 16;
						
						if (idx == len - 1) {
							markSentenceBoundary();
						}
					} else {
						ftrMap |= 4;
					}
				}
			} else {
				ftrMap |= 2;
			}
			
			idx++;
		}
		
		if (allCaps)
			ftrMap |= 64;
	}

	public boolean hasAlpha() {
		return matchesMask(128);
	}
	
	public boolean hasAllCaps() {
		return matchesMask(64);
	}

	public boolean hasNumber() {
		return matchesMask(32);
	}
	
	public boolean hasPunct() {
		return matchesMask(16);
	}
	
	public boolean hasApos() {
		return matchesMask(8);
	}
	
	public boolean hasSymbol() {
		return matchesMask(4);
	}
	
	public boolean hasNonASCII() {
		return matchesMask(2);
	}
	
	public boolean isSentenceBoundary() {
		return matchesMask(1);
	}
	
	protected boolean matchesMask(int mask) {
		return matches(ftrMap, mask);
	}
	
	private boolean matches(int map, int mask) {
		return ((map & mask) == mask);
	}
	
	/**
	 * Getter for termText
	 * This is a sample implementation and you CAN change this
	 * to suit your class definition and data structure needs.
	 * @return the underlying termText
	 */
	protected String getTermText() {
		return termText;
	}
	
	/**
	 * Method to set the termBuffer to the given buffer.
	 * This is a sample implementation and you CAN change this
	 * to suit your class definition and data structure needs.
	 * @param buffer: The buffer to be set
	 */
	protected void setTermBuffer(char[] buffer) {
		String str = new String(buffer);
		setTermText(str);
	}
	
	/**
	 * Getter for the field termBuffer
	 * @return The termBuffer
	 */
	protected char[] getTermBuffer() {
		return termBuffer;
	}
	
	/**
	 * Method to merge this token with the given array of Tokens
	 * You are free to update termText and termBuffer as you please
	 * based upon your Token implementation. But the toString() method
	 * below must return whitespace separated value for all tokens combined
	 * Also the token order must be maintained.
	 * @param tokens The token array to be merged
	 */
	protected void merge(Token...tokens) {
		if (tokens != null) {
			StringBuilder txt = new StringBuilder(termText);
			for (Token t : tokens) {
				txt.append(" ").append(t.toString());
				mergeFlags(t);
			}
			
			setTermText(txt.toString());
		}
	}
	
	private void mergeFlags(Token t) {
		//A(lpha) : OR 
		//C(aps) : AND
		//N(umber) : OR
		//P(unct) : OR
		//(Ap)O(s) : OR
		//S(ymbol) : OR
		//(nonASC-e)E : OR
		
		int other = t.ftrMap;
		int val = ftrMap | other;
		
		if (hasAllCaps() && t.hasAllCaps())
			val |= 64;
		
		ftrMap = val;
		
	}

	/**
	 * Returns the string representation of this token. It must adhere to the
	 * following rules:
	 * 1. Return only the associated "text" with the token. Any other information 
	 * must be suppressed.
	 * 2. Must return a non-empty value only for tokens that are going to be indexed
	 * If you introduce special token types (sentence boundary for example), return
	 * an empty string
	 * 3. IF the original token A (with string as "a") was merged with tokens B ("b"),
	 * C ("c") and D ("d"), toString should return "a b c d"
	 * @return The raw string representation of the token
	 */
	@Override
	public String toString() {
		return termText;
	}
	
	public String getNormalizedText() {
		return normText;
	}

	public void markSentenceBoundary() {
		ftrMap |= 1;
		int idx = TYPENAMES.SENTBND.ordinal();
		int mask = 1;
		mask = (mask << idx);
		tokenTypeMap |= mask;
	}

	public boolean isDate() {
		return matches(tokenTypeMap, 1);
	}
	
	protected boolean hasTokenType(TYPENAMES name) {
		int val = ( 1 << name.ordinal());
		return matches(tokenTypeMap, val);
	}
	
	protected boolean hasTokenType() {
		return tokenTypeMap > 1;
	}
	
	public static void addTokenType(TokenType... types) {
		for (TokenType tt : types) {
			set.add(tt);
		}
									}
									
	public static void initTokenTypes() {
		addTokenType(new TokenType(TYPENAMES.DAY, "([1-9]|[1-2][0-9]|30|31)[,]?", 32),
				new TokenType(TYPENAMES.MONTH, "(January|February|March|April|May|June|July|August|September|October|November|December)[,]?", 128),
				new TokenType(TYPENAMES.MONTH, "Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Oct|Nov|Dec", 128),
				new TokenType(TYPENAMES.YEAR, "([1-2][0-9]{3})[,]?", 32),
				new TokenType(TYPENAMES.YEAR, "[1-9][0-9]{1,3}", 32),
				new TokenType(TYPENAMES.YEAR, "[1-9][0-9]{1,3}(AD|BC|CE|BCE)[,\\.]?", 160),
				new TokenType(TYPENAMES.TIME, "[0-9]{1,2}:[0-9]{2}", 36),
				new TokenType(TYPENAMES.TIME, "[0-9]{2}:[0-9]{2}:[0-9]{2}", 36),
				new TokenType(TYPENAMES.TIME, "[0-9]{1,2}:[0-9]{2}(AM|PM)[,\\.]?", 164),
				new TokenType(TYPENAMES.AMPM, "(am|pm)[,\\.]?", 128),
				new TokenType(TYPENAMES.AMPM, "(AM|PM)[,\\.]?", 192),
				new TokenType(TYPENAMES.TIMEZONE, "[A-Z]{3}", 192),
				new TokenType(TYPENAMES.ERA, "AD|BC|CE|BCE", 192),
				new TokenType(TYPENAMES.INITCAP, "[A-Z].*", 128));
	}

	protected void setIsDate() {
		tokenTypeMap |= 1;
		
	}
}
