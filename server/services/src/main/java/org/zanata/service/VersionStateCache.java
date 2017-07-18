/*
 *
 *  * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */

package org.zanata.service;

import org.zanata.common.LocaleId;
import org.zanata.events.DocStatsEvent;
import org.zanata.ui.model.statistic.WordStatistic;

import java.io.Serializable;

/**
 * Defines a Cache Service for project version states.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface VersionStateCache extends Serializable {

    /**
     * Informs the cache that a text flow has changed its state in a given
     * locale.
     *
     */
    void docStatsUpdated(DocStatsEvent event);

    WordStatistic getVersionStatistics(Long projectIterationId,
            LocaleId localeId);

    /**
     * Clears all caches for a single project version (all locales).
     * @param versionId All cached stats for this version will be cleared.
     */
    void clearVersionStatsCache(Long versionId);
}
