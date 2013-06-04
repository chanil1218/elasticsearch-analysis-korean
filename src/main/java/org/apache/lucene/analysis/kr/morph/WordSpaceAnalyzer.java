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
package org.apache.lucene.analysis.kr.morph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.kr.utils.DictionaryUtil;
import org.apache.lucene.analysis.kr.utils.MorphUtil;
import org.apache.lucene.analysis.kr.utils.SyllableUtil;
import org.apache.lucene.analysis.kr.utils.VerbUtil;

/**
 * 
 * @author smlee
 *
 */
public class WordSpaceAnalyzer {

	private MorphAnalyzer morphAnal;
	
	public WordSpaceAnalyzer() {
		morphAnal = new MorphAnalyzer();
		morphAnal.setExactCompound(false);
	}
	
	public List analyze(String input)  throws MorphException {

		List stack = new ArrayList();
		
		WSOutput output = new WSOutput();
		
		int wStart = 0;
		
		int sgCount = -9;
		
		Map<Integer, Integer> fCounter = new HashMap();
		
		for(int i=0;i<input.length();i++) {						
			
			char[] f = SyllableUtil.getFeature(input.charAt(i));
			
			String prefix = i==input.length()-1 ? "X" : input.substring(wStart,i+2);					
			Iterator iter = DictionaryUtil.findWithPrefix(prefix);
			
			List<AnalysisOutput> candidates = new ArrayList();		
			
			WordEntry entry = null;
					
			if(input.charAt(i)=='\uC788' || input.charAt(i)=='\uC5C6' || input.charAt(i)=='\uC55E') {
				addSingleWord(input.substring(wStart,i), candidates);
				
								
			// 다음 음절이 2음절 이상 단어에 포함되어 있고 마지막 음절이 아니라면   띄워쓰기 위치가 아닐 가능성이 크다.
			// 부사, 관형사, 감탄사 등 단일어일 가능성인 경우 띄워쓰기가 가능하나, 
			// 이 경우는 다음 음절을 조사하여 
			} else if(i!= input.length()-1 && iter.hasNext()) { 
				// 아무짓도 하지 않음.
				sgCount = i;
			} else if(!iter.hasNext() && 
					(entry=DictionaryUtil.getBusa(input.substring(wStart,i+1)))!=null) { 				
				candidates.add(buildSingleOutput(entry));
				
			// 현 음절이 조사나 어미가 시작되는 음절일 가능성이 있다면...	
			} else if(f[SyllableUtil.IDX_EOGAN]=='1'||f[SyllableUtil.IDX_JOSA1]=='1'){				
				if(f[SyllableUtil.IDX_JOSA1]=='1') 
					candidates.addAll(anlysisWithJosa(input.substring(wStart), i-wStart));

				if(f[SyllableUtil.IDX_EOGAN]=='1') 
					candidates.addAll(anlysisWithEomi(input.substring(wStart), i-wStart));
			}
	
			// 호보가 될 가능성이 높은 순으로 정렬한다.
			Collections.sort(candidates, new WSOuputComparator());
			
			// 길이가 가장 긴 단어를 단일어로 추가한다.
			appendSingleWord(candidates);
			
			// 분석에 실패한 단어를 
			analysisCompouns(candidates);
			
			// 호보가 될 가능성이 높은 순으로 정렬한다.
			Collections.sort(candidates, new WSOuputComparator());			
			
			int reseult = validationAndAppend(output, candidates, input);
			if(reseult==1) {
				i = output.getLastEnd()-1;
				wStart = output.getLastEnd();
			} else if(reseult==-1) {
				Integer index = fCounter.get(output.getLastEnd());
				if(index==null) index = output.getLastEnd();
				else index = index + 1;
				i = index;
				wStart = output.getLastEnd();
				fCounter.put(output.getLastEnd(), index);				
			}

		}
		
		// 분석에 실패하였다면 원래 문자열을 되돌려 준다.
		if(output.getLastEnd()<input.length()) {
			
			String source = input.substring(output.getLastEnd());
			int score = DictionaryUtil.getWord(source)==null ? AnalysisOutput.SCORE_ANALYSIS : AnalysisOutput.SCORE_CORRECT;
			AnalysisOutput o =new AnalysisOutput(source,null,null,PatternConstants.POS_NOUN,
					PatternConstants.PTN_N,score);
			
			o.setSource(source);
			output.getPhrases().add(o);
			morphAnal.confirmCNoun(o);
			
		}

		return output.getPhrases();
	}
	
