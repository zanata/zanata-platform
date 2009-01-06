package org.fedorahosted.flies.projects;

import java.io.File;
import java.util.List;

public interface IProjectAdapter {
	
	public String getAdapterId();

	public int getAdapterVersion();
	
	public boolean isWritable();
	
	public File getBasePath();

	public String getResourceBasePath();
	
	public String getResourceBasePath(String language);
	
	public List<String> getTargetLanguages();
	
	public List<String> getTargetLanguages(String resource);

	public List<String> getResources();
	
	public List<String> getResources(String language);
	
	public String getSourceLanguage();
	
}
