package org.fedorahosted.flies.webtrans.action;

import javax.faces.model.DataModel;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.DataModels;

@Name("org.jboss.seam.faces.dataModels")
@Install(precedence=Install.DEPLOYMENT)
@Scope(ScopeType.STATELESS)
@BypassInterceptors
public class CustomDataModels extends DataModels {
	
	@Override
	public DataModel getDataModel(Object value) {
		if (value instanceof DataModel) {
			return (DataModel) value;
		} else {
			return super.getDataModel(value);
		}
	}
}
