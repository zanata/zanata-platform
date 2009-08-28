package org.fedorahosted.flies.core.action;

import java.util.HashSet;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;

@Name("managmentTypes")
@Scope(ScopeType.APPLICATION)
public class ManagementTypes {

	public static final String TYPE_LOCAL = "Local File System (Publican)";  
	
	private static Set<String> managementTypes;
	
	static{
		managementTypes = new HashSet<String>();
		managementTypes.add("Web Client");
		managementTypes.add("REST Client");
		managementTypes.add("SVN Client (Publican)");
		managementTypes.add(TYPE_LOCAL);
	}
	
	@Unwrap
	public static Set<String> getManagementTypes() {
		return managementTypes;
	}
}
