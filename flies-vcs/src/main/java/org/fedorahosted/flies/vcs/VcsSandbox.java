package org.fedorahosted.flies.vcs;

import java.io.File;
import java.util.List;

public interface VcsSandbox {

        // this will checkout/clone or update/pull the repo to the latest revision
        public void update();
        
        // this will update/clone the repo at the given revision
        public void update(Changeset rev);
        
        // returns true if there is a newer version than the current revision
        public boolean isUpdated();
        
        public Changeset getCurrentChangeset();
        
        public List<Changeset> getRevisionsSince(Changeset oldRevision);
        public List<Changeset> getChangesets(Changeset from, Changeset to);
        
        public void commit(List<File> files, UserInfo user, String message);
        
        public List<Changeset> getRevisionsSince(String filename, Changeset oldRevision);
        public List<Changeset> getChangesets(String filename, Changeset from, Changeset to);
}

