/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.analysis.kr.tagging;

import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.kr.morph.AnalysisOutput;
import org.apache.lucene.analysis.kr.morph.MorphException;
import org.apache.lucene.analysis.kr.morph.PatternConstants;
import org.apache.lucene.analysis.kr.utils.ConstraintUtil;
import org.apache.lucene.analysis.kr.utils.FileUtil;
import org.apache.lucene.analysis.kr.utils.KoreanEnv;
import org.apache.lucene.analysis.kr.utils.StringUtil;
import org.apache.lucene.analysis.kr.utils.Trie;


/**
 * 여러개의 형태소분석 결과 중에 최적의 것을 선택한다.
 * 이 함수는 문장단위로 호출되어야 한다.
 * @author user
 *
 */
public class Tagger {
		
	private static Trie<String, String[]> occurrences;
	
	private static final String tagDicLoc = "tagger.dic";
	
	private static final String NILL = "NILL";
	
	private static final String NOPATN = "0";
	
	private AnalysisOutput po;
	
	public AnalysisOutput tagging(String psource, List<AnalysisOutput> pmorphs)  throws MorphException {
					
		return tagging(psource, null, pmorphs, null);
		
	}
	
	public AnalysisOutput tagging(String psource, String rsource, List<AnalysisOutput> pmorphs, List<AnalysisOutput> rmorphs)  throws MorphException {

		if((pmorphs==null||pmorphs.size()==0)&&(rmorphs==null||rmorphs.size()==0)) return null;
	
		po = lookupBest(psource, rsource, pmorphs, rmorphs);
		
		po.setSource(psource);

		return po;
		
	}
	
	/**
	 * po가 NULL이 아닌 경우만 호출된다.
	 * occurrence.dic 에 등록되어 있는 경우만.. 최적을 찾아서 반환한다.
	 * 1. 첫번째는 어간으로 시작되는 문법 규칙을 찾는다.
	 * 2. 두번째는 표층형으로 시작되는 문법규칙을 찾는다.
	 * @param morphs
	 * @return
	 */
	private AnalysisOutput lookupBest(String psource,String rsource, List<AnalysisOutput> pmorphs, List<AnalysisOutput> rmorphs)  throws MorphException {
		
		if(pmorphs.size()==1) return pmorphs.get(0);

		AnalysisOutput select  = null;
		if(rmorphs!=null&&rmorphs.size()!=0) select = lookupBestByRWord(psource, rsource, pmorphs, rmorphs);		
		if(select!=null) return select;

		if(po!=null) select = lookupBestByPWord(psource, pmorphs);
		
		if(select!=null) return select;

		return pmorphs.get(0);
	}
	
	/**
	 * 앞 어절에 의해 현재 어절을 결정한다.
	 * 앞 어절은 NULL이 아니다.
	 * @param source
	 * @param pmorphs
	 * @param rmorphs
	 * @return
	 * @throws MorphException
	 */
	private AnalysisOutput lookupBestByPWord(String rsource, List<AnalysisOutput> rmorphs)  throws MorphException {
		
	
		List<AnalysisOutput> removes = new ArrayList();				

		for(AnalysisOutput morph : rmorphs) {
	
			Iterator<String[]> iterw = getGR("F"+rsource+"^W");

			AnalysisOutput best = selectBest(iterw, po.getSource(), rsource, po, morph, true, removes);
			if(best!=null) return best;						

			Iterator<String[]> iters = getGR("F"+morph.getStem()+"^S");
			best = selectBest(iters, po.getSource(), rsource, po, morph, true, removes);
			if(best!=null) return best;				
			
		}	
		
		for(AnalysisOutput morph : removes) {
			if(rmorphs.size()>1) rmorphs.remove(morph);
		}
		
		return null;
		
	}
	
	/**
	 * 뒷 어절에 의해 현재 어절이 결정된다.
	 * 뒷 어절은 NULL이 아니다.
	 * @param source
	 * @param pmorphs
	 * @param rmorphs
	 * @return
	 * @throws MorphException
	 */
	private AnalysisOutput lookupBestByRWord(String psource, String rsource, List<AnalysisOutput> pmorphs, List<AnalysisOutput> rmorphs)  throws MorphException {
		
		List<AnalysisOutput> removes = new ArrayList();
		
		for(AnalysisOutput rmorph : rmorphs) {
			
			if(rmorph.getScore()!=AnalysisOutput.SCORE_CORRECT) break;
			
			String rend = rmorph.getJosa();
			if(rend==null) rend = rmorph.getEomi();						
			
			for(AnalysisOutput pmorph : pmorphs) {						
			
				Iterator<String[]> iterw = getGR("R"+psource+"^W/");
				
				String pend = pmorph.getJosa();
				if(pend==null) pend = pmorph.getEomi();
				
				AnalysisOutput best = selectBest(iterw, psource, rsource, pmorph, rmorph, false, removes);
				if(best!=null) return best;	
								
				Iterator<String[]> iters = getGR("R"+NILL+"/"+pend+"/");	
				best = selectBest(iters, psource, rsource, pmorph, rmorph, false, removes);
				if(best!=null) return best;	
				
				iters = getGR("R"+pmorph.getStem()+"^S/");	
				best = selectBest(iters, psource, rsource, pmorph, rmorph, false, removes);
				if(best!=null) return best;					
				
			}
						
		}		
		
		for(AnalysisOutput morph : removes) {
			if(pmorphs.size()>1) pmorphs.remove(morph);
		}
		
		return null;
		
	}
	
