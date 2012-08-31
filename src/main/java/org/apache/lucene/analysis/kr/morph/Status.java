package org.apache.lucene.analysis.kr.morph;

public class Status {

	private int josaMaxStart = 0;
	
	private int eomiMaxStart = 0;
	
	private int maxStart = 0;
	
	public void apply(int num) {
		if(maxStart<num) maxStart = num;
	}
	
	public int getMaxStart() {
		return maxStart;
	}

	public void setMaxStart(int maxStart) {
		this.maxStart = maxStart;
	}

	public int getJosaMaxStart() {
		return josaMaxStart;
	}

	public void setJosaMaxStart(int josaMaxStart) {
		this.josaMaxStart = josaMaxStart;
	}


	public int getEomiMaxStart() {
		return eomiMaxStart;
	}

	public void setEomiMaxStart(int eomiMaxStart) {
		this.eomiMaxStart = eomiMaxStart;
	}
	
}