	/**
	 * 조사로 끝나는 어구를 분석한다.
	 * @param snipt
	 * @param js
	 * @return
	 * @throws MorphException
	 */
	private List anlysisWithJosa(String snipt, int js) throws MorphException {

		List<AnalysisOutput> candidates = new ArrayList();
		if(js<1) return candidates;
		
		int jend = findJosaEnd(snipt, js);

		if(jend==-1) return candidates; // 타당한 조사가 아니라면...
	
		String input = snipt.substring(0,jend);

		boolean josaFlag = true;
		
		for(int i=input.length()-1;i>0;i--) {
			
			String stem = input.substring(0,i);
			
			String josa = input.substring(i);

			char[] feature =  SyllableUtil.getFeature(josa.charAt(0));	
			
			if(josaFlag&&feature[SyllableUtil.IDX_JOSA1]=='1') {
				morphAnal.analysisWithJosa(stem,josa,candidates);				
			}
				
			if(josaFlag&&feature[SyllableUtil.IDX_JOSA2]=='0') josaFlag = false;
			
			if(!josaFlag) break;
			
		}
		
		if(input.length()==1) {
			AnalysisOutput o =new AnalysisOutput(input,null,null,PatternConstants.POS_NOUN,
					 PatternConstants.PTN_N,AnalysisOutput.SCORE_ANALYSIS);
			candidates.add(o);
		}
		
		fillSourceString(input, candidates);
		
		return candidates;
	}
	
	/**
	 * 조사의 첫음절부터 조사의 2음절이상에 사용될 수 있는 음절을 조사하여
	 * 가장 큰 조사를 찾는다.
	 * @param snipt
	 * @param jstart
	 * @return
	 * @throws MorphException
	 */
	private int findJosaEnd(String snipt, int jstart) throws MorphException {
		
		int jend = jstart;

		// [것을]이 명사를 이루는 경우는 없다.
		if(snipt.charAt(jstart-1)=='것'&&(snipt.charAt(jstart)=='을')) return jstart+1;
		
		if(snipt.length()>jstart+2&&snipt.charAt(jstart+1)=='스') { // 사랑스러운, 자랑스러운 같은 경우르 처리함.
			char[] chrs = MorphUtil.decompose(snipt.charAt(jstart+2));

			if(chrs.length>=2&&chrs[0]=='ㄹ'&&chrs[1]=='ㅓ') return -1;
		}
		
		// 조사의 2음절로 사용될 수 마지막 음절을 찾는다.
		for(int i=jstart+1;i<snipt.length();i++) {
			char[] f = SyllableUtil.getFeature(snipt.charAt(i));
			if(f[SyllableUtil.IDX_JOSA2]=='0') break;
			jend = i;				
		}
				
		int start = jend;
		boolean hasJosa = false;
		for(int i=start;i>=jstart;i--) {
			String str = snipt.substring(jstart,i+1);
			if(DictionaryUtil.existJosa(str) && !findNounWithinStr(snipt,i,i+2) &&
					!isNounPart(snipt,jstart)) {
				jend = i;
				hasJosa = true;
				break;
			}
		}

		if(!hasJosa) return -1;
		
		return jend+1;
		
	}
	
	/**
	 * 향후 계산이나 원 문자열을 보여주기 위해 source string 을 저장한다.
	 * @param source
	 * @param candidates
	 */
	private void fillSourceString(String source, List<AnalysisOutput> candidates) {
		
		for(AnalysisOutput o : candidates) {
			o.setSource(source);
		}
		
	}
	
	/**
	 * 목록의 1번지가 가장 큰 길이를 가진다.
	 * @param candidates
	 */
	private void appendSingleWord(List<AnalysisOutput> candidates) throws MorphException {
	
		if(candidates.size()==0) return;
		
		String source = candidates.get(0).getSource();
		
		WordEntry entry = DictionaryUtil.getWordExceptVerb(source);
		
		if(entry!=null) {
			candidates.add(buildSingleOutput(entry));
		} else {

			if(candidates.get(0).getPatn()>PatternConstants.PTN_VM&&
					candidates.get(0).getPatn()<=PatternConstants.PTN_VMXMJ) return;
			
			if(source.length()<5) return;
			
			AnalysisOutput o =new AnalysisOutput(source,null,null,PatternConstants.POS_NOUN,
					 PatternConstants.PTN_N,AnalysisOutput.SCORE_ANALYSIS);
			o.setSource(source);
			morphAnal.confirmCNoun(o);			
			if(o.getScore()==AnalysisOutput.SCORE_CORRECT) candidates.add(o);
		}				
	}
	
