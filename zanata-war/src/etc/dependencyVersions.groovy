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

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
def props = new Properties()
// NB: project is a MavenProject
// http://maven.apache.org/ref/3-LATEST/maven-core/apidocs/org/apache/maven/project/MavenProject.html
project.artifacts.each { a ->
    def coord = a.groupId + ":" + a.artifactId + ":" + a.type + (a.classifier ? ":" + a.classifier : "")
    // Make version available to the build:
    project.properties.put "version." + coord, a.version
    // This can be expanded to other deps if required:
    if (a.groupId.startsWith('org.webjars')) {
        // Make webjar version available at runtime:
        props.put coord, a.version
    }
}

// NB: this has a corresponding resource directory
// declaration above.
def genDir = new File(project.build.directory,
    'generated-resources/deps')
genDir.mkdirs()
new File(genDir, 'dependencies.properties').withWriter { out ->
    props.store out, 'Zanata dependency versions'
}
