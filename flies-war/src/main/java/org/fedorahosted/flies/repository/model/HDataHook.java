package org.fedorahosted.flies.repository.model;

import java.util.Set;

import javax.persistence.Entity;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.rest.dto.DataHook;


@Entity
public class HDataHook extends HResource{

	public HDataHook() {
	}
	
	public HDataHook(DataHook hook) {
		super(hook);
	}

	private static final long serialVersionUID = -555978165911935456L;

	@Override
	public DataHook toResource(Set<LocaleId> includedTargets, int levels) {
		DataHook dataHook = new DataHook(this.getResId());
		return dataHook;
	}
}
