package org.fedorahosted.flies;



import com.google.common.collect.ImmutableList;

public final class LocaleOutputSourceTupple {
	
	private final ImmutableList<LocaleId> localeIds;
	private final OutputSource outputSource;
	
	public LocaleOutputSourceTupple(OutputSource outputSource, LocaleId ... localeIds) {
		if(outputSource == null)
			throw new IllegalArgumentException("outputSource");
		this.outputSource = outputSource;

		for (int i = 0; i < localeIds.length; i++) {
			if(localeIds[i] == null)
				throw new IllegalArgumentException("localeIds");
		}
		this.localeIds = ImmutableList.of(localeIds);
	}
	
	public ImmutableList<LocaleId> getLocaleIds() {
		return localeIds;
	}

	public OutputSource getOutputSource() {
		return outputSource;
	}

}
