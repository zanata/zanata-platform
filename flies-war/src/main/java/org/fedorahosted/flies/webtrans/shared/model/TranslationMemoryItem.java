package org.fedorahosted.flies.webtrans.shared.model;

import java.io.Serializable;

public class TranslationMemoryItem implements Serializable{

	private static final long serialVersionUID = -7381018377520206564L;

	private String source;
	private String target;

	private String sourceComment;
	// TODO we should probably include transunit id too (useful when we support browser history for TUs)
	// TODO include obsolete flag and show this info to the user

	private String targetComment;

	private TransUnitId transUnitId;
	private float relevanceScore;
	private int similarityPercent;

	public TranslationMemoryItem() {
	}

	@Deprecated
	public TranslationMemoryItem(String source, String memory, TransUnitId transUnitId) {
		this(source, memory, "", "", transUnitId, 0, 50, 50);
	}
	
	@Deprecated
	public TranslationMemoryItem(String source, String memory, String sourceComment, String targetComment, TransUnitId transUnitId, long projectContainer, float relevanceScore, int similarityPercent) {
		this(source, memory, sourceComment, targetComment, transUnitId, relevanceScore, 50);
	}
	
	public TranslationMemoryItem(String source, String memory, String sourceComment, String targetComment, TransUnitId transUnitId, float relevanceScore, int similarityPercent) {
		this.source = source;
		this.target = memory;
		this.sourceComment = sourceComment;
		this.targetComment = targetComment;
		this.transUnitId = transUnitId;
		this.relevanceScore = relevanceScore;
		this.similarityPercent = similarityPercent;
	}

	public TransUnitId getTransUnitId() {
		return transUnitId;
	}
	
	public void setTransUnitId(TransUnitId transUnitId) {
		this.transUnitId = transUnitId;
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

	public void setSourceComment(String sourceComment) {
		this.sourceComment = sourceComment;
	}

	public String getSourceComment() {
		return sourceComment;
	}

	public void setTargetComment(String targetComment) {
		this.targetComment = targetComment;
	}

	public String getTargetComment() {
		return targetComment;
	}

}