	private void addSingleWord(String source, List<AnalysisOutput> candidates) throws MorphException {
		
		WordEntry entry = DictionaryUtil.getWordExceptVerb(source);
		
		if(entry!=null) {
			candidates.add(buildSingleOutput(entry));
		} else {
			AnalysisOutput o =new AnalysisOutput(source,null,null,PatternConstants.POS_NOUN,
					 PatternConstants.PTN_N,AnalysisOutput.SCORE_ANALYSIS);
			o.setSource(source);
			morphAnal.confirmCNoun(o);			
			candidates.add(o);
		}
		
//		Collections.sort(candidates, new WSOuputComparator());
		
	}
	
	private List anlysisWithEomi(String snipt, int estart) throws MorphException {

		List<AnalysisOutput> candidates = new ArrayList();
		
		int eend = findEomiEnd(snipt,estart);		

		// 동사앞에 명사분리
		int vstart = 0;
		for(int i=estart-1;i>=0;i--) {	
			Iterator iter = DictionaryUtil.findWithPrefix(snipt.substring(i,estart)); 
			if(iter.hasNext()) vstart=i;
			else break;
		}
			
		if(snipt.length()>eend &&
				DictionaryUtil.findWithPrefix(snipt.substring(vstart,eend+1)).hasNext()) 
			return candidates;	// 다음음절까지 단어의 일부라면.. 분해를 안한다.
		
		String pvword = null;
		if(vstart!=0) pvword = snipt.substring(0,vstart);
			
		while(true) { // ㄹ,ㅁ,ㄴ 이기때문에 어미위치를 뒤로 잡았는데, 용언+어미의 형태가 아니라면.. 어구 끝을 하나 줄인다.
			String input = snipt.substring(vstart,eend);
			anlysisWithEomiDetail(input, candidates);				
			if(candidates.size()==0) break;		
			if(("ㄹ".equals(candidates.get(0).getEomi()) ||
					"ㅁ".equals(candidates.get(0).getEomi()) ||
					"ㄴ".equals(candidates.get(0).getEomi())) &&
					eend>estart+1 && candidates.get(0).getPatn()!=PatternConstants.PTN_VM &&
					candidates.get(0).getPatn()!=PatternConstants.PTN_NSM
					) {
				eend--;
			}else if(pvword!=null&&candidates.get(0).getPatn()>=PatternConstants.PTN_VM&& // 명사 + 용언 어구 중에.. 용언어구로 단어를 이루는 경우는 없다.
					candidates.get(0).getPatn()<=PatternConstants.PTN_VMXMJ && DictionaryUtil.getWord(input)!=null){
				candidates.clear();
				break;
			}else if(pvword!=null&&VerbUtil.verbSuffix(candidates.get(0).getStem())
					&&DictionaryUtil.getNoun(pvword)!=null){ // 명사 + 용언화 접미사 + 어미 처리
				candidates.clear();
				anlysisWithEomiDetail(snipt.substring(0,eend), candidates);
				pvword=null;
				break;				
			} else {
				break;
			}
		}
						
		if(candidates.size()>0&&pvword!=null) {
			AnalysisOutput o =new AnalysisOutput(pvword,null,null,PatternConstants.POS_NOUN,
					PatternConstants.PTN_N,AnalysisOutput.SCORE_ANALYSIS);	
			morphAnal.confirmCNoun(o);
			
			List<CompoundEntry> cnouns = o.getCNounList();
			if(cnouns.size()==0) {
				boolean is = DictionaryUtil.getWordExceptVerb(pvword)!=null;
				cnouns.add(new CompoundEntry(pvword,0,is));
			} 
			
			for(AnalysisOutput candidate : candidates) {
				candidate.getCNounList().addAll(cnouns);
				candidate.getCNounList().add(new CompoundEntry(candidate.getStem(),0,true));
				candidate.setStem(pvword+candidate.getStem()); // 이렇게 해야 WSOutput 에 복합명사 처리할 때 정상처리됨
			}
			
		}

		fillSourceString(snipt.substring(0,eend), candidates);
	
		return candidates;
	}
	
