package org.fedorahosted.flies.webtrans;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.fedorahosted.flies.core.model.FliesLocale;
import org.fedorahosted.flies.core.model.Person;
import org.fedorahosted.flies.core.model.ProjectIteration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;

public class TranslationWorkspace {
	
	private final FliesLocale locale;
	private final ProjectIteration projectIteration;
	
	private ConcurrentMap<Person, Date> translators = new MapMaker().expiration(60, TimeUnit.SECONDS).makeMap();

	public TranslationWorkspace(ProjectIteration projectIteration, FliesLocale locale) {
		if(projectIteration == null)
			throw new NullPointerException("projectIteration cannot be null");
		if(locale == null)
			throw new NullPointerException("locale cannot be null");
		this.locale = locale;
		this.projectIteration = projectIteration;
	}
	
	public FliesLocale getLocale() {
		return locale;
	}

	public ProjectIteration getProjectIteration() {
		return projectIteration;
	}
	
	public ImmutableList<Person> getTranslators(){
		return ImmutableList.copyOf(translators.keySet());
	}
	
	public int getTranslatorCount(){
		return translators.size();
	}
	
	public void registerTranslator(Person translator){
		Date timestamp = translators.get(translator);
		if(timestamp == null){
			timestamp = new Date();
		}
		translators.put(translator, timestamp);	
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if( !(obj instanceof TranslationWorkspace) ) return false;
		TranslationWorkspace other = (TranslationWorkspace) obj;
		return ( other.locale.equals(locale) 
				&& other.projectIteration.equals(projectIteration));
	}
	
	@Override
	public int hashCode() {
	    int hash = 1;
	    hash = hash * 31 + locale.hashCode();
	    hash = hash * 31 + projectIteration.hashCode();
	    return hash;
	}
}
