package org.zanata.service.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("versionManager")
@ApplicationScoped
public class VersionManager {
    private static final Logger log =
            LoggerFactory.getLogger(VersionManager.class);

    public boolean checkVersion(String client, String server) {
        log.debug("client version: {}, server version: {}",
                client, server);
        // TODO: check compatible server and client
        return true;
    }

    public boolean checkBuildTime(String client, String server) {
        return server.equalsIgnoreCase(client);
    }
}
