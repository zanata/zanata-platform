package org.fedorahosted.flies.webtrans;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.fedorahosted.flies.core.model.FliesLocale;
import org.fedorahosted.flies.core.model.Person;
import org.fedorahosted.flies.core.model.ProjectTarget;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;

public class TranslationWorkspace {
	
	private final FliesLocale locale;
	private final ProjectTarget projectTarget;
	
	private ConcurrentMap<Person, Date> translators = new MapMaker().expiration(60, TimeUnit.SECONDS).makeMap();

	public TranslationWorkspace(ProjectTarget projectTarget, FliesLocale locale) {
		if(projectTarget == null)
			throw new NullPointerException("projectTarget cannot be null");
		if(locale == null)
			throw new NullPointerException("locale cannot be null");
		this.locale = locale;
		this.projectTarget = projectTarget;
	}
	
	public FliesLocale getLocale() {
		return locale;
	}

	public ProjectTarget getProjectTarget() {
		return projectTarget;
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
				&& other.projectTarget.equals(projectTarget));
	}
	
	@Override
	public int hashCode() {
	    int hash = 1;
	    hash = hash * 31 + locale.hashCode();
	    hash = hash * 31 + projectTarget.hashCode();
	    return hash;
	}
}
