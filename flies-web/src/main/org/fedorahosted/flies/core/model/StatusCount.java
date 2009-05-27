package org.fedorahosted.flies.core.model;

import org.fedorahosted.flies.repository.model.AbstractTextUnitTarget.Status;

public class StatusCount{
		public final Status status;
		public final Long count;
		public StatusCount(Status status, Long count) {
			this.status = status;
			this.count = count;
		}
		
	}