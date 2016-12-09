/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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

package org.zanata.rest.service;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public final class RestConstants {
    private RestConstants() {
    }

    public static final String SLUG_PATTERN =
            "[a-zA-Z0-9]+([a-zA-Z0-9_\\-{.}]*[a-zA-Z0-9]+)?";

    // slug patterns:
    public static final String PROJECT_SLUG_TEMPLATE =
            "{projectSlug:" + SLUG_PATTERN + "}";
    private static final String ITERATION_SLUG_TEMPLATE =
            "{iterationSlug:" + SLUG_PATTERN + "}";
    static final String VERSION_SLUG_TEMPLATE =
            "/{versionSlug:" + SLUG_PATTERN + "}";

    // project-related service paths:
    static final String PROJECT_SERVICE_PATH =
            "/projects/p/" + PROJECT_SLUG_TEMPLATE;
    static final String PROJECT_ITERATION_SERVICE_PATH =
            PROJECT_SERVICE_PATH + "/iterations/i/" + ITERATION_SLUG_TEMPLATE;
    static final String SOURCE_DOC_SERVICE_PATH =
            PROJECT_ITERATION_SERVICE_PATH + "/r";

    // file-stream service paths:
    static final String SOURCE_FILE_SERVICE_PATH =
            "/proj/{projectSlug}/ver/{versionSlug}/document/source";
    static final String TRANSLATED_FILE_SERVICE_PATH =
            "/proj/{projectSlug}/ver/{versionSlug}/document/trans/{localeId}";

    // other service paths:
    static final String ASYNC_SERVICE_PATH =
            "/async";
    static final String FILE_SERVICE_PATH =
            "/file";
    static final String JOB_STATUS_SERVICE_PATH =
            "/job/{jobId}";
}
