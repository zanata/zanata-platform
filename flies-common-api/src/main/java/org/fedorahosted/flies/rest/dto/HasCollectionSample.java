package org.fedorahosted.flies.rest.dto;

import java.util.Collection;

public interface HasCollectionSample<T> extends HasSample<T> {
	T createSample();
	Collection<T> createSamples(); 
}
