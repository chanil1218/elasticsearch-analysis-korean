package org.apache.lucene.analysis.kr.morph;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.kr.utils.ConstraintUtil;
import org.apache.lucene.analysis.kr.utils.DictionaryUtil;
import org.apache.lucene.analysis.kr.utils.EomiUtil;
import org.apache.lucene.analysis.kr.utils.IrregularUtil;
import org.apache.lucene.analysis.kr.utils.MorphUtil;
import org.apache.lucene.analysis.kr.utils.NounUtil;
import org.apache.lucene.analysis.kr.utils.SyllableUtil;
import org.apache.lucene.analysis.kr.utils.VerbUtil;

public class MorphAnalyzer {

	/**
	 * starting word of sentence.
	 */
	public static final int POS_START = 1;

	/**
	 * middle word of sentence
	 */
	public static final int POS_MID = 2;

	/**
	 * ending word of sentence.
	 */
	public static final int POS_END = 3;

	private CompoundNounAnalyzer cnAnalyzer = new CompoundNounAnalyzer();

	public MorphAnalyzer() {
		cnAnalyzer.setExactMach(false);
	}

	public void setExactCompound(boolean is) {
		cnAnalyzer.setExactMach(is);
	}

	public List analyze(String input) throws MorphException {

		if (input.endsWith("."))
			return analyze(input.substring(0, input.length() - 1), POS_END);

		return analyze(input, POS_MID);
	}

	/**
	 * 
	 * @param input
	 * @param pos
	 * @return
	 * @throws MorphException
	 */
	public List analyze(String input, int pos) throws MorphException {

		List<AnalysisOutput> candidates = new ArrayList();
		boolean isVerbOnly = MorphUtil.hasVerbOnly(input);

		analysisByRule(input, candidates);

		if (!isVerbOnly || candidates.size() == 0)
			addSingleWord(input, candidates);

		Collections.sort(candidates, new AnalysisOutputComparator());

		// 복합명사 분해여부 결정하여 분해
		boolean changed = false;
		boolean correct = false;
		for (AnalysisOutput o : candidates) {

			if (o.getScore() == AnalysisOutput.SCORE_CORRECT) {
				if (o.getPatn() != PatternConstants.PTN_NJ)
					correct = true;
				// "활성화해"가 [활성화(N),하(t),어야(e)] 분석성공하였는데 [활성/화해]분해되는 것을 방지
				if (o.getPatn() == PatternConstants.PTN_NSM)
					break;
				continue;
			}

			if (o.getPatn() < PatternConstants.PTN_VM
					&& o.getStem().length() > 2) {
				if (!(correct && o.getPatn() == PatternConstants.PTN_N))
					confirmCNoun(o);
				if (o.getScore() >= AnalysisOutput.SCORE_COMPOUNDS)
					changed = true;
			}

		}

		if (changed) {
			Collections.sort(candidates, new AnalysisOutputComparator());
		}

		List<AnalysisOutput> results = new ArrayList();

		boolean hasCorrect = false;
		boolean hasCorrectNoun = false;
		boolean correctCnoun = false;

		HashMap stems = new HashMap();
		AnalysisOutput noun = null;

		double ratio = 0;
		AnalysisOutput compound = null;

		for (AnalysisOutput o : candidates) {

			if (o.getScore() == AnalysisOutput.SCORE_FAIL)
				continue; // 분석에는 성공했으나, 제약조건에 실패
			if (o.getScore() == AnalysisOutput.SCORE_CORRECT
					&& o.getPos() != PatternConstants.POS_NOUN) {
				addResults(o, results, stems);
				hasCorrect = true;
			} else if (o.getPos() == PatternConstants.POS_NOUN
					&& o.getScore() == AnalysisOutput.SCORE_CORRECT) {

				if ((hasCorrect || correctCnoun) && o.getCNounList().size() > 0)
					continue;

				if (o.getPos() == PatternConstants.POS_NOUN) {
					addResults(o, results, stems);
				} else if (noun == null) {
					addResults(o, results, stems);
					noun = o;
				} else if (o.getPatn() == PatternConstants.PTN_N
						|| (o.getPatn() > noun.getPatn())
						|| (o.getPatn() == noun.getPatn()
								&& o.getJosa() != null
								&& noun.getJosa() != null && o.getJosa()
								.length() > noun.getJosa().length())) {
					results.remove(noun);
					addResults(o, results, stems);
					noun = o;
				}
				hasCorrectNoun = true;
				// if(o.getCNounList().size()>0) correctCnoun = true;
			} else if (o.getPos() == PatternConstants.POS_NOUN
					&& o.getCNounList().size() > 0 && !hasCorrect
					&& !hasCorrectNoun) {
				double curatio = NounUtil.countFoundNouns(o);
				if (ratio < curatio
						&& (compound == null || (compound != null && compound
								.getJosa() == null))) {
					ratio = curatio;
					compound = o;
				}
			} else if (o.getPos() == PatternConstants.POS_NOUN && !hasCorrect
					&& !hasCorrectNoun && compound == null) {
				addResults(o, results, stems);
			} else if (!hasCorrectNoun
					&& o.getPatn() == PatternConstants.PTN_NSM) {
				addResults(o, results, stems);
			}
		}

		if (compound != null)
			addResults(compound, results, stems);

		if (results.size() == 0) {
			AnalysisOutput output = new AnalysisOutput(input, null, null,
					PatternConstants.PTN_N, AnalysisOutput.SCORE_ANALYSIS);
			output.setPos(PatternConstants.POS_NOUN);
			results.add(output);
		}

		return results;
	}

