package org.fedorahosted.flies.core.action;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;

import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

@Name("fileUploadBean")
public class FileUploadBean{
        
        private PoFile file;
        private String content;
        private int size;
        private int uploadsAvailable = 5;
	private boolean autoUpload = false;
        private boolean useFlash = true;
	
        @Out (required = false)
        private ArrayList<PoFile> files = new ArrayList<PoFile>();

        public int getSize() {
                if (getFiles().size()>0){
                        return getFiles().size();
                }else 
                {
                        return 0;
                }
        }

	public FileUploadBean() {
	}

	public void listener(UploadEvent event) throws IOException{
	    UploadItem item = event.getUploadItem();
	    file = new PoFile();
            file.setLength(item.getData().length);
            file.setName(item.getFileName());
            File temp = new File(item.getFileName());
            FileOutputStream f = new FileOutputStream(temp);
            f.write(item.getData());
            file.setContent(temp);
            //For test purpose
            System.out.println("File Name:"+file.getName());
            System.out.println(file.getContent());
            files.add(file);
            uploadsAvailable--;
	}
	
	public int getUploadsAvailable() {  
         	return uploadsAvailable;  
     	}  
    
	public void setUploadsAvailable(int uploadsAvailable) {  
         	this.uploadsAvailable = uploadsAvailable;  
	}  

        public ArrayList<PoFile> getFiles() {
                return files;
        }

        public void setFiles(ArrayList<PoFile> files) { 
                this.files = files;
        }
    
     	public boolean isAutoUpload() {  
         	return autoUpload;  
     	}  
    
     	public void setAutoUpload(boolean autoUpload) {  
         	this.autoUpload = autoUpload;  
     	}  
    
     	public boolean isUseFlash() {  
         	return useFlash;  
     	}  
    
     	public void setUseFlash(boolean useFlash) {  
         	this.useFlash = useFlash;  
     	}  

}

