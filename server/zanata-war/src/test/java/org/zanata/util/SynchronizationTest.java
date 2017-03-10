/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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

import static org.assertj.core.api.Assertions.assertThat;
import java.util.logging.LogManager;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.jodah.concurrentunit.ConcurrentTestCase;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.zanata.test.CdiUnitRunner;

@AdditionalClasses({ SynchronizationInterceptor.class })
@SupportDeltaspikeCore
@RunWith(CdiUnitRunner.class)
public class SynchronizationTest extends ConcurrentTestCase {

    static {
        // redirect JUL to slf4j
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
        // Tell DeltaSpike to give more warning messages
        ProjectStageProducer.setProjectStage(ProjectStage.UnitTest);
    }

    @ApplicationScoped
    @Synchronized
    public static class SyncClassBean {
        private static final org.slf4j.Logger log =
                org.slf4j.LoggerFactory.getLogger(SyncClassBean.class);

        volatile boolean executing;

        public void blockingMethod() {
            assertThat(executing).isFalse();
            executing = true;
            log.debug("starting to block");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.debug("finished blocking");
            executing = false;
        }
    }

    @Inject
    private SyncClassBean syncClassBean;

    @Test
    public void syncClassBean() throws Exception {
        new Thread(() -> {
            syncClassBean.blockingMethod();
            resume();
        }).start();
        new Thread(() -> {
            syncClassBean.blockingMethod();
            resume();
        }).start();
        await(1000, 2);
    }
}
