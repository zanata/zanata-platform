/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.shotoku.service;

import javax.ejb.Local;

import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Service;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.tools.Constants;
import org.apache.log4j.Logger;

/**
 * 
 * @author Adam Warski (adamw@aster.pl)
 */
@Service(objectName=Constants.SHOTOKU_SERVICE_NAME)
@Local(ShotokuServiceLocal.class)
@Management(ShotokuService.class)
public class ShotokuServiceImpl
        implements ShotokuService, ShotokuServiceLocal {
    Logger log = Logger.getLogger(ShotokuService.class);

    /*
     * Service-handling functions.
     */

    public void create() throws Exception {
        /*
         * Setting up content managers.
         */
        ContentManager.setup();

        log.info("ContentManager setup completed.");
    }

    public void start() throws Exception {
    }

    public void stop() {
    }

    public void destroy() {
    }

}
