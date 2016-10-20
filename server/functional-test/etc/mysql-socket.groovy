// This script sets the Maven project property 'mysql.socket'
// to a short path under /tmp (derived from the project build directory)
// and optionally deletes the socket file if it exists.

import java.security.MessageDigest

def messageDigest = MessageDigest.getInstance("MD5")
messageDigest.update(project.build.directory.bytes);
String md5Hex = messageDigest.digest().encodeHex()

File mysqlSocket = new File(System.getProperty('java.io.tmpdir'), 'mysql-'+md5Hex+'.socket')
project.properties['mysql.socket'] = mysqlSocket.absolutePath

if (project.properties['delete'] == 'true') {
    log.info('Deleting mysql.socket file: ' + mysqlSocket)
    mysqlSocket.delete();
}
