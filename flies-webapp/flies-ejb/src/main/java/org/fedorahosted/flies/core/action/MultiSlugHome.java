package org.fedorahosted.flies.core.action;

import org.jboss.seam.framework.EntityHome;

public class MultiSlugHome<E> extends EntityHome<E>{

	private static final long serialVersionUID = 1L;

	private String multiSlug;
	private String [] slugs = new String[0];
	
	public String getMultiSlug() {
		return multiSlug;
	}
	
	public void setMultiSlug(String multiSlug) {
		this.multiSlug = multiSlug;
		slugs = multiSlug.split("/");
		if(slugs.length != 0){
			this.setId(slugs[slugs.length-1]);
		}
	}
	
	public String getSlug(int pos){
		return slugs[pos];
	}
	
	public int getSlugCount(){
		return slugs.length;
	}

	@Override
	public void setId(Object id) {
		if(slugs.length != 0){
			slugs[slugs.length-1] = (String)id;
		}
		super.setId(id);
	}
	
}
