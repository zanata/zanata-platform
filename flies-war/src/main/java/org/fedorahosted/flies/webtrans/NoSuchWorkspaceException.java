package org.fedorahosted.flies.webtrans;

import org.jboss.seam.annotations.exception.HttpError;

@HttpError(errorCode=404,message="Workspace doesn't exist")
public class NoSuchWorkspaceException extends RuntimeException{

	public NoSuchWorkspaceException() {
		super();
	}

	public NoSuchWorkspaceException(String message) {
		super(message);
	}
	
	
}
