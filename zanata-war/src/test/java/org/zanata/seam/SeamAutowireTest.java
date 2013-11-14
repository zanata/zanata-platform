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
package org.zanata.seam;

import org.hamcrest.Matchers;
import org.jboss.seam.Component;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.dao.ProjectDAO;
import org.zanata.seam.test.ComponentWithNonRequiredChild;
import org.zanata.seam.test.ComponentWithRequiredAutoCreateChild;
import org.zanata.seam.test.ComponentWithRequiredBrokenChild;
import org.zanata.seam.test.ComponentWithRequiredNoCreateChild;
import org.zanata.seam.test.ComponentWithChildCycle;
import org.zanata.seam.test.ComponentWithNonRequiredBrokenChild;
import org.zanata.seam.test.ComponentWithRequiredCreateChild;
import org.zanata.service.CopyTransService;
import org.zanata.service.impl.CopyTransServiceImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for the {@link SeamAutowire} component. Also useful as a template for
 * other Autowire tests.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class SeamAutowireTest extends ZanataDbunitJpaTest {
    @Override
    protected void prepareDBUnitOperations() {
    }

    @BeforeMethod
    public void resetSeamAutowire() {
        SeamAutowire.instance().reset();
    }

    @Test
    public void autowireSession() {
        ProjectDAO dao =
                SeamAutowire.instance().ignoreNonResolvable()
                        .use("session", getSession())
                        .autowire(ProjectDAO.class);

        int t = dao.getTotalProjectCount();
        // System.out.println("Total Projects: " + t);
    }

    @Test
    public void autowireProvided() {
        ProjectDAO dao = new ProjectDAO();
        dao =
                SeamAutowire.instance().ignoreNonResolvable()
                        .use("session", getSession()).autowire(dao);

        int t = dao.getTotalProjectCount();
        // System.out.println("Total Projects: " + t);
    }

    @Test
    public void interfaceImplementations() {
        SeamAutowire.instance().ignoreNonResolvable()
                .autowire(CopyTransServiceImpl.class);

        CopyTransService copyTrans =
                SeamAutowire.instance().autowire(CopyTransService.class);
        assertThat(copyTrans, Matchers.notNullValue());
    }

    @Test
    public void testComponentInvocation() {
        SeamAutowire.instance().use("component", "This is the component!");

        String val = (String) Component.getInstance("component");

        assertThat(val, is("This is the component!"));
    }

    @Test(
            expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "Could not auto-wire component of type .*. No no-args constructor.")
    public void brokenChild() {
        SeamAutowire.instance().autowire(ComponentWithNonRequiredBrokenChild.class);
    }

    @Test
    public void ignoreBrokenChild() {
        ComponentWithNonRequiredBrokenChild test =
                SeamAutowire.instance().ignoreNonResolvable()
                        .autowire(ComponentWithNonRequiredBrokenChild.class);
        assertThat(test.getUnbuildableTestComponent(), nullValue());
    }

    @Test
    public void postConstructInvoked() {
        ComponentWithRequiredAutoCreateChild test =
                SeamAutowire.instance()
                        .autowire(ComponentWithRequiredAutoCreateChild.class);

        assertThat(test.isPostConstructInvoked(), is(true));
    }

    @Test(dataProvider = "workingComponents")
    public void buildWorkingComponents(Class<?> componentClass) {
        Object comp = SeamAutowire.instance().autowire(componentClass);
        assertThat(comp, notNullValue());
    }

    @DataProvider(name = "workingComponents")
    public Object[][] workingComponents() {
        return new Object[][] {
                {ComponentWithRequiredCreateChild.class},
                {ComponentWithRequiredAutoCreateChild.class},
                {ComponentWithNonRequiredChild.class}
        };
    }

    @Test(dataProvider = "brokenComponents", expectedExceptions=RuntimeException.class)
    public void buildBrokenComponents(Class<?> componentClass) {
        SeamAutowire.instance().autowire(componentClass);
    }

    @DataProvider(name = "brokenComponents")
    public Object[][] brokenComponents() {
        return new Object[][] {
                {ComponentWithRequiredBrokenChild.class},
                {ComponentWithRequiredNoCreateChild.class}
        };
    }

    @Test
    public void cyclesAllowed() {
        ComponentWithChildCycle parent =
                SeamAutowire.instance().allowCycles()
                        .autowire(ComponentWithChildCycle.class);

        assertThat(parent.getCyclicChildComponent(), notNullValue());
        assertThat(parent.getCyclicChildComponent().getCyclicParentComponent(),
                notNullValue());
    }

    @Test(
            expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "Recursive dependency: unable to inject .* into component of type .*")
    public
            void cyclesNotAllowed() {
        SeamAutowire.instance().autowire(ComponentWithChildCycle.class);
    }
}
