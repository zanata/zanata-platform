package org.fedorahosted.flies.webtrans.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

public class TranslationMemoryItem implements Serializable{

	private static final long serialVersionUID = -7381018377520206564L;

	private String source;
	private String target;

	private ArrayList<Long> transUnitIdList = new ArrayList<Long>();
	private float relevanceScore;
	private int similarityPercent;

	@SuppressWarnings("unused")
	private TranslationMemoryItem() {
	}

	public TranslationMemoryItem(String source, String memory, float relevanceScore, int similarityPercent) {
		this.source = source;
		this.target = memory;
		this.relevanceScore = relevanceScore;
		this.similarityPercent = similarityPercent;
	}

	public ArrayList<Long> getTransUnitIdList() {
		return transUnitIdList;
	}
	
	public void addTransUnitId(Long transUnitId) {
		this.transUnitIdList.add(transUnitId);
	}
	
	public void setTarget(String target) {
		this.target = target;
	}

	public String getTarget() {
		return target;
	}
	
	public void setRelevanceScore(float relevanceScore) {
		this.relevanceScore = relevanceScore;
	}
	
	public float getRelevanceScore() {
		return relevanceScore;
	}
	
	public int getSimilarityPercent() {
		return similarityPercent;
	}
	
	public void setSimilarityPercent(int similarityPercent) {
		this.similarityPercent = similarityPercent;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}

}
