/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.rest.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.validator.SlugValidator;
import org.zanata.rest.RestUtil;

/**
 * Service that provides static services to build, parse and interpret Zanata
 * resource identifier paths (Z-paths)
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @since 2.0.0
 */
@Named("zPathService")
@javax.enterprise.context.Dependent

public class ZPathService {
    /*
     * Public ZPaths. Used for rest resource path declaration.
     */
    public static final String PROJECT_ZPATH = "/proj/"
            + RestConstants.SLUG_PATTERN;

    /*
     * Private ZPaths. Mainly used for generation.
     */
    private static final String PROJECT_ZPATH_PRIVATE = "/projects/p/{0}";
    private static final String PROJECT_ITER_ZPATH_PRIVATE =
            PROJECT_ZPATH_PRIVATE + "/iterations/i/{1}";
    private static final String DOCUMENT_ZPATH_PRIVATE =
            PROJECT_ITER_ZPATH_PRIVATE + "/r/{2}";

    /*
     * Internal utilities based on the public ZPaths
     */
    private static final Pattern PROJECT_ZPATH_PATTERN = Pattern
            .compile(PROJECT_ZPATH);

    @Inject
    private ProjectDAO projectDAO;

    public HProject resolveProject(String zPath) {
        Matcher projMatcher = PROJECT_ZPATH_PATTERN.matcher(zPath);
        if (projMatcher.matches()) {
            String projectSlug = projMatcher.group(1); // Group 1, project slug
            return projectDAO.getBySlug(projectSlug);
        } else {
            return null;
        }
    }

    public String generatePathForProject(HProject project) {
        MessageFormat mssgFormat = new MessageFormat(PROJECT_ZPATH_PRIVATE);
        return mssgFormat.format(project.getSlug());
    }

    public String generatePathForProjectIteration(HProjectIteration iteration) {
        MessageFormat mssgFormat =
                new MessageFormat(PROJECT_ITER_ZPATH_PRIVATE);
        return mssgFormat.format(new Object[] {
                iteration.getProject().getSlug(), iteration.getSlug() });
    }

    public String generatePathForDocument(HDocument document) {
        String docIdNoSlash = null;
        try {
            docIdNoSlash =
                    URLEncoder.encode(RestUtil.convertToDocumentURIId(document
                            .getDocId()), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        MessageFormat mssgFormat = new MessageFormat(DOCUMENT_ZPATH_PRIVATE);
        return mssgFormat.format(new Object[] {
                document.getProjectIteration().getProject().getSlug(),
                document.getProjectIteration().getSlug(), docIdNoSlash });
    }

    public Object resolve(String zPath) {
        Matcher projMatcher = PROJECT_ZPATH_PATTERN.matcher(zPath);
        if (projMatcher.matches()) {
            return resolveProject(zPath);
        }

        return null;
    }
}