	private void anlysisWithEomiDetail(String input, List<AnalysisOutput> candidates ) 
	throws MorphException {

		boolean eomiFlag = true;
		
		int strlen = input.length();
		
		char ch = input.charAt(strlen-1);
		char[] feature =  SyllableUtil.getFeature(ch);
		
		if(feature[SyllableUtil.IDX_YNPNA]=='1'||feature[SyllableUtil.IDX_YNPLA]=='1'||
				feature[SyllableUtil.IDX_YNPMA]=='1')
			morphAnal.analysisWithEomi(input,"",candidates);
		
		for(int i=strlen-1;i>0;i--) {
			
			String stem = input.substring(0,i);
			String eomi = input.substring(i);

			feature =  SyllableUtil.getFeature(eomi.charAt(0));		
			
			if(eomiFlag) {			
				morphAnal.analysisWithEomi(stem,eomi,candidates);
			}			
			
			if(eomiFlag&&feature[SyllableUtil.IDX_EOMI2]=='0') eomiFlag = false;
			
			if(!eomiFlag) break;
		}
		
	}
	
	/**
	 * 어미의 첫음절부터 어미의 1음절이상에 사용될 수 있는 음절을 조사하여
	 * 가장 큰 조사를 찾는다.
	 * @param snipt
	 * @param jstart
	 * @return
	 * @throws MorphException
	 */
	private int findEomiEnd(String snipt, int estart) throws MorphException {
		
		int jend = 0;
		
		String tail = null;
		char[] chr = MorphUtil.decompose(snipt.charAt(estart));
		if(chr.length==3 && (chr[2]=='ㄴ')) {
			tail = '은'+snipt.substring(estart+1);
		}else if(chr.length==3 && (chr[2]=='ㄹ')) {
			tail = '을'+snipt.substring(estart+1);			
		}else if(chr.length==3 && (chr[2]=='ㅂ')) {
			tail = '습'+snipt.substring(estart+1);
		}else {
			tail = snipt.substring(estart);
		}				

		// 조사의 2음절로 사용될 수 마지막 음절을 찾는다.
		int start = 0;
		for(int i=1;i<tail.length();i++) {
			char[] f = SyllableUtil.getFeature(tail.charAt(i));	
			if(f[SyllableUtil.IDX_EOGAN]=='0') break;
			start = i;				
		}
					
		for(int i=start;i>0;i--) { // 찾을 수 없더라도 1음절은 반드시 반환해야 한다.
			String str = tail.substring(0,i+1);	
			char[] chrs = MorphUtil.decompose(tail.charAt(i));	
			if(DictionaryUtil.existEomi(str) || 
					(i<2&&chrs.length==3&&(chrs[2]=='ㄹ'||chrs[2]=='ㅁ'||chrs[2]=='ㄴ'))) { // ㅁ,ㄹ,ㄴ이 연속된 용언은 없다, 사전을 보고 확인을 해보자
				jend = i;
				break;
			}
		}
		
		return estart+jend+1;
		
	}
	
