import java.security.MessageDigest
import org.apache.commons.io.FileUtils

def messageDigest = MessageDigest.getInstance("MD5")
messageDigest.update(project.build.directory.bytes);
String md5Hex = messageDigest.digest().encodeHex()

File mysqlDir = new File(System.getProperty('java.io.tmpdir'), 'mysql-data-'+md5Hex)
project.properties['mysql.data'] = mysqlDir.absolutePath

if (project.properties['delete'] == 'true') {
    log.info('Deleting mysql.data directory: ' + mysqlDir)
    FileUtils.deleteDirectory(mysqlDir)
} else {
    log.info('Preparing mysql.data directory: ' + mysqlDir)
    mysqlDir.mkdir()
    FileUtils.cleanDirectory(mysqlDir)
}
