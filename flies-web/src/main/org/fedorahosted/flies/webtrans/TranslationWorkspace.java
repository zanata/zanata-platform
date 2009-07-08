package org.fedorahosted.flies.webtrans;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import net.openl10n.adapters.LocaleId;
import net.openl10n.packaging.jpa.project.HProject;

import org.fedorahosted.flies.core.model.Person;
import org.fedorahosted.flies.core.model.ProjectIteration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;

public class TranslationWorkspace {
	
	private final LocaleId locale;
	private final HProject project;
	
	private ConcurrentMap<Person, Date> translators = new MapMaker().expiration(60, TimeUnit.SECONDS).makeMap();

	public TranslationWorkspace(HProject project, LocaleId locale) {
		if(project == null)
			throw new IllegalArgumentException("project");
		if(locale == null)
			throw new IllegalArgumentException("locale");
		this.locale = locale;
		this.project = project;
	}
	
	public LocaleId getLocale() {
		return locale;
	}

	public HProject getProject() {
		return project;
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
				&& other.project.equals(project));
	}
	
	@Override
	public int hashCode() {
	    int hash = 1;
	    hash = hash * 31 + locale.hashCode();
	    hash = hash * 31 + project.hashCode();
	    return hash;
	}
}
