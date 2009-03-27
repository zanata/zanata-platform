package org.fedorahosted.flies.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

public class AntClasspathTask extends Task{

	private File sourceDir;
	private File classPathFile;
	private List<FileSet> filesets = new ArrayList<FileSet>();
	private List<PathVariable> variables = new ArrayList<PathVariable>();
	
	private File baseDir;
	
	private Element rootElement;
	private Document classpathDocument;
	private int firstEntryPos = -1;
	
	private File projectDir;
	
	private void openClassPathFile(){
		try{
			Builder b = new Builder();
			classpathDocument = b.build(classPathFile);
			rootElement = classpathDocument.getRootElement();
		}
		catch(IOException e){
			throw new BuildException(e);			
		} catch (ValidityException e) {
			throw new BuildException(e);			
		} catch (ParsingException e) {
			throw new BuildException(e);			
		}
	}
	
	public void addFileSet(FileSet fileSet){
		filesets.add(fileSet);
	}
	
	public void addPathVariable(PathVariable variable){
		variables.add(variable);
	}

	public File getSourceDir() {
		return sourceDir;
	}
	
	public void setSourceDir(File sourceDir) {
		this.sourceDir = sourceDir;
	}
	
	public File getBaseDir() {
		return baseDir;
	}
	
	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}
	
	public File getClassPathFile() {
		return classPathFile;
	}
	
	public void setClassPathFile(File classPathFile) {
		this.classPathFile = classPathFile;
	}

	private void cleanLibEntries(){
		String expression = "/classpath/classpathentry[@kind = 'lib']";

		Nodes nodes = classpathDocument.query(expression);
		for(int i=0;i<nodes.size();i++){
			Node node = nodes.get(i);
			if(firstEntryPos == -1){
				firstEntryPos = rootElement.indexOf(node);
			}
			// remove any existing definitions
			node.detach();
		}
	}
	
	private void cleanVarEntries(){
		
		if(variables.isEmpty()) return;
		
		String expression = "/classpath/classpathentry[@kind = 'var']";

		Nodes nodes = classpathDocument.query(expression);
		for(int i=0;i<nodes.size();i++){
			Element elem = (Element) nodes.get(i);
			if(firstEntryPos == -1){
				firstEntryPos = rootElement.indexOf(elem);
			}
			// remove any existing definitions
			String att = elem.getAttributeValue("path");
			for(PathVariable var : variables){
				if(att.startsWith(var.getName())){
					elem.detach();
					break;
				}
			}
		}
	}
	
	@Override
	public void execute() throws BuildException {

		if(classPathFile == null){
			throw new BuildException("classpathFile attribute must be set");
		}
		else if (!classPathFile.exists()){
			throw new BuildException("classpathFile does not exist!");
		}

		openClassPathFile();
		cleanLibEntries();
		cleanVarEntries();
		
		if(firstEntryPos == -1){
			firstEntryPos = rootElement.getChildCount();
		}

		projectDir =  classPathFile.getAbsoluteFile().getParentFile();
		
		for(FileSet fs : filesets){
			DirectoryScanner scanner = fs.getDirectoryScanner();
			
			File baseDir= scanner.getBasedir().getAbsoluteFile();

			for(String incFile: scanner.getIncludedFiles()){
				
				File absFile = new File(baseDir.getAbsolutePath() + File.separatorChar + incFile);
				
				Element cpEntry = new Element("classpathentry");
				
				File sourceFile = resolveSourceFile(absFile);
				
				EntryType eType = matchesVariablePath(absFile) ? EntryType.Var: EntryType.Lib;
				
				
				String path = resolvePath(absFile, eType);
				
				if(sourceFile == null){
					cpEntry = createEntry(path, eType);
				}
				else{
					String sourcePath = resolvePath(sourceFile, eType);
					cpEntry = createEntry(path, sourcePath, eType);
				}
				
				rootElement.insertChild(cpEntry, firstEntryPos++);
			}
		}

		OutputStream out = null;
		
		try{
			out = new FileOutputStream(classPathFile); 
			Serializer serializer = new Serializer(out);
			serializer.setIndent(4);
			serializer.write(classpathDocument);
		}
		catch(IOException e){
			throw new BuildException(e);
		}
		finally{
			if(out != null){
				try{ out.close();}
				catch(Exception e){}
			}
		}
	}

	private File resolveSourceFile(File absFile){
		
		String filename = absFile.getName();
		String sourceFilename = filename.substring(0,filename.length()-4) + "-sources.jar";
		
		File sourceFile;
		// first look in the same dir
		sourceFile = new File(absFile.getParentFile().getAbsolutePath() + 
										File.separatorChar + sourceFilename);
		
		if(sourceFile.exists()) return sourceFile;
		
		// then in a ./src/ subdir
		sourceFile = new File(absFile.getParentFile().getAbsolutePath() + 
				File.separatorChar + "src" +
				File.separatorChar + sourceFilename);
		
		if(sourceFile.exists()) return sourceFile;
		
		return null;
	}

	private boolean matchesVariablePath(File absFile){
		String absFileStr = absFile.getAbsolutePath();

		for(PathVariable var : variables){
			String pathVar = var.getPath().getAbsolutePath();
			if(absFileStr.startsWith(pathVar)){
				//return "$" + var.getName() + "$" + absFileStr.substring(pathVar.length());
				return true;
			}
			
		}
		
		return false;
	}

	private String resolvePath(File absFile, EntryType type){
		
		switch(type){
		case Lib:
			return resolveLibraryPath(absFile);
		default:
			return resolveVariablePath(absFile);
		}
	}
	
	private String resolveVariablePath(File absFile){
		String absFileStr = absFile.getAbsolutePath();

		for(PathVariable var : variables){
			String pathVar = var.getPath().getAbsolutePath();
			if(absFileStr.startsWith(pathVar)){
				return var.getName() + absFileStr.substring(pathVar.length());
			}
			
		}
		throw new RuntimeException("file does not match any variable paths");
		
	}
	
	private String resolveLibraryPath(File absFile){
		String absFileStr = absFile.getAbsolutePath();
		String absProjectPathStr = projectDir.getAbsolutePath();
		String absBaseDirStr = null;
		if(baseDir != null)
			absBaseDirStr = baseDir.getAbsolutePath();
		
		if(absFileStr.startsWith(absProjectPathStr)){

			return absFileStr.substring(absProjectPathStr.length()+1);
		}
		else if(absBaseDirStr != null && absFileStr.startsWith(absBaseDirStr)){
			return absFileStr.substring(absBaseDirStr.length());
		}
		return absFileStr;
	}
	
	enum EntryType {Lib, Var};
	
	private Element createEntry(String path, EntryType type){
		Element cpEntry = new Element("classpathentry");
		switch(type){
		case Lib:
			cpEntry.addAttribute(new Attribute("kind", "lib"));
			break;
		case Var:
			cpEntry.addAttribute(new Attribute("kind", "var"));
			break;
		}
		cpEntry.addAttribute(new Attribute("path", path));
		return cpEntry;
	}
	
	private Element createEntry(String path, String sourcepath, EntryType type){
		Element cpEntry = createEntry(path, type);
		cpEntry.addAttribute(new Attribute("sourcepath", sourcepath));
		return cpEntry;
	}
	
}
