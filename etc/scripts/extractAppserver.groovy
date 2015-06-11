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

import groovy.util.AntBuilder

// NB: project is a MavenProject
// http://maven.apache.org/ref/3-LATEST/maven-core/apidocs/org/apache/maven/project/MavenProject.html

String downloadDir = project.properties.get('download.dir') // ~/Downloads
String cargoExtractDir = project.properties.get('cargo.extract.dir') // target/cargo/installs
String url = project.properties.get('cargo.installation') // http://example.com/jbosseap6.zip

String filename = url.substring(url.lastIndexOf('/')+1)
String filePath = "${downloadDir}/${filename}"
String basename = filename.substring(0, filename.lastIndexOf('.'))
String extractDir = "${cargoExtractDir}/${basename}"

def ant = new AntBuilder()
ant.mkdir(dir: downloadDir)
ant.get(src: url, dest: filePath, skipexisting: "true")
ant.unzip(src: filePath, dest:"${extractDir}")

def files = new File(extractDir).listFiles()
if (files.length != 1) {
    throw new Exception('zip should contain exactly one top-level dir; see ' + extractDir)
}
def topLevelDir = files[0].path

project.properties.put('appserver.home', topLevelDir)
