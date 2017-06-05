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
package org.zanata.hibernate.search;

import org.hibernate.search.indexes.interceptor.EntityIndexingInterceptor;
import org.hibernate.search.indexes.interceptor.IndexingOverride;
import org.zanata.common.EntityStatus;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class EntityStatusIndexingInterceptor implements
        EntityIndexingInterceptor<HTextFlowTarget> {
    @Override
    public IndexingOverride onAdd(HTextFlowTarget hTextFlowTarget) {
        if (hTextFlowTarget.getTextFlow().isObsolete()) {
            return IndexingOverride.SKIP;
        }
        if (hTextFlowTarget.getTextFlow().getDocument().isObsolete()) {
            return IndexingOverride.SKIP;
        }
        HProjectIteration projectIteration =
                hTextFlowTarget.getTextFlow().getDocument()
                        .getProjectIteration();
        if (projectIteration.getStatus() == EntityStatus.OBSOLETE) {
            return IndexingOverride.SKIP;
        }
        if (projectIteration.getProject().getStatus() == EntityStatus.OBSOLETE) {
            return IndexingOverride.SKIP;
        }
        return IndexingOverride.APPLY_DEFAULT;
    }

    @Override
    public IndexingOverride onUpdate(HTextFlowTarget hTextFlowTarget) {
        if (hTextFlowTarget.getTextFlow().isObsolete()) {
            return IndexingOverride.REMOVE;
        }
        if (hTextFlowTarget.getTextFlow().getDocument().isObsolete()) {
            return IndexingOverride.REMOVE;
        }
        HProjectIteration projectIteration =
                hTextFlowTarget.getTextFlow().getDocument()
                        .getProjectIteration();
        if (projectIteration.getStatus() == EntityStatus.OBSOLETE) {
            return IndexingOverride.REMOVE;
        }
        if (projectIteration.getProject().getStatus() == EntityStatus.OBSOLETE) {
            return IndexingOverride.REMOVE;
        }
        return IndexingOverride.UPDATE;
    }

    @Override
    public IndexingOverride onDelete(HTextFlowTarget hTextFlowTarget) {
        return IndexingOverride.APPLY_DEFAULT;
    }

    @Override
    public IndexingOverride onCollectionUpdate(
            HTextFlowTarget hTextFlowTarget) {
        return onUpdate(hTextFlowTarget);
    }
}
