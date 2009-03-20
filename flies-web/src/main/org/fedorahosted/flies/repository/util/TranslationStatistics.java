package org.fedorahosted.flies.repository.util;

import org.fedorahosted.flies.repository.model.AbstractTextUnitTarget.Status;

public class TranslationStatistics {

	private int approved;
	private int forReview;
	private int fuzzyMatch;
	private int statNew;

	public TranslationStatistics(int approved, int forReview, int fuzzyMatch,
			int statNew) {
		this.approved = approved;
		this.forReview = forReview;
		this.fuzzyMatch = fuzzyMatch;
		this.statNew = statNew;
	}

	public int get(Status status) {
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

	public int getTotal() {
		return approved + forReview + fuzzyMatch + statNew;
	}

	public int getNotApproved() {
		return forReview + fuzzyMatch + statNew;
	}

	public int getApproved() {
		return approved;
	}

	public int getForReview() {
		return forReview;
	}

	public int getFuzzyMatch() {
		return fuzzyMatch;
	}

	public int getNew() {
		return statNew;
	}

	@Override
	public String toString() {
		return "{a:" + approved + ",r:" + forReview + ",f:" + fuzzyMatch
				+ ",n:" + statNew + "}";
	}
}
