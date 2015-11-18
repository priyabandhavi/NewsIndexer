/**
 * 
 */
package edu.buffalo.cse.irf14.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author nikhillo
 * Class that emulates reading data back from a written index
 */
public class IndexReader {
	private HashMap<String, List<String>> idx;
	private Map<String, Integer> dfCnts;
	private Map<String, Integer> totCnts;
	private int N;
	
	/**
	 * Default constructor
	 * @param indexDir : The root directory from which the index is to be read.
	 * This will be exactly the same directory as passed on IndexWriter. In case 
	 * you make subdirectories etc., you will have to handle it accordingly.
	 * @param type The {@link IndexType} to read from
	 */
	public IndexReader(String indexDir, IndexType type) {
		String filename = null;
		switch (type) {
		case AUTHOR:
			filename = "author.idx";
			break;
		case CATEGORY:
			filename = "category.idx";
			break;
		case PLACE:
			filename = "place.idx";
			break;
		case TERM:
			filename = "term.idx";
			break;
		}
		
		try {
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(indexDir+File.separator+filename), "UTF-8"));
			loadIndex(rdr);
			rdr =  new BufferedReader(new InputStreamReader(new FileInputStream(indexDir+File.separator+"idx.props"), "UTF-8"));
			String line = rdr.readLine();
			rdr.close();
			line = line.substring(line.indexOf('=')+1);
			N = Integer.parseInt(line);
			
		} catch (IOException e) {
			
		} 
	}
	
	private void loadIndex(BufferedReader rdr) throws IOException {
		String line, key;
		String[] splits, pair;
		idx = new HashMap<String, List<String>>();
		dfCnts = new HashMap<String, Integer>();
		totCnts = new HashMap<String, Integer>();
		int cnt;
		List<String> tmp;
		
		while ((line = rdr.readLine()) != null) {
			splits = line.split("#");
			for (String row : splits) {
				pair = row.split(":");
				key = pair[0];
				line = pair[1];
				pair = key.split("/");
				key = pair[0];
				cnt = Integer.parseInt(pair[1]);
				tmp = retrievePostings(line);
				dfCnts.put(key, cnt);
				totCnts.put(key, getSum(tmp));
				idx.put(key, tmp);
			}
		}
		
		rdr.close();
		sortByCount();
	}

	private int getSum(List<String> tmp) {
		int sum = 0;
		String[] splits;
		for (String s : tmp) {
			splits = s.split("/");
			sum += Integer.parseInt(splits[1]);
		}
		
		return sum;
	}

	private void sortByCount() {
		TreeMap<String, Integer> tmap = new TreeMap<String, Integer>(new VComp());
		tmap.putAll(totCnts);
		totCnts = tmap;
	}

	private List<String> retrievePostings(String line) {
		line = line.substring(1, line.length() - 1);
		String[] splits = line.split(", ");
		List<String> rlist = new ArrayList<String>(splits.length);
		for (String s : splits) {
			rlist.add(s);
		}
		
		return rlist;
	}

	/**
	 * Get total number of terms from the "key" dictionary associated with this 
	 * index. A postings list is always created against the "key" dictionary
	 * @return The total number of terms
	 */
	public int getTotalKeyTerms() {
		return idx.size();
	}
	
	/**
	 * Get total number of terms from the "value" dictionary associated with this 
	 * index. A postings list is always created with the "value" dictionary
	 * @return The total number of terms
	 */
	public int getTotalValueTerms() {
		return N;
	}
	
	/**
	 * Method to get the postings for a given term. You can assume that
	 * the raw string that is used to query would be passed through the same
	 * Analyzer as the original field would have been.
	 * @param term : The "analyzed" term to get postings for
	 * @return A Map containing the corresponding fileid as the key and the 
	 * number of occurrences as values if the given term was found, null otherwise.
	 */
	public Map<String, Integer> getPostings(String term) {
		if (idx.containsKey(term)) {
			List<String> postings = idx.get(term);
			Map<String, Integer> map = new HashMap<String, Integer>(postings.size());
			String fileId;
			int tf;
			String[] splits;
			
			for (String s : postings) {
				splits = s.split("/");
				fileId = splits[0];
				tf = Integer.parseInt(splits[1]);
				map.put(fileId, tf);
			}
			
			return map;
			
		}
		return null;
	}
	
	/**
	 * Method to get the top k terms from the index in terms of the total number
	 * of occurrences.
	 * @param k : The number of terms to fetch
	 * @return : An ordered list of results. Must be <=k fr valid k values
	 * null for invalid k values
	 */
	public List<String> getTopK(int k) {
		if (k > 0 && k < totCnts.size()) {
			List<String> rlist = new ArrayList<String>(k);
			int ctr = 0;
			
			for(String term : totCnts.keySet()) {
				rlist.add(term);
				ctr++;
				
				if (ctr == k)
					break;
			}
			
			return rlist;
		}
		return null;
	}
	
	/**
	 * Method to implement a simple boolean AND query on the given index
	 * @param terms The ordered set of terms to AND, similar to getPostings()
	 * the terms would be passed through the necessary Analyzer.
	 * @return A Map (if all terms are found) containing FileId as the key 
	 * and number of occurrences as the value, the number of occurrences 
	 * would be the sum of occurrences for each participating term. return null
	 * if the given term list returns no results
	 * BONUS ONLY
	 */
	public Map<String, Integer> query(String...terms) {
		Map<String, Integer> map = new HashMap<String, Integer>(), postings;
		String key;
		int value1, value2;
		for (String s : terms) {
			postings = getPostings(s);
			if (postings == null)
				return null;
			else if (map.isEmpty()) {
				map.putAll(postings);
			} else {
				map.keySet().retainAll(postings.keySet());
				
				if (map.isEmpty()) {
					return null;
				} else {
					for (Entry<String, Integer> etr : postings.entrySet()) {
						key = etr.getKey();
						if (map.containsKey(key)) {
							value1 = etr.getValue();
							value2 = map.get(key);
							map.put(key, value1 + value2);
						}
					}
				}	
			}
		}
		
		return map;
	}
	
	private class VComp implements Comparator<String> {
		
		@Override
		public int compare(String o1, String o2) {
			Integer i1 = totCnts.get(o1), i2 = totCnts.get(o2);
			if (i1 != null && i2 != null) {
				int v = i2.compareTo(i1);
				return (v == 0) ? o1.compareTo(o2) : v;
			} else if (i1 != null) {
				return -1;
			} else if (i2 != null) {
				return 1;
			} else
				return 0;
		}
		
	}
}
