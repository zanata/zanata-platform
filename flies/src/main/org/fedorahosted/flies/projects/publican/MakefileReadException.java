package org.fedorahosted.flies.projects.publican;

public class MakefileReadException extends RuntimeException{
	public MakefileReadException() {
		super();
	}
	public MakefileReadException(String message) {
		super(message);
	}
	public MakefileReadException(Throwable cause) {
		super(cause);
	}
	public MakefileReadException(String message, Throwable cause) {
		super(message, cause);
	}
	
}	