	/**
	 * validation 후 후보가 될 가능성이 높은 최상위 것을 결과에 추가한다.
	 * 
	 * @param output
	 * @param candidates
	 * @param stack
	 */
	private int validationAndAppend(WSOutput output, List<AnalysisOutput> candidates, String input)
	throws MorphException {
		
		if(candidates.size()==0) return 0;
		
		AnalysisOutput o = candidates.remove(0);		
		AnalysisOutput po = output.getPhrases().size()>0 ?  output.getPhrases().get(output.getPhrases().size()-1) : null;
		
		String ejend = o.getSource().substring(o.getStem().length());
		
		char[] chrs = po!=null&&po.getStem().length()>0 ? MorphUtil.decompose(po.getStem().charAt(po.getStem().length()-1)) : null;
		String pjend = po!=null&&po.getStem().length()>0 ? po.getSource().substring(po.getStem().length()) : null;
		
		char ja = 'x'; // 임의의 문자
		if(po!=null&&(po.getPatn()==PatternConstants.PTN_VM||po.getPatn()==PatternConstants.PTN_VMCM||po.getPatn()==PatternConstants.PTN_VMXM)) {		
			char[] chs = MorphUtil.decompose(po.getEomi().charAt(po.getEomi().length()-1));
			if(chs.length==3) ja=chs[2];
			else if(chs.length==1) ja=chs[0];			
		}
		
		int nEnd = output.getLastEnd()+o.getSource().length();
		
		char[] f = nEnd<input.length() ? SyllableUtil.getFeature(input.charAt(nEnd)) : null;			
		
		// 밥먹고 같은 경우가 가능하나.. 먹고는 명사가 아니다.
		if(po!=null&&po.getPatn()==PatternConstants.PTN_N&&candidates.size()>0&&  
				o.getPatn()==PatternConstants.PTN_VM&&candidates.get(0).getPatn()==PatternConstants.PTN_N) {
			o = candidates.remove(0); 			
		}else if(po!=null&&po.getPatn()>=PatternConstants.PTN_VM&&candidates.size()>0&&
				candidates.get(0).getPatn()==PatternConstants.PTN_N&&
				(ja=='ㄴ'||ja=='ㄹ')) { // 다녀가ㄴ, 사,람(e) 로 분해 방지
			o = candidates.remove(0);
		}
		
		//=============================================
		if(o.getPos()==PatternConstants.POS_NOUN && MorphUtil.hasVerbOnly(o.getStem())) {		
			output.removeLast();		
			return -1;
		}else if(nEnd<input.length() && f[SyllableUtil.IDX_JOSA1]=='1' 
			&& DictionaryUtil.getNoun(o.getSource())!=null) {
			return -1;
		}else if(nEnd<input.length() && o.getScore()==AnalysisOutput.SCORE_ANALYSIS 
			&& DictionaryUtil.findWithPrefix(ejend+input.charAt(nEnd)).hasNext()) { // 루씬하ㄴ 글형태소분석기 방지
			return -1;	
		}else if(po!=null&&po.getPatn()==PatternConstants.PTN_VM&&"ㅁ".equals(po.getEomi())&&
				o.getStem().equals("하")) { // 다짐 합니다 로 분리되는 것 방지
			output.removeLast();
			return -1;	
		}else if(po!=null&&po.getPatn()==PatternConstants.PTN_N&&VerbUtil.verbSuffix(o.getStem())&&
				!"있".equals(o.getStem())) { // 사랑받다, 사랑스러운을 처리, 그러나 있은 앞 단어와 결합하지 않는다.
			output.removeLast();
			return -1;			
		} else {	
			output.addPhrase(o);				
		}
				
		return 1;
	}
	
	
	private AnalysisOutput buildSingleOutput(WordEntry entry) {
		
		char pos = PatternConstants.POS_NOUN;
		
		int ptn = PatternConstants.PTN_N;
		
		if(entry.getFeature(WordEntry.IDX_NOUN)=='0') {
			pos = PatternConstants.POS_AID;
			ptn = PatternConstants.PTN_AID;
		}
		
		AnalysisOutput o = new AnalysisOutput(entry.getWord(),null,null,pos,
				ptn,AnalysisOutput.SCORE_CORRECT);
		
		o.setSource(entry.getWord());
		
		return o;
	}
	
	private void analysisCompouns(List<AnalysisOutput> candidates) throws MorphException {
		
		// 복합명사 분해여부 결정하여 분해
		boolean changed = false;
		boolean correct = false;
		for(AnalysisOutput o:candidates) {
			
			if(o.getScore()==AnalysisOutput.SCORE_CORRECT) {
				if(o.getPatn()!=PatternConstants.PTN_NJ) correct=true;
				// "활성화해"가 [활성화(N),하(t),어야(e)] 분석성공하였는데 [활성/화해]분해되는 것을 방지
				if("하".equals(o.getVsfx())) break; 
				continue;
			}

			if(o.getPatn()<=PatternConstants.PTN_VM&&o.getStem().length()>2) {
				 if(!(correct&&o.getPatn()==PatternConstants.PTN_N)) morphAnal.confirmCNoun(o);
				 if(o.getScore()==AnalysisOutput.SCORE_CORRECT) changed=true;
			}
		}
		
	}
	
	/**
	 * 문자열에 
	 * @param str	분석하고자 하는 전체 문자열
	 * @param ws	문자열에서 명사를 찾는 시작위치
	 * @param es	문자열에서 명사를 찾는 끝 위치
	 * @return
	 * @throws MorphException
	 */
	private boolean findNounWithinStr(String str, int ws, int es) throws MorphException {

		if(str.length()<es) return false;
				
		for(int i=es;i<str.length();i++) {
			char[] f = SyllableUtil.getFeature(str.charAt(i));	
			if(i==str.length() || (f[SyllableUtil.IDX_JOSA1]=='1')) {				
				return (DictionaryUtil.getWord(str.substring(ws,i))!=null);
			}
		}
		
		return false;
	}
	
	private boolean isNounPart(String str, int jstart) throws MorphException  {
		
		if(true) return false;
		
		for(int i=jstart-1;i>=0;i--) {			
			if(DictionaryUtil.getWordExceptVerb(str.substring(i,jstart+1))!=null)
				return true;
			
		}
		
		
		return false;
		
	}
	
	private void printCandidate(WSOutput output) {
		
		List<AnalysisOutput> os = output.getPhrases();
		for(AnalysisOutput o : os) {
			System.out.print(o.toString()+"("+o.getScore()+")| ");
		}
		System.out.println("<==");
		
	}	
}
