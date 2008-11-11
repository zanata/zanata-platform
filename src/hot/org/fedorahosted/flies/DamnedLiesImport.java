package org.fedorahosted.flies;

import javax.ejb.Local;

@Local
public interface DamnedLiesImport {

    public String damnedLiesImport();
    
    public String getUrl();
    public void setUrl(String url);
    public String getSource();
    public void setSource(String source);
    
    public String getStep();
    public void setStep(String step);
    
    public void destroy();

}
