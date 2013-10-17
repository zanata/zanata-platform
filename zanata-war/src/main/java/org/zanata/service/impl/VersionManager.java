package org.zanata.service.impl;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("versionManager")
// Singleton with no state
@Scope(ScopeType.APPLICATION)
public class VersionManager {
    @Logger
    static Log log;

    public boolean checkVersion(String client, String server) {
        log.debug("start version check client version:" + client
                + " server version:" + server);
        // TODO: compatible server and client
        return true;
    }

    public boolean checkBuildTime(String client, String server) {
        return server.equalsIgnoreCase(client);
    }

}
