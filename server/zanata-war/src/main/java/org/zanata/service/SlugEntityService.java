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
package org.zanata.service;

import org.zanata.model.SlugEntityBase;

/**
 * Provides common services related to the slug based entities (
 * {@link org.zanata.model.SlugEntityBase}).
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface SlugEntityService {
    /**
     * Determines if a given slug is available in a class.
     *
     * @param slug
     *            The slug to check
     * @param cls
     *            The class to verify the slug against.
     * @return True if the slug is not in use by any other elements of cls.
     *         False, otherwise.
     */
    boolean isSlugAvailable(String slug, Class<? extends SlugEntityBase> cls);

    /**
     * Determines if a given slug is available for a project iteration.
     *
     * @param slug
     *            The slug to check
     * @param projectSlug
     *            The project slug.
     * @return True if the slug is not in use by any other project iteration in
     *         the given project. False, otherwise.
     */
    boolean isProjectIterationSlugAvailable(String slug, String projectSlug);
}
