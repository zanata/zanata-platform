/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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

String getPropertyValue(String name) {
    def value = session.userProperties.get(name)
    if (value) return value //property was defined from command line e.g.: -DpropertyName=value
    return project.properties.get(name)
}

String appServerHome = getPropertyValue('appserver.home')
if (appServerHome == null) {
  throw new Exception('Missing appserver properties.  Please invoke mvn with -Dappserver=jbosseap6/wildfly8 or -Dappserver.home=jboss_home_dir')
}

// NB: project is a MavenProject
// http://maven.apache.org/ref/3-LATEST/maven-core/apidocs/org/apache/maven/project/MavenProject.html

def groovyAll = project.artifactMap.get('org.codehaus.groovy:groovy-all')
if (!groovyAll) throw new Exception("Please add groovy-all as a dependency")

String scriptName = 'zanataConfigTest.groovy'
String zanataConfigScript = new File("${project.basedir}/../etc/scripts/$scriptName").canonicalPath

def args = [
        'java',
        '-Djava.util.logging.manager=org.jboss.logmanager.LogManager',
        '-jar', "$appServerHome/jboss-modules.jar",
        '-modulepath', "$appServerHome/modules",
        '-dependencies', 'org.jboss.as.cli,org.apache.commons.cli,org.jboss.logmanager',
        '-classpath', groovyAll.file,
        'groovy.ui.GroovyMain',
        zanataConfigScript
//        ,'-v'
]

boolean runningArquillian = project.artifactId != 'functional-test'
if (runningArquillian) args.add('--datasource')

println "Executing $scriptName"
//println args

// 'as String[]' coerces any GStrings and Files to Strings
def processBuilder = new ProcessBuilder(args as String[])
processBuilder.environment().put('JBOSS_HOME', appServerHome)
def resultCode = processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT).redirectError(ProcessBuilder.Redirect.INHERIT).start().waitFor()
if (resultCode) {
  throw new Exception("$scriptName failed with exit code $resultCode")
} else {
  println "$scriptName completed successfully"
}
