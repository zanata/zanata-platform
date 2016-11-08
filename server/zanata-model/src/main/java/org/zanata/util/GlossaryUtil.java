/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.util;

import org.apache.commons.lang.StringUtils;
import org.zanata.common.LocaleId;
import org.zanata.rest.service.GlossaryResource;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class GlossaryUtil {

    private final static String SEPARATOR = "\u0000";

    private final static String QUALIFIED_NAME_SEPARATOR = "/";

    public static String GLOBAL_QUALIFIED_NAME =
            GlossaryResource.GLOBAL_QUALIFIED_NAME;

    /**
     * Generate contentHash for HGlossaryEntry from
     *
     * {@link org.zanata.model.HGlossaryEntry#srcLocale}
     * {@link org.zanata.model.HGlossaryEntry} source content
     * {@link org.zanata.model.HGlossaryEntry#pos}
     * {@link org.zanata.model.HGlossaryEntry#description}
     */
    public static String generateHash(LocaleId locale, String content, String pos,
        String description) {
        String hashBase = locale + SEPARATOR + content + SEPARATOR + pos +
            SEPARATOR + description;
        return HashUtil.generateHash(hashBase);
    }

    /**
     * Generate qualifiedName from namespace and name.
     * e.g project/zanata, global/default
     *
     * {@link QUALIFIED_NAME_SEPARATOR} at the end of namespace will be removed.
     */
    public static String generateQualifiedName(String namespace, String name) {
        String processedNamespace =
                StringUtils.removeEnd(namespace, QUALIFIED_NAME_SEPARATOR);
        return processedNamespace + QUALIFIED_NAME_SEPARATOR + name;
    }
}
