package org.zanata.service.impl;

import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("versionManager")
// Singleton with no state
@Scope(ScopeType.APPLICATION)
@Slf4j
public class VersionManager {

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
