package org.fedorahosted.flies.repository.util;

import org.fedorahosted.flies.repository.model.AbstractTextUnitTarget.Status;

public class TranslationStatistics {

	private long approved;
	private long forReview;
	private long fuzzyMatch;
	private long statNew;

	public TranslationStatistics() {
		// TODO Auto-generated constructor stub
	}
	
	public void set(Status status, long count){
		switch(status){
		case Approved:
			approved = count;
			break;
		case ForReview:
			forReview = count;
			break;
		case FuzzyMatch:
			fuzzyMatch = count;
			break;
		case New:
			statNew = count;
			break;
		}
	}
	
	public TranslationStatistics(Long approved, Long forReview, Long fuzzyMatch,
			Long statNew) {
		this.approved = approved;
		this.forReview = forReview;
		this.fuzzyMatch = fuzzyMatch;
		this.statNew = statNew;
	}

	public long get(Status status) {
		switch (status) {
		case Approved:
			return getApproved();
		case ForReview:
			return getForReview();
		case FuzzyMatch:
			return getFuzzyMatch();
		case New:
		default:
			return getNew();
		}
	}

	public long getTotal() {
		return approved + forReview + fuzzyMatch + statNew;
	}

	public long getNotApproved() {
		return forReview + fuzzyMatch + statNew;
	}

	public long getApproved() {
		return approved;
	}

	public long getForReview() {
		return forReview;
	}

	public long getFuzzyMatch() {
		return fuzzyMatch;
	}

	public long getNew() {
		return statNew;
	}

	@Override
	public String toString() {
		return "{a:" + approved + ",r:" + forReview + ",f:" + fuzzyMatch
				+ ",n:" + statNew + "}";
	}
}
