package org.fedorahosted.flies.vcs;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;

public interface Changeset {
	    
        public enum Modification{Add,Modify,Delete}
        
        public String getId();
        
        public UserInfo getUser();
        
        public DateTime getTimestamp();
        
        public Map<String, Modification> getChanges();
        
        public List<String> getChanges(Modification type);
        
        public Modification getModificationType(File file);

}
