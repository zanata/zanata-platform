/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
package org.zanata.test;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.jglue.cdiunit.CdiRunner;
import org.jglue.cdiunit.ContextController;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.reflect.Modifier.isFinal;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class CdiUnitRunner extends CdiRunner {
    private static final Logger log = LoggerFactory.getLogger(CdiUnitRunner.class);

    public CdiUnitRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        boolean isFinal = isFinal(clazz.getModifiers());
        if (isFinal) {
            log.warn(clazz + " is final. This may cause problems " +
                    "with Mockito or CDI. " +
                    "Check kotlin-allopen configuration.");
        }
    }

    @Override
    public void run(RunNotifier notifier) {
//        ProjectStage oldStage = ProjectStageProducer.getInstance().getProjectStage();
        // Tell DeltaSpike to give more warning messages
        ProjectStageProducer.setProjectStage(ProjectStage.UnitTest);
        try {
            super.run(notifier);
        } finally {
//            ProjectStageProducer.setProjectStage(oldStage);
            try {
                ContextController contextController = BeanProvider
                        .getContextualReference(ContextController.class);
                // ensure we close request and session scope
                contextController.closeRequest();
                contextController.closeSession();
            } catch (IllegalStateException e) {
                // ignored when there is no context
            }
        }
    }
}
