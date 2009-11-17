package org.fedorahosted.flies.core.model;

import org.fedorahosted.flies.common.ContentState;

public class StatusCount{
		public final ContentState status;
		public final Long count;
		public StatusCount(ContentState status, Long count) {
			this.status = status;
			this.count = count;
		}
		
	}