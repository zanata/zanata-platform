/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.zanata.client.util;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.client.GlossaryClient;

import javax.ws.rs.client.ResponseProcessingException;

/**
 *
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 *
 */
public class GlossaryCommandUtil {
    private static final Logger log = LoggerFactory
            .getLogger(GlossaryCommandUtil.class);

    public static String getQualifiedProjectName(String project, GlossaryClient client)
            throws Exception {
        String qualifiedName = null;
        try {
            qualifiedName = StringUtils.isBlank(project)
                    ? client.getGlobalQualifiedName()
                    : client.getProjectQualifiedName(project);
        } catch (ResponseProcessingException rpe) {
            if (rpe.getResponse().getStatus() == 404) {
                log.error("Project {} not found", project);
            } else {
                throw rpe;
            }
        }
        return qualifiedName;
    }
}
