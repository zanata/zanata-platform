package net.openl10n.packaging.jpa.document;

import javax.persistence.Entity;

import net.openl10n.api.rest.document.DataHook;

@Entity
public class HDataHook extends HParentResource{

	public HDataHook() {
	}
	
	public HDataHook(DataHook hook) {
		super(hook);
	}

	private static final long serialVersionUID = -555978165911935456L;

}
