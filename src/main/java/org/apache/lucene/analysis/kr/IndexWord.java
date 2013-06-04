package org.apache.lucene.analysis.kr;

/**
 * Index word extracted from a phrase.
 * @author lsm
 *
 */
public class IndexWord {

	private String word;
	
	private int offset = 0;
	
	public IndexWord() {
		
	}
	
	public IndexWord(String word, int pos) {
		this.word = word;
		this.offset = pos;
	}
	
	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}
	
	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	
}
