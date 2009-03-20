package org.fedorahosted.flies.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


public class PathVariable extends Task{
	
	
	private String name;
	private File path;
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setPath(File path) {
		this.path = path;
	}
	
	public File getPath() {
		return path;
	}
	
	
	@Override
	public void execute() throws BuildException {
		if(name == null){
			throw new BuildException("name attribute must be set");
		}
		if(path == null){
			throw new BuildException("path attribute must be set");
		}
	}
}