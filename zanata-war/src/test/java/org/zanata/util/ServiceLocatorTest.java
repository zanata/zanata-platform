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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.zanata.seam.SeamAutowire;
import org.zanata.test.CdiUnitRunner;

@SupportDeltaspikeCore
@InRequestScope
@RunWith(CdiUnitRunner.class)
public class ServiceLocatorTest {

    static {
        SeamAutowire.instance();
        // redirect JUL to slf4j
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();

        // Tell DeltaSpike to give more warning messages
        ProjectStageProducer.getInstance().setProjectStage(ProjectStage.UnitTest);
    }

    @Named
    public static class DependentTestBean {
    }

    @Named("myNamedBean")
    @RequestScoped
    public static class ExplicitlyNamedBean {
    }

    @Named
    @RequestScoped
    public static class NamedBean {
    }

    @Inject
    private DependentTestBean dependentBean;

    @Inject
    private ExplicitlyNamedBean myNamedBean;

    @Inject
    private NamedBean namedBean;

//    @Inject
    private IServiceLocator locator = ServiceLocator.instance();

    @Test
    public void dependentBeanByClass() throws Exception {
        DependentTestBean got = locator.getInstance(DependentTestBean.class);
        assertThat(got).isNotEqualTo(dependentBean);
        assertThat(got.getClass()).isEqualTo(dependentBean.getClass());
    }

    @Test
    public void dependentBeanByName() throws Exception {
        DependentTestBean got =
                locator.getInstance("dependentTestBean", DependentTestBean.class);
        assertThat(got).isNotEqualTo(dependentBean);
        assertThat(got.getClass()).isEqualTo(dependentBean.getClass());
    }

    @Test
    public void explicitlyNamedBeanByClass() throws Exception {
        assertThat(locator.getInstance(ExplicitlyNamedBean.class)).isEqualTo(myNamedBean);
    }

    @Test
    public void explicitlyNamedBeanByName() throws Exception {
        assertThat(locator.getInstance("myNamedBean", ExplicitlyNamedBean.class)).isEqualTo(myNamedBean);
    }

    @Test
    public void namedBeanByClass() throws Exception {
        assertThat(locator.getInstance(NamedBean.class)).isEqualTo(namedBean);
    }

    @Test
    public void namedBeanByName() throws Exception {
        assertThat(locator.getInstance("namedBean", NamedBean.class)).isEqualTo(namedBean);
    }

}
