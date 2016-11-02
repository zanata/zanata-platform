/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata.arquillian;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Condition;
import org.junit.Assume;
import org.junit.Test;

/**
 * To run this test by itself in Maven, try this:
 * mvn test -pl :zanata-war -am -Dtest=DeploymentsSanityTest -DfailIfNoTests=false -Danimal.sniffer.skip -Dnogwt -Dskip.npm -DexcludeFrontend
 * NB: don't use Maven's -rf option, because it uses the local Maven repository, not the code in the modules' target directories.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class DeploymentsSanityTest {
    @Test
    public void testSanity() {
        String classpath = System.getProperty("java.class.path");
//        System.out.println(classpath.replaceAll(":", "\n"));

        // This is just a bellwether, representing Zanata classes which should be in the Maven reactor
        String apiClasses = "/zanata-common-api/target/classes".replaceAll("/", File.separator);
        // This test won't work when building zanata-war by itself (eg mvn test -pl :zanata-war or mvn test -rf :zanata-war)
        Assume.assumeThat("classpath contains reactor dependencies", classpath, containsString(apiClasses));
        System.out.println("resolving dependencies:");
        List<File> depList = Arrays.asList(Deployments.runtimeAndTestDependenciesFromPom());
        System.out.println("dependency count: " + depList.size());
        System.out.println("zanata dependencies: ");
        depList.stream().filter(f -> f.getPath().contains("zanata")).forEach(System.out::println);
        assertThat(depList.size()).isGreaterThan(200);
        // most zanata dependencies (except assets, frontend, api-compat) should come from reactor (/tmp or similar)
        assertThat(depList)
                .has(new Condition<>(
                        list -> list.stream().anyMatch(
                                f -> f.getPath().matches(".*te?mp.*zanata.*")),
                        "contains some zanata reactor dependencies (under tmp)"));
        assertThat(depList).doesNotHave(new Condition<>(list -> list.stream().anyMatch(f -> f.getName().contains("javamelody")), "javamelody"));
    }
}
