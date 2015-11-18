package edu.buffalo.cse.irf14.analysis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import edu.buffalo.cse.irf14.analysis.TokenType.TYPENAMES;


public class DateFilter extends TokenFilter {
	private class DatePattern {
		private TYPENAMES[] names;
		private SimpleDateFormat sdf;
		private boolean hasEra;
		private boolean isTime;
		private boolean hasYear;
		int numTokens;
		
		private DatePattern(TYPENAMES[] tn, SimpleDateFormat df) {
			names = tn;
			sdf = df;
			numTokens = tn.length;
			
			hasEra = false;
			
			for (TYPENAMES type : tn) {
				if (type == TYPENAMES.ERA) {
					hasEra = true;
				} else if (type == TYPENAMES.TIME || type == TYPENAMES.AMPM) {
					isTime = true;
				} else if (type == TYPENAMES.YEAR) {
					hasYear = true;
				}
			}
		}
		
		private String getString(String dt) throws ParseException {
			boolean endsWithSymbol = (dt.endsWith(",") || dt.endsWith("."));
			boolean needsNegation = hasEra && (dt.indexOf("BC") != -1);
			String suffix = null;
			if (endsWithSymbol) {
				suffix = dt.substring(dt.length() - 1);
			}
			
			dt = dt.replaceAll("[,\\.]","");
			Date d = sdf.parse(dt);
			
			if (!hasYear && !isTime) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(d);
				cal.set(Calendar.YEAR, 1900);
				d = cal.getTime();
			}
			
			dt = (isTime) ? tgtTimeFmt.format(d) : tgtDateFmt.format(d);
			
			if (suffix != null) {
				dt += suffix;
			}
			
			
			return needsNegation ? "-" + dt : dt;
		}
	}
	
	private static HashMap<TYPENAMES, DatePattern[]> patternMap = null;
	
	private final SimpleDateFormat tgtDateFmt = new SimpleDateFormat("yyyyMMdd");
	private final SimpleDateFormat tgtTimeFmt = new SimpleDateFormat("HH:mm:ss");
	
	public DateFilter(TokenStream stream) {
		super(stream);
		
		if (patternMap == null) {
			patternMap = new HashMap<TokenType.TYPENAMES, DateFilter.DatePattern[]>();
			patternMap.put(TYPENAMES.DAY, new DatePattern[] {new DatePattern(new TYPENAMES[] {TYPENAMES.DAY, TYPENAMES.MONTH, TYPENAMES.YEAR},
					new SimpleDateFormat("dd MMMMMMMM yyyy"))});
			patternMap.put(TYPENAMES.MONTH, new DatePattern[] {new DatePattern(new TYPENAMES[]{TYPENAMES.MONTH, TYPENAMES.DAY, TYPENAMES.YEAR},
					new SimpleDateFormat("MMMMMMMM dd yyyy")), new DatePattern(new TYPENAMES[]{TYPENAMES.MONTH, TYPENAMES.DAY },
							new SimpleDateFormat("MMMMMMM dd")), new DatePattern(new TYPENAMES[]{TYPENAMES.MONTH}, new SimpleDateFormat("MMM"))});
			patternMap.put(TYPENAMES.YEAR, new DatePattern[] {new DatePattern(new TYPENAMES[]{TYPENAMES.YEAR, TYPENAMES.ERA},
					new SimpleDateFormat("yyyy GGG")), new DatePattern(new TYPENAMES[]{TYPENAMES.YEAR}, new SimpleDateFormat("yyyy"))});
			patternMap.put(TYPENAMES.TIME, new DatePattern[]{new DatePattern(new TYPENAMES[]{TYPENAMES.TIME, TYPENAMES.AMPM}, 
					new SimpleDateFormat("hh:mm aa")), new DatePattern(new TYPENAMES[]{TYPENAMES.TIME}, new SimpleDateFormat("hh:mmaa"))});
		}
	}

	@Override
	public boolean increment() throws TokenizerException {
		if (stream != null) {
			Token t = getToken();
			
			if (t != null) {
				TYPENAMES tn;
				DatePattern[] dparr;
				String str = "";
				if (t.hasTokenType()) {
					for (Entry<TYPENAMES, DatePattern[]> etr : patternMap.entrySet()) {
						tn = etr.getKey();
						
						if (t.hasTokenType(tn)) {
							dparr = etr.getValue();
							
							for (DatePattern dp : dparr) {
								if (stream.evaluatePattern(dp.names)) {
									
									for (int i = 0; i < dp.numTokens - 1; i++) {
										str += t.toString() + " ";
										stream.remove();
										t = stream.next();
									}
									
									
									str += t.toString();
									try {
										str = dp.getString(str);
										t.setTermText(str);
										t.setIsDate();
									} catch (ParseException e) {
									
									}
									
									break;
									
								}
							}
						}
					}
				}
			}
			
			
			return stream.hasNext();
		}
		return false;
	}

}
