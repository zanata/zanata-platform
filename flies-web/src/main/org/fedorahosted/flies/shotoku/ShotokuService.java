package org.fedorahosted.flies.shotoku;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.svn.SvnContentManager;
import org.jboss.shotoku.svn.SvnTools;
import org.jboss.shotoku.svn.service.SvnRepository;
import org.jboss.shotoku.svn.service.delayed.DelayedOperation;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;

@Scope(ScopeType.APPLICATION)
@Name("shotokuService")
public class ShotokuService {

	@Logger
	Log log;
	
	private ConcurrentMap<String, SvnRepository> repositories;

    private boolean firstUpdate;

//    private CacheItemUser<Object, Object> svnCacheItem;
    
    /*
     * Service lifecycle management.
     */

    @Observer("Flies.startup")
    public void create() throws Exception {
    	
    	ContentManager.setup();
        // Set up connection protocols support:
        // for DAV (over http and https)
        DAVRepositoryFactory.setup();
        log.info("DAVRepositoryFactory setup.  Setting up SVNRepositoryFactory...");

        // for SVN (over svn and svn+ssh)
        SVNRepositoryFactoryImpl.setup();
        log.info("SVNRepositoryFactory setup complete.");

        repositories = new ConcurrentHashMap<String, SvnRepository>();

//        svnCacheItem = CacheItem.create(new SvnCacheItemDataSource(this));
//        svnCacheItem.get(new Object());
//        svnCacheItem.setInterval(ContentManager.getProperty(
//                SvnTools.PROPERTY_INTERVAL, SvnTools.DEFAULT_TIMER_INTERVAL));
        setFirstUpdate(ContentManager.getProperty(
                SvnTools.FIRST_UPDATE, 1) != 0);

        ContentManager.initializeContentManager(SvnContentManager.class.getName());
        
        
        log.info(getServiceInfo());
        HashMap<String, String> repoConf = new HashMap<String, String>();
        repoConf.put(SvnTools.PROPERTY_URL, "http://svn.fedorahosted.org/svn/tennera/trunk/jgettext/src/main/antlr/");
        repoConf.put(SvnTools.PROPERTY_USERNAME, "");
        repoConf.put(SvnTools.PROPERTY_PASSWORD, "");
        repoConf.put(SvnTools.PROPERTY_LOCALPATH, "/tmp/shotoku-tennera");
        registerRepository("tennera", new MapConfiguration(repoConf));
        log.info(getServiceInfo());
        
        
    }

    /*
      * Timer-handling functions.
      */

    public void update() {
        for (SvnRepository repo : repositories.values()) {
            try {
                repo.update();
            } catch (Exception e) {
                log.error("Exception while updating repository: " +
                        repo.getId() + ".", e);
            }
        }
    }

    public String getServiceInfo() {
        StringBuffer sb = new StringBuffer("These repositories are currently handled: ");
        for (String id : repositories.keySet()) {
            sb.append(id).append(" (").
                    append(repositories.get(id).getLastRevision()).append("); ");
        }

        return sb.append(".").toString();
    }

    /*
     * SvnService implementation.
     */

    public boolean getFirstUpdate() {
        return firstUpdate;
    }

    public void setFirstUpdate(boolean firstUpdate) {
        this.firstUpdate = firstUpdate;
    }

    public void registerRepository(String id, Configuration conf) {
        if (repositories.get(id) == null) {
            SvnRepository repo = new SvnRepository(id,
                    conf.getString(SvnTools.PROPERTY_USERNAME),
                    conf.getString(SvnTools.PROPERTY_PASSWORD),
                    conf.getString(SvnTools.PROPERTY_URL),
                    conf.getString(SvnTools.PROPERTY_LOCALPATH),
                    conf.getInt(SvnTools.PROPERTY_FULLUPDATE, 1),
                    conf.getBoolean(SvnTools.PROPERTY_EXTERNALS, true));

            repositories.put(id, repo);

            // First update/ checkout.
            if (firstUpdate) {
                repo.firstUpdate();
            }

            log.info("Added svn repository: " + id);
        }
    }

    public boolean isModified(String id, String fullPath) {
        return repositories.get(id).isModified(fullPath);
    }

    public void addToDeleted(String id, String fullPath) {
        repositories.get(id).addToDeleted(fullPath);
    }

    public boolean isDeleted(String id, String fullPath) {
        return repositories.get(id).isDeleted(fullPath);
    }

    public void addNodeToModfied(String id, String fullPath) {
        repositories.get(id).addNodeToModfied(fullPath);
    }

    public boolean isNodeModified(String id, String fullPath) {
        return repositories.get(id).isNodeModified(fullPath);
    }

    public void addTreeToModfied(String id, String fullPath) {
        repositories.get(id).addTreeToModfied(fullPath);
    }

    public boolean isTreeModified(String id, String fullPath) {
        return repositories.get(id).isTreeModified(fullPath);
    }

    public void addDirectoryToModfied(String id, String fullPath) {
        repositories.get(id).addDirectoryToModfied(fullPath);
    }

    public boolean isDirectoryModified(String id, String fullPath) {
        return repositories.get(id).isDirectoryModified(fullPath);
    }

    public void addDelayedOperation(String id, DelayedOperation op) {
        repositories.get(id).addDelayedOperation(op);
    }

    public void addPathsToUpdate(String id, long revision, Set<String> paths) {
        repositories.get(id).addPathsToUpdate(id, revision, paths);
    }
}
