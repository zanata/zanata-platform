package org.fedorahosted.flies.projects;

import java.io.File;
import java.util.List;

/**
 *	 
 */
public interface IProjectAdapter {

	/**
	 * Retrieves the unique ID of this adapter
	 * 
	 * @return the ID of this adapter
	 */
	public String getAdapterId();

	/**
	 * Retrieves the version of this adapter
	 * 
	 * @return the version of this adapter
	 */
	public int getAdapterVersion();

	/**
	 * Retrieves the base path of this adapter.
	 * 
	 * This path is the absolute file-system path of the adapter, to which
	 * relative paths are resolved.
	 * 
	 * @return the base path of this adapter
	 */
	public File getBasePath();

	/**
	 * Retrieves the common relative path to the resource templates managed by
	 * this adapter.
	 * 
	 * @return the common relative path for all resources managed by this
	 *         adapter
	 */
	public String getResourceBasePath();

	/**
	 * Retrieves the common relative path to the resource templates managed by
	 * this adapter.
	 * 
	 * @param language
	 *            the language for which to resolve the path
	 * @return the common relative path for target-language-specific resources
	 *         managed by this adapter
	 */
	public String getResourceBasePath(String language);

	/**
	 * Retrieves the list of languages managed by this adapter
	 * 
	 * @return the list of languages managed by this adapter
	 */
	public List<String> getTargetLanguages();

	/**
	 * 
	 * @param resource
	 * @return
	 */
	public List<String> getTargetLanguages(String resource);

	/**
	 * 
	 * @return
	 */
	public List<String> getResources();

	/**
	 * 
	 * @param language
	 * @return
	 */
	public List<String> getResources(String language);

	/**
	 * 
	 * @return
	 */
	public String getSourceLanguage();

}
