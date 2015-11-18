/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

/**
 * The abstract class that you must extend when implementing your 
 * TokenFilter rule implementations.
 * Apart from the inherited Analyzer methods, we would use the 
 * inherited constructor (as defined here) to test your code.
 * @author nikhillo
 *
 */
public abstract class TokenFilter implements Analyzer {
	protected final TokenStream stream;
	protected boolean isAnalyzer;
	/**
	 * Default constructor, creates an instance over the given
	 * TokenStream
	 * @param stream : The given TokenStream instance
	 */
	public TokenFilter(TokenStream stream) {
		//TODO : YOU MUST IMPLEMENT THIS METHOD
		this.stream = stream;
	}
	
	public TokenStream getStream() {
		return stream;
	}
	
	protected void setAnalyzerMode() {
		isAnalyzer = true;
	}
	
	protected Token getToken() {
		if (stream != null) {
			Token t = (isAnalyzer) ? stream.getCurrent() : stream.next();
			
			//if (t == null && stream.hasNext()) {
				//t = stream.next();
			//}
			
			//System.out.println("CT: " + ((t!= null) ? t.toString() : "NULL") + " - " + this.getClass().getName());
			return t;
		}
		
		return null;
	}
}
