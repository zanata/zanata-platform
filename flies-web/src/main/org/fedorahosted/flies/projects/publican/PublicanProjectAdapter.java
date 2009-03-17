package org.fedorahosted.flies.projects.publican;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.projects.AbstractProjectAdapter;
import org.fedorahosted.flies.projects.AdapterException;

public class PublicanProjectAdapter extends AbstractProjectAdapter{

	public static final String ID = "publican";
	public static final int VERSION = 1;
	
	private Map<String, String> makefileVariables;
	
	private String brandName;
	private String sourceLanguage;
	private List<String> targetLanguges;
	
	private MakefileVariableReader makefileReader;

	private static final String VARIABLE_XML_LANG = "XML_LANG";
	private static final String VARIABLE_OTHER_LANGS = "OTHER_LANGS";
	private static final String VARIABLE_BRAND = "BRAND";

	private static final String EXTENSION_PO = ".po";
	private static final String EXTENSION_POT = ".pot";
	
	private static final String RESOURCE_BASE_PATH = "pot";

	private List<String> resources;
	
	private static FilenameFilter templateResourceFilenameFilter = new ExtentionBasedFilenameFilter(EXTENSION_POT);
	private static FilenameFilter resourceFilenameFilter = new ExtentionBasedFilenameFilter(EXTENSION_PO);

	public PublicanProjectAdapter(File basePath) {
		super(basePath);
		makefileReader = new MakefileVariableReader();
		makefileReader.setFailOnMissingVariables(true);
	}
	
	public String getAdapterId(){
		return ID;
	}
	
	public int getAdapterVersion() {
		return VERSION;
	}

	public List<String> getTargetLanguages(){
		if(targetLanguges == null){
			if(makefileVariables == null){
				readMakefile();
			}
			targetLanguges = Arrays.asList(
						makefileVariables.get(VARIABLE_OTHER_LANGS).split("\\s")
							);
		}
		return targetLanguges;
	}
	
	@Override
	public List<String> getTargetLanguages(String resource) {
		List<String> allLanguages = getTargetLanguages();
		List<String> targetLanguages = new ArrayList<String>();
		
		for(String lang : allLanguages){
			File langPath = new File(getBasePath(), getResourceBasePath(lang));
			File resourceFile = new File(langPath, resource.substring(0,resource.length()-EXTENSION_POT.length())+ EXTENSION_PO);
			if(resourceFile.exists()){
				targetLanguages.add(lang);
			}
		}
		return targetLanguages;
	}

	@Override
	public String getResourceBasePath(){
		return RESOURCE_BASE_PATH;
	}

	@Override
	public String getResourceBasePath(String language){
		return language;
	}
	
	
	public List<String> getResources(){
		if(resources == null){
			File sourceDir = new File(getBasePath(), getResourceBasePath());
			if(!sourceDir.exists() || !sourceDir.isDirectory()){
				throw new AdapterException("Couldn't find resource directory");
			}
			resources = Arrays.asList(sourceDir.list(templateResourceFilenameFilter));
			
		}
		return resources;
	}

	public List<String> getResources(String language){
		
		if(language.equals(getSourceLanguage())){
			return getResources();
		}
		
		File sourceDir = new File(getBasePath(), language);
		if(!sourceDir.exists() || !sourceDir.isDirectory()){
			throw new AdapterException("Couldn't find resource directory");
		}
		return Arrays.asList(sourceDir.list(resourceFilenameFilter));
	}
	
	
	public static void main(String[] args) {
		PublicanProjectAdapter adaptor = new PublicanProjectAdapter(new File("/home/asgeirf/projects/gitsvn/Deployment_Guide"));
		
		for(String resource : adaptor.getResources()){
			System.out.println(resource + StringUtils.join(adaptor.getTargetLanguages(resource),","));
		}
		System.out.println(adaptor.getBookName());
	}
	
	public String getSourceLanguage(){
		if(sourceLanguage == null){
			if(makefileVariables == null){
				readMakefile();
			}
			sourceLanguage = makefileVariables.get(VARIABLE_XML_LANG); 
		}
		return sourceLanguage; 
	}
	
	private void readMakefile(){
		try{
			makefileVariables = makefileReader.read(new File(getBasePath(), "Makefile"),
				VARIABLE_XML_LANG, VARIABLE_OTHER_LANGS, VARIABLE_BRAND);
		}
		catch(MakefileReadException e){
			throw new AdapterException("Error parsing Makefile", e);
		}
	}
	
	public String getBrandName(){
		if(brandName == null){
			if(makefileVariables == null){
				readMakefile();
			}
			brandName = makefileVariables.get(VARIABLE_BRAND); 
		}
		return brandName; 
	}

	public String getBookName(){
		return getBasePath().getName();
	}
	
	public static String getPublicanVersion(){
		// TODO : parse output of rpm 
		throw new AdapterException("Unable to retrieve version.");
	}
}
