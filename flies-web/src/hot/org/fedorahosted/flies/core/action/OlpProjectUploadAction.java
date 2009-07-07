package org.fedorahosted.flies.core.action;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

@Name("olpUpload")
@Scope(ScopeType.EVENT)
public class OlpProjectUploadAction{

	@Logger 
	Log log;
	
    public OlpProjectUploadAction() {
    }

    public void listener(UploadEvent event) throws Exception{
        UploadItem item = event.getUploadItem();
        log.info("Uploading {0}. temp-file: {1}", item.getFileName(), item.isTempFile());
    }
}