	private void analysisByRule(String input, List candidates)
			throws MorphException {

		boolean josaFlag = true;
		boolean eomiFlag = true;

		int strlen = input.length();

		// boolean isVerbOnly = MorphUtil.hasVerbOnly(input);
		boolean isVerbOnly = false;
		analysisWithEomi(input, "", candidates);

		for (int i = strlen - 1; i > 0; i--) {

			String stem = input.substring(0, i);
			String eomi = input.substring(i);

			char[] feature = SyllableUtil.getFeature(eomi.charAt(0));
			if (!isVerbOnly && josaFlag
					&& feature[SyllableUtil.IDX_JOSA1] == '1') {
				analysisWithJosa(stem, eomi, candidates);
			}

			if (eomiFlag) {
				analysisWithEomi(stem, eomi, candidates);
			}

			if (josaFlag && feature[SyllableUtil.IDX_JOSA2] == '0')
				josaFlag = false;
			if (eomiFlag && feature[SyllableUtil.IDX_EOMI2] == '0')
				eomiFlag = false;

			if (!josaFlag && !eomiFlag)
				break;
		}
	}

	private void addResults(AnalysisOutput o, List results,
			HashMap<String, AnalysisOutput> stems) {
		AnalysisOutput old = stems.get(o.getStem());
		if (old == null || old.getPos() != o.getPos()) {
			results.add(o);
			stems.put(o.getStem(), o);
		} else if (old.getPatn() < o.getPatn()) {
			results.remove(old);
			results.add(o);
			stems.put(o.getStem(), o);
		}
	}

	private void addSingleWord(String word, List<AnalysisOutput> candidates)
			throws MorphException {

		// if(candidates.size()!=0&&candidates.get(0).getScore()==AnalysisOutput.SCORE_CORRECT)
		// return;

		AnalysisOutput output = new AnalysisOutput(word, null, null,
				PatternConstants.PTN_N);
		output.setPos(PatternConstants.POS_NOUN);

		WordEntry entry;
		if ((entry = DictionaryUtil.getWord(word)) != null) {

			if (entry.getFeature(WordEntry.IDX_NOUN) != '1'
					&& entry.getFeature(WordEntry.IDX_BUSA) == '1') {
				AnalysisOutput busa = new AnalysisOutput(word, null, null,
						PatternConstants.PTN_AID);
				busa.setPos(PatternConstants.POS_ETC);

				busa.setScore(AnalysisOutput.SCORE_CORRECT);
				candidates.add(0, busa);
			} else if (entry.getFeature(WordEntry.IDX_NOUN) == '1') {
				output.setScore(AnalysisOutput.SCORE_CORRECT);
				candidates.add(0, output);
			} else if (entry.getFeature(WordEntry.IDX_NOUN) == '2') {
				candidates.add(0, output);
			}

			if (entry.getFeature(WordEntry.IDX_VERB) != '1')
				return;
		} else if (candidates.size() == 0 || !NounUtil.endsWith2Josa(word)) {
			output.setScore(AnalysisOutput.SCORE_ANALYSIS);
			candidates.add(0, output);
		}
	}

