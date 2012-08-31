package org.apache.lucene.analysis.kr.morph;

/**
 * 복합명사의 개별단어에 대한 정보를 담고있는 클래스 
 * @author S.M.Lee
 *
 */
public class CompoundEntry {
	
	private String word;
	
	private int offset = -1;
	
	private boolean exist = true;
	
	private char pos = PatternConstants.POS_NOUN;
	
	public CompoundEntry() {
		
	}
	
	public CompoundEntry(String w) {
		this.word = w;
	}
	
	public CompoundEntry(String w,int o) {
		this(w);
		this.offset = o;		
	}
	
	public CompoundEntry(String w,int o, boolean is) {
		this(w,o);
		this.exist = is;		
	}
	
	public CompoundEntry(String w,int o, boolean is, char p) {
		this(w,o,is);
		this.pos = p;
	}
	
	public void setWord(String w) {
		this.word = w;
	}
	
	public void setOffset(int o) {
		this.offset = o;
	}
	
	public String getWord() {
		return this.word;
	}
	
	public int getOffset() {
		return this.offset;
	}
	
	public boolean isExist() {
		return exist;
	}
	
	public void setExist(boolean is) {
		this.exist = is;
	}
	
	public char getPos() {
		return pos;
	}

	public void setPos(char pos) {
		this.pos = pos;
	}	
}
