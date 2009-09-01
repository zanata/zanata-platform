package org.fedorahosted.flies.client.ant.properties;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.fedorahosted.flies.ContentType;
import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.adapter.properties.PropReader;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentRef;
import org.fedorahosted.flies.rest.dto.ProjectIteration;

public class Props2ProjectTask extends MatchingTask {

    private String apiKey;
    private boolean debug;
    private String dst;
    private String[] locales;
    private String projectID;
    private String sourceLang;
    private File srcDir;
    private String url;

    @Override
    public void execute() throws BuildException {
	try {
	    DirectoryScanner ds = getDirectoryScanner(srcDir);
	    // use default includes if unset:
	    if (!getImplicitFileSet().hasPatterns()) {
		ds.setIncludes(new String[] { "**/*.properties" }); //$NON-NLS-1$
	    }
	    ds.setSelectors(getSelectors());
	    ds.scan();
	    String[] files = ds.getIncludedFiles();

	    Marshaller m = null;
	    JAXBContext jc = Context.newJAXBContext();
	    m = jc.createMarshaller();
	    if (debug) {
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    }
	    
	    ProjectIteration projectIteration = new ProjectIteration(projectID, null);
	    PropReader propReader = new PropReader();
	    // for each of the base props files under srcdir:
	    for (String filename : files) {
		Document doc = new Document(filename, ContentType.TextPlain);
		doc.setLang(LocaleId.fromJavaName(sourceLang));
		File f = new File(srcDir, filename);
		propReader.extractAll(doc, f, locales);
		projectIteration.getDocuments().add(new DocumentRef(doc));
	    }
	    if (debug) {
		m.marshal(projectIteration, System.out);
	    }
	    // TODO send project to rest api

	    if(dst == null)
		return;
	    
	    URL dstURL = Utility.createURL(dst, getProject());
//	    m.marshall(project, dstURL.openConnection().getOutputStream());
	    if("file".equals(dstURL.getProtocol())) {
		m.marshal(projectIteration, new File(dstURL.getFile()));
	    }
		

	} catch (Exception e) {
	    throw new BuildException(e);
	}
    }
    
    FileSelector[] getSelectors() {
	if (locales != null)
	    return new FileSelector[] { new BasePropertiesSelector(locales) };
	else
	    return new FileSelector[0];
    }
    
    @Override
    public void log(String msg) {
        super.log(msg+"\n\n");
    }

    private void logVerbose(String msg) {
	super.log(msg, org.apache.tools.ant.Project.MSG_VERBOSE);
    }
    
    public void setApiKey(String apiKey) {
	this.apiKey = apiKey;
    }
    
    public void setDebug(boolean debug) {
	this.debug = debug;
    }
    
    public void setDst(String dst) {
	this.dst = dst;
    }
    
    public void setLocales(String locales) {
	this.locales = locales.split(","); //$NON-NLS-1$
    }

    public void setProjectID(String projectID) {
	this.projectID = projectID;
    }

    public void setSourceLang(String sourceLang) {
	this.sourceLang = sourceLang;
    }

    public void setSrcDir(File srcDir) {
	this.srcDir = srcDir;
	logVerbose("srcDir=" + srcDir);
    }

    public void setUrl(String url) {
	this.url = url;
    }
}
