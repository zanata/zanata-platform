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
package org.zanata.util;

import java.util.List;

import org.zanata.model.Activity;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountActivationKey;
import org.zanata.model.HAccountResetPasswordKey;
import org.zanata.model.HDocument;
import org.zanata.model.HDocumentHistory;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HPerson;
import org.zanata.model.HPersonEmailValidationKey;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowHistory;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;
import org.zanata.model.HTextFlowTargetReviewComment;
import org.zanata.model.WebHook;
import org.zanata.model.po.HPoHeader;
import org.zanata.model.po.HPoTargetHeader;
import org.zanata.model.po.HPotEntryData;
import org.zanata.model.security.HCredentials;
import org.zanata.model.tm.TransMemory;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.tm.TransMemoryUnitVariant;

import com.google.common.collect.ImmutableList;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ZanataEntities {
    private static List<Class> entitiesForDelete;

    /**
     * TODO probably put this under test and make a test-jar of zanata-model
     *
     * @return a list of entity classes that is in order for deletion i.e. won't
     *         violate referential constraint.
     */
    public static List<Class> entitiesForRemoval() {
        if (entitiesForDelete != null) {
            return entitiesForDelete;
        }
        ImmutableList.Builder<Class> builder = ImmutableList.builder();
        // TMX
        builder.add(TransMemoryUnitVariant.class, TransMemoryUnit.class,
                TransMemory.class);
        builder.add(Activity.class);
        // glossary
        builder.add(HGlossaryTerm.class, HGlossaryEntry.class);
        // text flows and targets
        builder.add(HPoTargetHeader.class, HTextFlowTargetHistory.class,
                HTextFlowTargetReviewComment.class,
                HTextFlowTarget.class,
                HTextFlowHistory.class,
                HTextFlow.class);
        builder.add(HPotEntryData.class);
        // documents
        builder.add(HDocumentHistory.class, HDocument.class);
        builder.add(HPoHeader.class);
        // iteration group (references locales)
        builder.add(HIterationGroup.class);
        // locales
        builder.add(HLocaleMember.class, HLocale.class);
        // project
        builder.add(HProjectIteration.class, WebHook.class, HProject.class);
        // account
        builder.add(HAccountActivationKey.class, HCredentials.class,
                HPersonEmailValidationKey.class,
                HPerson.class,
                HAccountResetPasswordKey.class,
                HAccount.class);

        entitiesForDelete = builder.build();
        return entitiesForDelete;
    }
}
