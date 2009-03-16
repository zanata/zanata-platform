package org.fedorahosted.flies.projects;

import java.io.File;
import java.util.List;

public abstract class AbstractProjectAdapter implements IProjectAdapter{

	private File basePath;

	public AbstractProjectAdapter(File basePath) {
		this.basePath = basePath;
	}
	
	public File getBasePath() {
		return basePath;
	}
	
	public String getResourceBasePath() {
		return "";
	}
	
	public String getResourceBasePath(String language) {
		return "";
	}

	public List<String> getTargetLanguages(String resource) {
		return getTargetLanguages();
	}
}
