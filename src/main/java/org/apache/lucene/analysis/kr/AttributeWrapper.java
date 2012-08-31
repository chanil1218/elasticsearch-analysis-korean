package org.apache.lucene.analysis.kr;

import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class AttributeWrapper {

    private TermAttribute termAtt;
    private OffsetAttribute offsetAtt;
    private PositionIncrementAttribute posIncrAtt;
    private TypeAttribute typeAtt;
	  
    public AttributeWrapper(TermAttribute term,OffsetAttribute offset,
    		PositionIncrementAttribute pos, TypeAttribute type) {
    	this.termAtt = term;
    	this.offsetAtt = offset;
    	this.posIncrAtt = pos;
    	this.typeAtt = type;
    }
    
    public AttributeWrapper() {
    	
    }
    
	public TermAttribute getTermAtt() {
		return termAtt;
	}
	public void setTermAtt(TermAttribute termAtt) {
		this.termAtt = termAtt;
	}
	public OffsetAttribute getOffsetAtt() {
		return offsetAtt;
	}
	public void setOffsetAtt(OffsetAttribute offsetAtt) {
		this.offsetAtt = offsetAtt;
	}
	public PositionIncrementAttribute getPosIncrAtt() {
		return posIncrAtt;
	}
	public void setPosIncrAtt(PositionIncrementAttribute posIncrAtt) {
		this.posIncrAtt = posIncrAtt;
	}
	public TypeAttribute getTypeAtt() {
		return typeAtt;
	}
	public void setTypeAtt(TypeAttribute typeAtt) {
		this.typeAtt = typeAtt;
	}

	  
	
}
