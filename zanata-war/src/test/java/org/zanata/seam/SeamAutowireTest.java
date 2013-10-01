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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.dao.ProjectDAO;
import org.zanata.seam.test.CyclicParentComponent;
import org.zanata.seam.test.TestComponent;
import org.zanata.service.CopyTransService;
import org.zanata.service.impl.CopyTransServiceImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

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

    @BeforeTest
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
            expectedExceptionsMessageRegExp = "Could not auto-wire component of type org.zanata.seam.test.UnbuildableTestComponent. No no-args constructor.")
    public
            void nonResolvableNotAllowed() {
        SeamAutowire.instance().reset().autowire(TestComponent.class);
    }

    @Test
    public void nonResolvableAllowed() {
        TestComponent test =
                SeamAutowire.instance().ignoreNonResolvable()
                        .autowire(TestComponent.class);

        assertThat(test.getUnbuildableTestComponent(), nullValue());
    }

    @Test
    public void postConstructInvoked() {
        TestComponent test =
                SeamAutowire.instance().ignoreNonResolvable()
                        .autowire(TestComponent.class);

        assertThat(test.isPostConstructInvoked(), is(true));
    }

    @Test
    public void cyclesAllowed() {
        CyclicParentComponent parent =
                SeamAutowire.instance().reset().allowCycles()
                        .autowire(CyclicParentComponent.class);

        assertThat(parent.getCyclicChildComponent(), notNullValue());
        assertThat(parent.getCyclicChildComponent().getCyclicParentComponent(),
                notNullValue());
    }

    @Test(
            expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "Recursive dependency: unable to inject cyclicChildComponent into component of type org.zanata.seam.test.CyclicParentComponent")
    public
            void cyclesNotAllowed() {
        SeamAutowire.instance().reset().autowire(CyclicParentComponent.class);
    }
}
