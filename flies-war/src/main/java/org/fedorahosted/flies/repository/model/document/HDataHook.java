package org.fedorahosted.flies.repository.model.document;

import javax.persistence.Entity;

import org.fedorahosted.flies.rest.dto.DataHook;


@Entity
public class HDataHook extends HParentResource{

	public HDataHook() {
	}
	
	public HDataHook(DataHook hook) {
		super(hook);
	}

	private static final long serialVersionUID = -555978165911935456L;

}
