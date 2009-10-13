package org.fedorahosted.flies.webtrans.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TransUnit implements IsSerializable {
	
	private boolean fuzzy;
	
	private String source;
	private String target;
	
	public TransUnit(String source, String target) {
		this.source = source;
		this.target = target;
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
