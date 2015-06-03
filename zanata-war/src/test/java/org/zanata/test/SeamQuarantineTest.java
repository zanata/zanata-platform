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

import com.binarytweed.test.Quarantine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.ZanataTest;
import org.zanata.seam.SeamAutowire;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Quarantine({ "org.jboss.seam", "org.zanata.test.SeamQuarantineTest" })
public class SeamQuarantineTest extends ZanataTest {
    @Test
    public void test1() throws ClassNotFoundException {
        // load Seam before test2 gets a chance to use SeamAutowire
        Class.forName("org.jboss.seam.contexts.Contexts");
    }
    @Test
    public void test2() {
        // Assuming:
        // 1. test1 runs before test2
        // 2. SeamAutowire not initialised by an earlier test
        // 3. not using ClassLoader isolation (QuarantiningRunner)
        // this test will fail with:
        // java.lang.ExceptionInInitializerError
        // Caused by: java.lang.RuntimeException: Problem rewiring Seam's
        // Contexts class
        // Caused by: javassist.CannotCompileException: by
        // java.lang.LinkageError: loader (instance of
        // sun/misc/Launcher$AppClassLoader): attempted  duplicate class
        // definition for name: "org/jboss/seam/contexts/Contexts"
        SeamAutowire.instance();
    }
}
