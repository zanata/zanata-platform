package org.fedorahosted.flies.webtrans;

import java.util.Date;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.core.model.HPerson;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;

public class TranslationWorkspace {
	
	private final LocaleId locale;
	private final Long projectId;
	
	private ConcurrentMap<HPerson, Date> translators = new MapMaker().expiration(60, TimeUnit.SECONDS).makeMap();

	public TranslationWorkspace(Long projectId, LocaleId locale) {
		if(projectId == null)
			throw new IllegalArgumentException("projectId");
		if(locale == null)
			throw new IllegalArgumentException("locale");
		this.locale = locale;
		this.projectId = projectId;
	}
	
	public LocaleId getLocale() {
		return locale;
	}

	public Long getProjectId() {
		return projectId;
	}
	
	public ImmutableList<HPerson> getTranslators(){
		return ImmutableList.copyOf(translators.keySet());
	}
	
	public int getTranslatorCount(){
		return translators.size();
	}
	
	public void registerTranslator(HPerson translator){
		Date timestamp = translators.get(translator);
		if(timestamp == null){
			timestamp = new Date();
			translators.put(translator, timestamp);	
		}
		else{
			translators.replace(translator, timestamp);	
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if( !(obj instanceof TranslationWorkspace) ) return false;
		TranslationWorkspace other = (TranslationWorkspace) obj;
		return ( other.locale.equals(locale) 
				&& other.projectId.equals(projectId));
	}
	
	@Override
	public int hashCode() {
	    int hash = 1;
	    hash = hash * 31 + locale.hashCode();
	    hash = hash * 31 + projectId.hashCode();
	    return hash;
	}
}
