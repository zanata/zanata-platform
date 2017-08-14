/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.shared.rpc;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LuceneQuery {
    /**
     * Used by TM and glossary lucene search.
     * limit to 16384 char for search query
     *
     * Usage:
     * {@link org.zanata.service.impl.TranslationMemoryServiceImpl}
     * {@link org.zanata.dao.GlossaryDAO}
     * {@link org.zanata.webtrans.client.view.GlossaryView}
     * {@link org.zanata.webtrans.client.view.TransMemoryView}
     */
    public static final int QUERY_MAX_LENGTH = 16384;
}
