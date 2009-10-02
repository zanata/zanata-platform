package org.fedorahosted.flies.webtrans.client;

import com.extjs.gxt.ui.client.data.BaseModel;

public class TransUnit extends BaseModel {
	
	public TransUnit(String source, String target) {
		set("source", source);
		set("target", target);
	}
	
	public String getSource() {
		return (String) get("source");
	}
	
	public String getTarget() {
		return (String) get("target");
	}

}
