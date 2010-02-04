package org.fedorahosted.flies.repository.util;

import org.fedorahosted.flies.common.ContentState;

public class TranslationStatistics {

	private long approved;
	private long needReview;
	private long statNew;

	public TranslationStatistics() {
		// TODO Auto-generated constructor stub
	}
	
	public void set(ContentState status, long count){
		switch(status){
		case Approved:
			approved = count;
			break;
		case NeedReview:
			needReview = count;
			break;
		case New:
			statNew = count;
			break;
		}
	}
	
	public TranslationStatistics(Long approved, Long needReview, Long statNew) {
		this.approved = approved;
		this.needReview = needReview;
		this.statNew = statNew;
	}

	public long get(ContentState status) {
		switch (status) {
		case Approved:
			return getApproved();
		case NeedReview:
			return getNeedReview();
		case New:
		default:
			return getNew();
		}
	}

	public long getTotal() {
		return approved + needReview + statNew;
	}

	public long getNotApproved() {
		return needReview + statNew;
	}

	public long getApproved() {
		return approved;
	}

	public long getNeedReview() {
		return needReview;
	}

	public long getNew() {
		return statNew;
	}

	@Override
	public String toString() {
		return "{a:" + approved + ",r:" + needReview + ",n:" + statNew + "}";
	}
}
