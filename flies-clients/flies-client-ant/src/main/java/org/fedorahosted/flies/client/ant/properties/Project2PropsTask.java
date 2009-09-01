package org.fedorahosted.flies.client.ant.properties;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.fedorahosted.flies.adapter.properties.PropWriter;
import org.fedorahosted.flies.rest.dto.DocumentRef;
import org.fedorahosted.flies.rest.dto.ProjectIteration;

public class Project2PropsTask extends MatchingTask {

    private String apiKey;
    private boolean debug;
    private File dstDir;
    private String src;
    private String url;
    
    @Override
    public void execute() throws BuildException {
	try {
	    Unmarshaller m = null;
	    if (debug) {
		JAXBContext jc = Context.newJAXBContext();
		m = jc.createUnmarshaller();
	    }
	    
	    URL srcURL = Utility.createURL(src, getProject());
	    
	    // TODO use rest api to fetch project
	    ProjectIteration projectIteration = (ProjectIteration) m.unmarshal(srcURL);
	    
	    for (DocumentRef doc : projectIteration.getDocuments()) {
		PropWriter.write(doc.getRef(), dstDir);
	    }
	} catch (Exception e) {
	    throw new BuildException(e);
	}
    }
    
    @Override
    public void log(String msg) {
        super.log(msg+"\n\n");
    }
    
//    private void logVerbose(String msg) {
//	super.log(msg, org.apache.tools.ant.Project.MSG_VERBOSE);
//    }
    
    public void setApiKey(String apiKey) {
	this.apiKey = apiKey;
    }
    
    public void setDebug(boolean debug) {
	this.debug = debug;
    }
    
    public void setDstDir(File dstDir) {
	this.dstDir = dstDir;
    }

    public void setSrc(String src) {
	this.src = src;
    }
    
    public void setUrl(String url) {
	this.url = url;
    }

}