	/**
	 * 체언 + 조사 (PTN_NJ) 체언 + 용언화접미사 + '음/기' + 조사 (PTN_NSMJ 용언 + '음/기' + 조사
	 * (PTN_VMJ) 용언 + '아/어' + 보조용언 + '음/기' + 조사(PTN_VMXMJ)
	 * 
	 * @param stem
	 * @param end
	 * @param candidates
	 * @throws MorphException
	 */
	public void analysisWithJosa(String stem, String end, List candidates)
			throws MorphException {

		if (stem == null || stem.length() == 0)
			return;

		char[] chrs = MorphUtil.decompose(stem.charAt(stem.length() - 1));
		if (!DictionaryUtil.existJosa(end)
				|| (chrs.length == 3 && ConstraintUtil.isTwoJosa(end))
				|| (chrs.length == 2 && (ConstraintUtil.isThreeJosa(end)) || ""
						.equals(end)))
			return; // 연결이 가능한 조사가 아니면...

		AnalysisOutput output = new AnalysisOutput(stem, end, null,
				PatternConstants.PTN_NJ);
		output.setPos(PatternConstants.POS_NOUN);

		boolean success = false;
		try {
			success = NounUtil.analysisMJ(output.clone(), candidates);
		} catch (CloneNotSupportedException e) {
			throw new MorphException(e.getMessage(), e);
		}

		WordEntry entry = DictionaryUtil.getWordExceptVerb(stem);
		if (entry != null) {
			output.setScore(AnalysisOutput.SCORE_CORRECT);
			if (entry.getFeature(WordEntry.IDX_NOUN) == '0'
					&& entry.getFeature(WordEntry.IDX_BUSA) == '1') {
				output.setPos(PatternConstants.POS_ETC);
				output.setPatn(PatternConstants.PTN_ADVJ);
			}
		} else {
			if (MorphUtil.hasVerbOnly(stem))
				return;
		}

		candidates.add(output);

	}

	/**
	 * 
	 * 1. 사랑받다 : 체언 + 용언화접미사 + 어미 (PTN_NSM) <br>
	 * 2. 사랑받아보다 : 체언 + 용언화접미사 + '아/어' + 보조용언 + 어미 (PTN_NSMXM) <br>
	 * 3. 학교에서이다 : 체언 + '에서/부터/에서부터' + '이' + 어미 (PTN_NJCM) <br>
	 * 4. 돕다 : 용언 + 어미 (PTN_VM) <br>
	 * 5. 도움이다 : 용언 + '음/기' + '이' + 어미 (PTN_VMCM) <br>
	 * 6. 도와주다 : 용언 + '아/어' + 보조용언 + 어미 (PTN_VMXM) <br>
	 * 
	 * @param stem
	 * @param end
	 * @param candidates
	 * @throws CloneNotSupportedException
	 */
	public void analysisWithEomi(String stem, String end, List candidates)
			throws MorphException {

		String[] morphs = EomiUtil.splitEomi(stem, end);
		if (morphs[0] == null)
			return; // 어미가 사전에 등록되어 있지 않다면....

		String[] pomis = EomiUtil.splitPomi(morphs[0]);

		AnalysisOutput o = new AnalysisOutput(pomis[0], null, morphs[1],
				PatternConstants.PTN_VM);
		o.setPomi(pomis[1]);

		try {

			WordEntry entry = DictionaryUtil.getVerb(o.getStem());
			if (entry != null
					&& !(("을".equals(end) || "은".equals(end) || "음".equals(end)) && (entry
							.getFeature(WordEntry.IDX_REGURA) == IrregularUtil.IRR_TYPE_LIUL || entry
							.getFeature(WordEntry.IDX_REGURA) == IrregularUtil.IRR_TYPE_BIUP))) {
				// System.out.println(entry.getWord());
				AnalysisOutput output = o.clone();
				output.setScore(AnalysisOutput.SCORE_CORRECT);
				MorphUtil.buildPtnVM(output, candidates);

				char[] features = SyllableUtil.getFeature(stem.charAt(stem
						.length() - 1)); // ㄹ불규칙일 경우
				if (features[SyllableUtil.IDX_YNPLN] == '0'
						|| morphs[1].charAt(0) != 'ㄴ')
					return;
			}

			String[] irrs = IrregularUtil.restoreIrregularVerb(o.getStem(),
					o.getPomi() == null ? o.getEomi() : o.getPomi());

			if (irrs != null) { // 불규칙동사인 경우
				AnalysisOutput output = o.clone();
				output.setStem(irrs[0]);
				if (output.getPomi() == null)
					output.setEomi(irrs[1]);
				else
					output.setPomi(irrs[1]);

				// entry = DictionaryUtil.getVerb(output.getStem());
				// if(entry!=null && VerbUtil.constraintVerb(o.getStem(),
				// o.getPomi()==null?o.getEomi():o.getPomi())) { // 4. 돕다
				// (PTN_VM)
				output.setScore(AnalysisOutput.SCORE_CORRECT);
				MorphUtil.buildPtnVM(output, candidates);
				// }
			}

			if (VerbUtil.ananlysisNSM(o.clone(), candidates))
				return;

			if (VerbUtil.ananlysisNSMXM(o.clone(), candidates))
				return;

			// [체언 + '에서/에서부터' + '이' + 어미]
			if (VerbUtil.ananlysisNJCM(o.clone(), candidates))
				return;

			if (VerbUtil.analysisVMCM(o.clone(), candidates))
				return;

			VerbUtil.analysisVMXM(o.clone(), candidates);

		} catch (CloneNotSupportedException e) {
			throw new MorphException(e.getMessage(), e);
		}

	}

