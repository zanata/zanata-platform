package org.fedorahosted.flies.resources;

import org.fedorahosted.flies.common.LocaleId;



public final class LocaleOutputSourcePair {
	
	private final LocaleId localeId;
	private final OutputSource outputSource;
	
	public LocaleOutputSourcePair(OutputSource outputSource, LocaleId localeId) {
		if(outputSource == null)
			throw new IllegalArgumentException("outputSource");
		this.outputSource = outputSource;

		if(localeId == null)
			throw new IllegalArgumentException("localeId");
		this.localeId = localeId;
	}
	
	public LocaleId getLocaleId() {
		return localeId;
	}

	public OutputSource getOutputSource() {
		return outputSource;
	}

}
