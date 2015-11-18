/**
 * 
 */
package edu.buffalo.cse.irf14.index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.Token;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.analysis.TokenizerException;
import edu.buffalo.cse.irf14.document.Document;
import edu.buffalo.cse.irf14.document.FieldNames;

/**
 * @author nikhillo
 * Class responsible for writing indexes to disk
 */
public class IndexWriter {
	private static AnalyzerFactory factory;
	private static Tokenizer tizer;
	
	private String idxDir;
	private HashMap<String, List<String>> termIdx;
	private HashMap<String, List<String>> authorIdx;
	private HashMap<String, List<String>> categoryIdx;
	private HashMap<String, List<String>> placeIdx;
	private HashSet<Integer> seenDocs;
	private String authorOrg;
	
	/**
	 * Default constructor
	 * @param indexDir : The root directory to be sued for indexing
	 */
	public IndexWriter(String indexDir) {
		
		if (factory == null) {
			factory = AnalyzerFactory.getInstance();
			tizer = new Tokenizer();
		}
		
		termIdx = new HashMap<String, List<String>>(50000);
		authorIdx = new HashMap<String, List<String>>(1000);
		categoryIdx = new HashMap<String, List<String>>(100);
		placeIdx = new HashMap<String, List<String>>(1000);
		idxDir = indexDir;
		seenDocs = new HashSet<Integer>();
	}
	
	
	
	/**
	 * Method to add the given Document to the index
	 * This method should take care of reading the filed values, passing
	 * them through corresponding analyzers and then indexing the results
	 * for each indexable field within the document. 
	 * @param d : The Document to be added
	 * @throws IndexerException : In case any error occurs
	 */
	public void addDocument(Document d) throws IndexerException {
		String[] values;
		TokenStream stream;
		Analyzer analyzer;
		String fileId = d.getField(FieldNames.FILEID)[0];
		int fid = Integer.parseInt(fileId);
		authorOrg = null;
		
		try {
			if (seenDocs.contains(fid)) {
				stream = tizer.consume(d.getField(FieldNames.CATEGORY)[0]);
				addToIndex(stream, FieldNames.CATEGORY, fileId); //only add to category idx
				
			} else {
				for (FieldNames fn : FieldNames.values()) {
					if (fn == FieldNames.FILEID) {
						continue;
					} else if (fn == FieldNames.AUTHOR) {
						values = d.getField(FieldNames.AUTHORORG);
						if (values != null) {
							authorOrg = values[0];
						}
					}
					
					values = d.getField(fn);
					
					if (values != null) {
						for (String v : values) {
							stream = tizer.consume(v);
							analyzer = factory.getAnalyzerForField(fn, stream);
							
							if (analyzer != null) {
								while (analyzer.increment()) {
									
								}			
								stream = analyzer.getStream();
							}
							
							addToIndex(stream, fn, fileId);
						}
					}
				}
				
				seenDocs.add(fid);
			}
		} catch (TokenizerException e) {
			
		}
	}
	
	private void addToIndex(TokenStream stream, FieldNames fn, String fileId) {
		HashMap<String, List<String>> idx = getIndex(fn);
		boolean appendOrg = (fn == FieldNames.AUTHOR && authorOrg != null);
		
		if (idx != null) {
			Map<String, Integer> streamMap = getAsMap(stream);
			String key;
			List<String> postings;
			for (Entry<String, Integer> etr : streamMap.entrySet()) {
				key = etr.getKey();
				
				if (appendOrg)
					key += "|" + authorOrg;
				
				postings = idx.containsKey(key) ? idx.get(key) : new ArrayList<String>();
				postings.add(fileId+"/"+etr.getValue());
				idx.put(key, postings);
			}
		}
		
	}

	private HashMap<String, List<String>> getIndex(FieldNames fn) {
		switch (fn) {
		case AUTHOR:
			return authorIdx;
		case CATEGORY:
			return categoryIdx;
		case PLACE:
			return placeIdx;
		case TITLE:
		case CONTENT:
		case NEWSDATE:
			return termIdx;
			
			default:
				return null;
		}
	}

	private Map<String, Integer> getAsMap(TokenStream stream) {
		Token t;
		String s;
		stream.reset();
		Map<String, Integer> map = new HashMap<String, Integer>(stream.size());
		int val;
		while (stream.hasNext()) {
			t = stream.next();
			
			if (t!= null) {
				s = t.toString();
				val = map.containsKey(s) ? map.get(s) : 0;
				map.put(s, ++val);
			}
		}
		
		return map;
	}



	/**
	 * Method that indicates that all open resources must be closed
	 * and cleaned and that the entire indexing operation has been completed.
	 * @throws IndexerException : In case any error occurs
	 */
	public void close() throws IndexerException {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(idxDir+File.separator+"author.idx"), "UTF-8"));
			passivate(writer, authorIdx);
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(idxDir+File.separator+"place.idx"), "UTF-8"));
			passivate(writer, placeIdx);
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(idxDir+File.separator+"category.idx"), "UTF-8"));
			passivate(writer, categoryIdx);
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(idxDir+File.separator+"term.idx"), "UTF-8"));
			passivate(writer, termIdx);
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(idxDir+File.separator+"idx.props"), "UTF-8"));
			writer.write("N="+seenDocs.size());
			writer.flush();
			writer.close();
			seenDocs.clear();
			seenDocs = null;
		} catch (IOException e) {
			throw new IndexerException();
		}
		
		
	}

	private void passivate(BufferedWriter writer,
			HashMap<String, List<String>> idx) throws IOException {
		StringBuilder bldr = new StringBuilder();
		int ctr = 0;
		List<String> value;
		TreeMap<String, List<String>> map = new TreeMap<String, List<String>>(idx);
		for (Entry<String, List<String>> etr : map.entrySet()) {
			value = etr.getValue();
			bldr.append(etr.getKey()).append("/").append(value.size()).append(":");
			bldr.append(asString(value));
			bldr.append("#");
			ctr++;
			
			if (ctr % 1000 == 0) {
				writer.write(bldr.toString());
				writer.newLine();
				bldr.delete(0, bldr.length());
				writer.flush();
			}
		}
		
		writer.write(bldr.toString());
		bldr.delete(0, bldr.length());
		writer.newLine();
		writer.flush();
		writer.close();
		idx.clear();
		idx = null;
		map.clear();
		map = null;
		
	}

	private String asString(List<String> value) {
		Collections.sort(value);
		return value.toString();
	}
}