	private AnalysisOutput selectBest(Iterator<String[]> iter, String psource, String rsource, 
			AnalysisOutput pmorph, AnalysisOutput rmorph, boolean rear, List removes) {

		while(iter.hasNext()) {		

			String[] values = iter.next();
		
			if(checkGrammer(values, psource, rsource, pmorph, rmorph, rear)) {
				if(rear) return rmorph;
				else return pmorph;
			} else if("1".equals(values[6])) {
				if(!removes.contains(pmorph)) removes.add(pmorph);
				break;
			}				
		}
		
		return null;
		
	}
	
	private boolean checkGrammer(String[] values, String psource, String rsource, AnalysisOutput pmorph, AnalysisOutput rmorph, boolean depFront) {
		
		boolean ok = true;		
		
		String pend = pmorph.getJosa();
		if(pend==null) pend = pmorph.getEomi();

		String rend = rmorph.getJosa();
		if(rend==null) rend = rmorph.getEomi();

		if(depFront&&!NILL.equals(values[0])&&!checkWord(psource,values[0],pmorph)) { // 앞 어절의 어휘
			return false;
		} 			

		if(!NILL.equals(values[1])&& !checkEomi(values[1], pend)) { // 앞 어절의 어미
			return false;
		}

		if(!NOPATN.equals(values[2])&&!checkPattern(values[2], pmorph.getPatn())) {// 앞 어절의 패턴
			return false;
		} 	

		if(!depFront&&!NILL.equals(values[3])&&!checkWord(rsource,values[3],rmorph)) { // 뒷 어절의 어휘
			return false;			
		}

		if(!NILL.equals(values[4])&& !checkEomi(values[4], rend)) { // 뒷 어절의 어미
			return false;
		}

		if(!NOPATN.equals(values[5]) && !checkPattern(values[5], rmorph.getPatn())) { // 뒷 어절의 패턴
			return false;
		}

		return true;
		
	}
	
	private boolean checkWord(String source, String value, AnalysisOutput morph) {		
		
		String[] types = StringUtil.split(value,"^");
		String[] strs  = StringUtil.split(types[0],",");
		
		String text = source;
		if("S".equals(types[1])) text = morph.getStem();		
	
		for(int i=0;i<strs.length;i++) {
			if(strs[i].equals(text)) return true;
		}
		
		return false;
	}
	
	private boolean checkEomi(String value, String rend) {
		
		String[] strs  = StringUtil.split(value,",");
		
		for(int i=0;i<strs.length;i++) {
			if(strs[i].equals(rend)) return true;
		}
		
		return false;		
	}
	
	private boolean checkPattern(String value, int ptn) {
		
		String[] strs  = StringUtil.split(value,",");
		String strPtn = Integer.toString(ptn);
		
		for(int i=0;i<strs.length;i++) {
			
			if("E".equals(strs[i])&&ConstraintUtil.isEomiPhrase(ptn))
				return true;
			else if("J".equals(strs[i])&&
					(ConstraintUtil.isJosaNounPhrase(ptn)||ptn==PatternConstants.PTN_N)) 
				return true;			
			else if(strs[i].equals(strPtn)) 
				return true;
			
		}
		
		return false;		
	}
	
	public static synchronized Iterator<String[]> getGR(String prefix) throws MorphException {

		if(occurrences==null) loadTaggerDic();
		
		return occurrences.getPrefixedBy(prefix);
	}
	
	private static synchronized void loadTaggerDic() throws MorphException {
		
		occurrences = new Trie(true);
		
		try {
			
			List<String> strs = FileUtil.readLines(KoreanEnv.getInstance().getValue(tagDicLoc), "UTF-8");
			
			for(String str : strs) {
				if(str==null) continue;
				str = str.trim();
				String[] syls = StringUtil.split(str,":");
				if(syls.length!=4) continue;
				
				String key = null;				
				if("F".equals(syls[0])) key = syls[2].substring(0,syls[2].lastIndexOf("/")+1) + syls[1].substring(0,syls[1].lastIndexOf("/"));
				else key = syls[1].substring(0,syls[1].lastIndexOf("/")+1) + syls[2].substring(0,syls[2].lastIndexOf("/"));
				
				String[] patns = StringUtil.split(syls[1]+"/"+syls[2]+"/"+syls[3],"/");
				
				occurrences.add(syls[0]+key, patns);
				
			}			
			
		} catch (Exception e) {
			throw new MorphException("Fail to read the tagger dictionary.("+tagDicLoc+")\n"+e.getMessage());
		}
	}
		
}