	public void analysisCNoun(List<AnalysisOutput> candidates)
			throws MorphException {

		boolean success = false;
		for (AnalysisOutput o : candidates) {
			if (o.getPos() != PatternConstants.POS_NOUN)
				continue;
			if (o.getScore() == AnalysisOutput.SCORE_CORRECT)
				success = true;
			else if (!success)
				confirmCNoun(o);
		}
	}

	/**
	 * 복합명사인지 조사하고, 복합명사이면 단위명사들을 찾는다. 복합명사인지 여부는 단위명사가 모두 사전에 있는지 여부로 판단한다.
	 * 단위명사는 2글자 이상 단어에서만 찾는다.
	 * 
	 * @param o
	 * @return
	 * @throws MorphException
	 */
	public boolean confirmCNoun(AnalysisOutput o) throws MorphException {

		if (o.getStem().length() < 3)
			return false;

		WordEntry cnoun = DictionaryUtil.getCNoun(o.getStem());
		if (cnoun != null && cnoun.getFeature(WordEntry.IDX_NOUN) == '2') {
			o.addCNoun(cnoun.getCompounds());
			o.setScore(AnalysisOutput.SCORE_CORRECT);
			return true;
		}

		List<CompoundEntry> results = cnAnalyzer.analyze(o.getStem());
		// System.out.println(o);
		// for(CompoundEntry c :results)
		// System.out.println(c.getWord()+":"+c.isExist());
		boolean success = false;

		if (results.size() > 1) {
			o.setCNoun(results);
			success = true;
			for (CompoundEntry entry : results) {
				if (!entry.isExist())
					success = false;
			}
			o.setScore(AnalysisOutput.SCORE_COMPOUNDS);
		}

		if (success) {
			if (constraint(o)) {
				o.setScore(AnalysisOutput.SCORE_CORRECT);
			} else {
				o.setScore(AnalysisOutput.SCORE_FAIL);
				return false;
			}
		} else {
			if (NounUtil.confirmDNoun(o)
					&& o.getScore() != AnalysisOutput.SCORE_CORRECT) {
				confirmCNoun(o);
			}
			if (o.getScore() == AnalysisOutput.SCORE_CORRECT)
				success = true;
			if (o.getCNounList().size() > 0 && !constraint(o))
				o.setScore(AnalysisOutput.SCORE_FAIL);
		}

		return success;

	}

	private boolean constraint(AnalysisOutput o) throws MorphException {

		List<CompoundEntry> cnouns = o.getCNounList();

		if ("\uD654\uD574".equals(cnouns.get(cnouns.size() - 1).getWord())) {
			if (!ConstraintUtil.canHaheCompound(cnouns.get(cnouns.size() - 2)
					.getWord()))
				return false;
		} else if (o.getPatn() == PatternConstants.PTN_NSM) {
			if ("\uB0B4".equals(o.getVsfx())
					&& cnouns.get(cnouns.size() - 1).getWord().length() != 1) {
				WordEntry entry = DictionaryUtil.getWord(cnouns.get(
						cnouns.size() - 1).getWord());
				if (entry != null && entry.getFeature(WordEntry.IDX_NE) == '0')
					return false;
			} else if ("\uD558".equals(o.getVsfx())
					&& cnouns.get(cnouns.size() - 1).getWord().length() == 1) {
				// 짝사랑하다 와 같은 경우에 뒷글자가 1글자이면 제외
				return false;
			}
		}
		return true;

	}
}
