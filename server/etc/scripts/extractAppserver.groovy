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

import java.util.zip.*

// NB: project is a MavenProject
// http://maven.apache.org/ref/3-LATEST/maven-core/apidocs/org/apache/maven/project/MavenProject.html

String downloadDir = project.properties.get('download.dir') // ~/Downloads
String cargoExtractDir = project.properties.get('cargo.extract.dir') // target/cargo/installs
String url = project.properties.get('cargo.installation') // http://example.com/jbosseap7.zip

if (downloadDir == null || cargoExtractDir == null || url == null) {
    throw new Exception('Missing appserver properties.  Please invoke mvn with -Dappserver=jbosseap6 or -Dappserver=wildfly8')
}

String filename = url.substring(url.lastIndexOf('/')+1)
String filePath = "${downloadDir}/${filename}"
String basename = filename.substring(0, filename.lastIndexOf('.'))
String extractDir = "${cargoExtractDir}/${basename}"

new File(downloadDir).mkdirs()
download(url, filePath)
unzip(filePath, extractDir)

def files = new File(extractDir).listFiles()
if (files.length != 1) {
    throw new Exception('zip should contain exactly one top-level dir; see ' + extractDir)
}
def topLevelDir = files[0].path

project.properties.put('appserver.home', topLevelDir)


// download url to file, skip if file exists
static void download(String url, String filePath) {
  def file = new File(filePath)
  // TODO check file length against Content-Length header
  if (!file.exists() || file.length() == 0) {
    println "Downloading from $url:"
    file.withOutputStream { out ->
      new URL(url).withInputStream { from ->
        out << from;
      }
    }
  }
}

static void unzip(String zipFileName, String outputDir) {
  println "Extracting zip file $zipFileName:"
  new ZipFile(new File(zipFileName)).withCloseable { zip ->
    zip.entries().each { entry ->
      if (!entry.isDirectory()) {
        def file = new File(outputDir + File.separator + entry.name)
        file.parentFile.mkdirs()
        file.withOutputStream { out ->
          zip.getInputStream(entry).withStream { from ->
            out << from;
          }
        }
      }
    }
  }
}
