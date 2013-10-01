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

package org.zanata.service;

import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.model.TransMemoryQuery;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public interface TranslationMemoryQueryService {

    /**
     * Performs the specified TM query via Hibernate Search
     *
     * @param query
     * @param sourceLocale
     * @param maxResult
     *            number of results to return
     * @return
     * @throws ParseException
     */
    public List<Object[]> getSearchResult(TransMemoryQuery query,
            LocaleId sourceLocale, LocaleId targetLocale, int maxResult)
            throws ParseException;

}
