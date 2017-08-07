package org.zanata.service.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named("versionManager")
@ApplicationScoped
public class VersionManager {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(VersionManager.class);

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
