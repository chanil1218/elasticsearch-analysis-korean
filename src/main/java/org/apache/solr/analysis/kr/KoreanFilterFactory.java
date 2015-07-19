package org.apache.solr.analysis.kr;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.kr.KoreanFilter;
import org.apache.lucene.analysis.util.TokenFilterFactory;

public class KoreanFilterFactory extends TokenFilterFactory {

	private boolean bigrammable = true;

	private boolean hasOrigin = true;

	private boolean hasCNoun = true;

	private boolean exactMatch = false;

	public KoreanFilterFactory(Map<String, String> args) {
		super(new HashMap<String, String>());
	}

	public void init(Map<String, String> args) {
		bigrammable = getBoolean(args, "bigrammable", true);
		hasOrigin = getBoolean(args, "hasOrigin", true);
		exactMatch = getBoolean(args, "exactMatch", false);
		hasCNoun = getBoolean(args, "hasCNoun", true);
	}

	public TokenStream create(TokenStream tokenstream) {
		return new KoreanFilter(tokenstream, bigrammable, hasOrigin, exactMatch, hasCNoun);
	}

	public void setBigrammable(boolean bool) {
		this.bigrammable = bool;
	}

	public void setHasOrigin(boolean bool) {
		this.hasOrigin = bool;
	}

	public void setHasCNoun(boolean bool) {
		this.hasCNoun = bool;
	}	

	/**
	 * determin whether the original compound noun is returned or not if a input word is analyzed morphically.
	 * @param has
	 */
	public void setExactMatch(boolean bool) {
		exactMatch = bool;
	}	
}
