/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.HashMap;

import edu.buffalo.cse.irf14.document.FieldNames;

/**
 * @author nikhillo
 * This factory class is responsible for instantiating "chained" {@link Analyzer} instances
 */
public class AnalyzerFactory {
	private static HashMap<FieldNames, TokenFilterType[]> filterMap;
	private static AnalyzerFactory factory;
	private static TokenFilterFactory tffFact;
	/**
	 * Static method to return an instance of the factory class.
	 * Usually factory classes are defined as singletons, i.e. 
	 * only one instance of the class exists at any instance.
	 * This is usually achieved by defining a private static instance
	 * that is initialized by the "private" constructor.
	 * On the method being called, you return the static instance.
	 * This allows you to reuse expensive objects that you may create
	 * during instantiation
	 * @return An instance of the factory
	 */
	public static AnalyzerFactory getInstance() {
		if (factory == null) {
			factory = new AnalyzerFactory();
			tffFact = TokenFilterFactory.getInstance();
			initFilterMap();
		}
		
		return factory;
	}
	
	private static void initFilterMap() {
		filterMap = new HashMap<FieldNames, TokenFilterType[]>();
		filterMap.put(FieldNames.AUTHOR, new TokenFilterType[] {TokenFilterType.ACCENT, TokenFilterType.CAPITALIZATION});
		filterMap.put(FieldNames.CONTENT,  new TokenFilterType[] {TokenFilterType.SYMBOL, TokenFilterType.SPECIALCHARS, TokenFilterType.ACCENT, 
				TokenFilterType.STOPWORD, TokenFilterType.DATE, TokenFilterType.NUMERIC, TokenFilterType.STEMMER, TokenFilterType.CAPITALIZATION});
		filterMap.put(FieldNames.NEWSDATE, new TokenFilterType[]{TokenFilterType.SYMBOL, TokenFilterType.SPECIALCHARS, TokenFilterType.DATE});
		filterMap.put(FieldNames.PLACE, new TokenFilterType[] {TokenFilterType.SYMBOL, TokenFilterType.SPECIALCHARS,
				TokenFilterType.ACCENT, TokenFilterType.STOPWORD, TokenFilterType.CAPITALIZATION});
		filterMap.put(FieldNames.TITLE, new TokenFilterType[] {TokenFilterType.SYMBOL, TokenFilterType.SPECIALCHARS, TokenFilterType.ACCENT, 
				TokenFilterType.STOPWORD, TokenFilterType.DATE, TokenFilterType.NUMERIC, TokenFilterType.STEMMER, TokenFilterType.CAPITALIZATION});
		
	}

	/**
	 * Returns a fully constructed and chained {@link Analyzer} instance
	 * for a given {@link FieldNames} field
	 * Note again that the singleton factory instance allows you to reuse
	 * {@link TokenFilter} instances if need be
	 * @param name: The {@link FieldNames} for which the {@link Analyzer}
	 * is requested
	 * @param TokenStream : Stream for which the Analyzer is requested
	 * @return The built {@link Analyzer} instance for an indexable {@link FieldNames}
	 * null otherwise
	 */
	public Analyzer getAnalyzerForField(FieldNames name, TokenStream stream) {
		TokenFilterType[] types = filterMap.get(name);
		
		if (types != null) {
			TokenFilter[] arr = new TokenFilter[types.length];
			int len = arr.length;
			
			for (int i = 0; i < len; i++) {
				arr[i] = tffFact.getFilterByType(types[i], stream);
				arr[i].setAnalyzerMode();
			}
			
			return new AnalyzerImpl(stream, arr);
		}
		return null;
	}
}
