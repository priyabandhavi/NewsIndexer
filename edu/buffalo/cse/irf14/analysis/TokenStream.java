	/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import edu.buffalo.cse.irf14.analysis.TokenType.TYPENAMES;

/**
 * @author nikhillo
 * Class that represents a stream of Tokens. All {@link Analyzer} and
 * {@link TokenFilter} instances operate on this to implement their
 * behavior
 */
public class TokenStream implements Iterator<Token>{
	private List<Token> tokenList;
	private Iterator<Token> iterator;
	private Token currToken = null;
	private int currIdx;
	private int listSize;
	
	protected TokenStream() {
		this((Token) null);
	}
	
	protected TokenStream(Token...tokens) {
		int len = tokens != null ? tokens.length : 10;
		tokenList = new ArrayList<Token>(len);
		
		if (tokens != null) {
			for (Token t : tokens) {
				tokenList.add(t);
			}
		}
		
		iterator = tokenList.iterator();
		currIdx = -1;
		listSize = tokenList.size();
		tokenList.get(listSize - 1).markSentenceBoundary();
	}
	
	/**
	 * Method that checks if there is any Token left in the stream
	 * with regards to the current pointer.
	 * DOES NOT ADVANCE THE POINTER
	 * @return true if at least one Token exists, false otherwise
	 */
	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	/**
	 * Method to return the next Token in the stream. If a previous
	 * hasNext() call returned true, this method must return a non-null
	 * Token.
	 * If for any reason, it is called at the end of the stream, when all
	 * tokens have already been iterated, return null
	 */
	@Override
	public Token next() {
		try {
			currToken = iterator.next();
			if (currIdx < listSize - 1)
				currIdx ++;
			
		} catch (NoSuchElementException e) {
			currToken = null;
		}
		
		return currToken;
		
	}
	
	/**
	 * Method to remove the current Token from the stream.
	 * Note that "current" token refers to the Token just returned
	 * by the next method. 
	 * Must thus be NO-OP when at the beginning of the stream or at the end
	 */
	@Override
	public void remove() {
		try {
			iterator.remove();
			currToken = null;
			listSize--;
			//currIdx--; //coz next will increment it
		} catch (IllegalStateException e) {
			//do nothing!
		}
		
	}
	
	/**
	 * Method to reset the stream to bring the iterator back to the beginning
	 * of the stream. Unless the stream has no tokens, hasNext() after calling
	 * reset() must always return true.
	 */
	public void reset() {
		iterator = null;
		iterator = tokenList.iterator();
	}
	
	private void resetIterator() {
		reset();
		
		int ctr = -1;
		Token t = null;
		while (iterator.hasNext() && ctr < currIdx) {
			t = iterator.next();
			ctr++;
		}
		
		currToken = t;
	}
	
	/**
	 * Method to append the given TokenStream to the end of the current stream
	 * The append must always occur at the end irrespective of where the iterator
	 * currently stands. After appending, the iterator position must be unchanged
	 * Of course this means if the iterator was at the end of the stream and a 
	 * new stream was appended, the iterator hasn't moved but that is no longer
	 * the end of the stream.
	 * @param stream : The stream to be appended
	 */
	public void append(TokenStream stream) {
		if (stream != null) {
			List<Token> other = stream.tokenList;
			
			if (other != null && !other.isEmpty()) {
				((ArrayList<Token>) tokenList).ensureCapacity(listSize + stream.listSize);
				tokenList.addAll(other);
				resetIterator();
				listSize += stream.listSize;
			}
		}
		
 	}
	
	public Token getCurrent() {
		return currToken;
	}
	
	protected Token getPrevious() {
		return (currIdx >= 1) ? tokenList.get(currIdx - 1) : null;
	}
	
	protected boolean evaluatePattern(TYPENAMES...names) {
		boolean rv = true;
		
		int sz = tokenList.size(), len = names.length;
		Token t;
		if (len - 1 + currIdx >= sz)
			return false;
		
		for (int i = 0; i < len && i + currIdx < sz; i++) {
			t = tokenList.get(i + currIdx);
			
			if (t != null) {
				rv &= t.hasTokenType(names[i]);
				
				if (!rv)
					break;
			}
			
		}
		
		return rv;
	}
	
	protected boolean evaluatePattern(int ftrMask, char regex, int filterMask) {
		//regex patterns: ?  - exactly 1,+ - 1 or more ,* - 0 or more: this is no-op
		int len = (regex == '?') ? 1 : (listSize - currIdx - 1);
		boolean rv = (len >= 1) ? true : false;
		boolean hasFilter = (filterMask > 0);
		
		Token t;

		for (int i = 1; i <= len; i++) {
			t = tokenList.get(currIdx + i);
			if (t.isSentenceBoundary()) {
				break;
			} else {
				if (hasFilter && !t.matchesMask(filterMask)) {
					continue;
				}
				
				if (!t.matchesMask(ftrMask)) {
					rv = false;
					break;
				} 
			}
		}
		
		return rv;
	}

 	
	public int size() {
		return listSize;
	}
	
}
