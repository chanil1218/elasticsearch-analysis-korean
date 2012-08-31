package org.apache.lucene.analysis.kr.morph;

import java.util.Comparator;
import java.util.List;

public class WSCandidateComparator implements Comparator<WSOutput> {

	public int compare(WSOutput o1, WSOutput o2) {		
		
		int end = o2.getLastEnd() - o1.getLastEnd();
		if(end!=0) return end;
		
		int s1 = o1.getPhrases().size()==0 ? 999999999 : o1.getPhrases().size();
		int s2 = o2.getPhrases().size()==0 ? 999999999 : o2.getPhrases().size();
		
		int size = s1-s2;		
		if(size!=0) return size;
				
		int score = calculateScore(o2)-calculateScore(o1);
		if(score!=0) return score;
					
		return 0;
	}

	private int calculateScore(WSOutput o) {		
				
		List<AnalysisOutput> entries = o.getPhrases();
		
		if(entries.size()==0) return 0;
		
		int sum = 0;
		for(int i=0;i<entries.size();i++) {
			sum += entries.get(i).getScore();
		}
	
		return sum / entries.size();
	}
	
}
