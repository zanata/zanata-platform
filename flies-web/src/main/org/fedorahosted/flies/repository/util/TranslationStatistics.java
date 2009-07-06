package org.fedorahosted.flies.repository.util;

import net.openl10n.packaging.document.TextFlowTarget.ContentState;

public class TranslationStatistics {

	private long approved;
	private long forReview;
	private long fuzzyMatch;
	private long statNew;

	public TranslationStatistics() {
		// TODO Auto-generated constructor stub
	}
	
	public void set(ContentState status, long count){
		switch(status){
		case Final:
			approved = count;
			break;
		case ForReview:
			forReview = count;
			break;
		case Leveraged:
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

	public long get(ContentState status) {
		switch (status) {
		case Final:
			return getApproved();
		case ForReview:
			return getForReview();
		case Leveraged:
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
