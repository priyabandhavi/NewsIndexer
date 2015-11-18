/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.regex.Pattern;

/**
 * Adds some sort of a type support to a token
 * The idea is we cant seem to be able to get away from regex type 
 * matches on strings. We are also looking at muti token or across
 * token matches.
 * So we define tokentype - this class needs the following to work
 * 		- a name
 * 		- a bitmap mask to match (blind regexes are expensive)
 * 		- a regex to execute
 * So once matched, we can annotate a token with type(s).
 * The stream can then be taught to match token type patterns
 * @author nikhillo
 *
 */
public class TokenType {
	private final TYPENAMES type;
	private final int bitMask;
	private final Pattern pattern;
	
	public static enum TYPENAMES {DATE, //pseudo - ordinal 0 always
		DAY, MONTH, YEAR, TIME, TIMEZONE, ERA, AMPM, //for dates
		INITCAP, SENTBND};
	
	
	public TokenType(TYPENAMES nm, String regex, int mask) {
		type = nm;
		bitMask = mask;
		pattern = Pattern.compile(regex);
	}
	
	protected TYPENAMES eval(Token t) {
		return (t.matchesMask(bitMask) && pattern.matcher(t.getTermText()).matches()) ?
				type : null;
	}
	
	@Override
	public int hashCode() {
		return ((type.ordinal() + 1) * bitMask * pattern.hashCode());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof TokenType) {
			TokenType other = (TokenType) o;
			return (other.type == this.type && other.bitMask == this.bitMask && 
					other.pattern.equals(this.pattern));
		}
		
		return false;
	}
	
}
