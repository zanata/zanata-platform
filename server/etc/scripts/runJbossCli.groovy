import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

import static java.nio.file.attribute.PosixFilePermission.*


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
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
String appServerHome = project.properties.get('appserver.home')

if (appServerHome == null) {
    throw new Exception('Missing appserver properties.  Please invoke mvn with -Dappserver=jbosseap6 or -Dappserver=wildfly8')
}

File baseDir = new File(project.basedir as String)

// basedir will be either functional-test module or zanata-war module
File commonCLIScript = new File(baseDir.getParentFile(), "etc/scripts/zanata-config-test-common.cli")
File arqTestCLIScript = new File(baseDir.getParentFile(), "etc/scripts/zanata-config-arq-test.cli")

File serverHome = new File(appServerHome)

String ext = isWindows() ? 'bat' : 'sh'

File jbossCLI = new File(serverHome, "bin/jboss-cli.$ext")

// need to set file to executable
Set<PosixFilePermission> permissions = [GROUP_READ, GROUP_EXECUTE, OTHERS_EXECUTE, OTHERS_READ, OWNER_EXECUTE, OWNER_READ, OWNER_WRITE]
Files.setPosixFilePermissions(jbossCLI.toPath(), permissions)

runJBossCLI(jbossCLI, commonCLIScript)
if (project.artifactId == 'zanata-war') {
    // we are running arquillian test. need to run extra script
    runJBossCLI(jbossCLI, arqTestCLIScript)
}

static void runJBossCLI(File jbossCLI, File cliScript) {
    String cmd = "$jbossCLI.absolutePath --file=$cliScript.absolutePath"

    println "about to execute $cmd"

    def sout = new StringBuilder(), serr = new StringBuilder()
    def proc = cmd.execute()
    proc.consumeProcessOutput(sout, serr)
// give it 30 seconds
    proc.waitForOrKill(30000)
    println "jboss cli execution:stdout>  $sout"
    if (!serr.toString().isEmpty()) {
        println "jboss cli execution:stderr>  $serr"
    }
    def exitValue = proc.exitValue()
    assert exitValue == 0 : "jboss cli execution returned non-zero code:$exitValue"
}


static boolean isWindows() {
    System.properties.getProperty('os.name').toLowerCase().contains('windows')
}
