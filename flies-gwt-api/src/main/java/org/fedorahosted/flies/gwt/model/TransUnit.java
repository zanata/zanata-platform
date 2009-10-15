package org.fedorahosted.flies.gwt.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TransUnit implements IsSerializable, Serializable{
	
	private static final long serialVersionUID = -8247442475446266600L;

	private boolean fuzzy;
	
	private TransUnitId id;
	
	private LocaleId localeId;
	
	private String source;
	private String target;
	
	@SuppressWarnings("unused")
	private TransUnit(){
	}
	
	public TransUnit(TransUnitId id, LocaleId localeId) {
		this.id = id;
		this.localeId = localeId;
		this.source = "";
		this.target = "";
	}
	public TransUnit(TransUnitId id, LocaleId localeId, String source, String target) {
		this.id = id;
		this.source = source;
		this.target = target;
	}
	
	public TransUnitId getId() {
		return id;
	}
	
	public LocaleId getLocaleId() {
		return localeId;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getTarget() {
		return target;
	}
	
	public void setTarget(String target) {
		this.target = target;
	}
	
	public boolean isFuzzy() {
		return fuzzy;
	}
	
	public void setFuzzy(boolean fuzzy) {
		this.fuzzy = fuzzy;
	}
}
