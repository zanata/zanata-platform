/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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
package org.zanata.service.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.service.TranslationFinder;
import org.zanata.service.ValidationService;
import org.zanata.service.VersionStateCache;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Named("copyTransWorkFactory")
@javax.enterprise.context.Dependent
public class CopyTransWorkFactory {

    // Inject textFlowTargetDAO for Hibernate-based query
    // Inject translationMemoryServiceImpl for Hibernate Search query
    @Inject
//    @Inject
    private TranslationFinder translationFinder;

    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;

    @Inject
    private ValidationService validationServiceImpl;

    @Inject
    private VersionStateCache versionStateCacheImpl;

    public CopyTransWork createCopyTransWork(HLocale targetLocale,
            HCopyTransOptions options, HDocument document,
            boolean requireTranslationReview, List<HTextFlow> copyTargets) {
        return new CopyTransWork(options, document, copyTargets, targetLocale,
                requireTranslationReview,
                translationFinder, textFlowTargetDAO,
                versionStateCacheImpl, validationServiceImpl);
    }

}
