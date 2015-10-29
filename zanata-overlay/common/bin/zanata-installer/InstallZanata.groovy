import groovy.xml.XmlUtil

import java.nio.file.Files

/**
 * Zanata installer script.
 * Downloads Zanata and configures certain parts of the application for first
 * use.
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
Properties installerProps = new Properties()
File propertiesFile = new File('installer.properties')
propertiesFile.withInputStream {
    installerProps.load(it)
}
def ZANATA_VERSION = installerProps['zanata.version']
final JBOSS_CFG_DIR = "../../standalone/configuration"
final CUSTOM_STANDALONE_XML_LOC = "${JBOSS_CFG_DIR}/standalone-zanata.xml"
final ORIGINAL_STANDALONE_XML_LOC = "${JBOSS_CFG_DIR}/standalone.xml"
final WAR_FILE_LOC = "../../standalone/deployments/zanata.war"

boolean askYesNoQuestion(String question) {
    def reader = new BufferedReader(new InputStreamReader(System.in))
    println ""
    println question
    def answer = reader.readLine()
    return answer.trim().equalsIgnoreCase("y")
}

println "====================================================="
println " Welcome to the Zanata Installer. This will install"
println " Zanata ${ZANATA_VERSION} onto your JBoss or Wildfly"
println ""
println " Note: This installer will change your standalone.xml"
println " file."
println "====================================================="

def downloadWarFile = true
if( new File(WAR_FILE_LOC).exists() ) {
    downloadWarFile = askYesNoQuestion("It seems Zanata is already installed. Do you wish to download it again? (y/N)")
} else {
    downloadWarFile = askYesNoQuestion("Do you wish to download the Zanata package (zanata.war)? (y/N)")
}

def dbHost
def dbPort
def dbSchema
def dbUsername
def dbPassword

// Download the war file
if( downloadWarFile ) {
    def fileUrl = "https://github.com/zanata/zanata-server/releases/download/server-${ZANATA_VERSION}/zanata-war-${ZANATA_VERSION}.war"
    println "Downloading $fileUrl"
    println "This might take a few minutes."

    try {
        def file = new FileOutputStream(WAR_FILE_LOC)
        def out = new BufferedOutputStream(file)
        out << new URL(fileUrl).openStream()
        out.close()
        println "Downloaded Zanata ${ZANATA_VERSION}..."
    } catch (Exception ex) {
        println "Could not download zanata-war-${ZANATA_VERSION}.war"
    }
}

// Move the original standalone and copy the custom one
def xmlFile = new File(ORIGINAL_STANDALONE_XML_LOC)
def xmlBackupFile = new File("${JBOSS_CFG_DIR}/standalone.xml.original")
def customXmlFile = new File(CUSTOM_STANDALONE_XML_LOC)

// If the installer has already been ran
def modifyStandaloneXml = true
if(xmlBackupFile.exists()) {
    modifyStandaloneXml = askYesNoQuestion("It looks like you have already run this installer. " +
        "If you continue, your current standalone.xml file will be backed up and modified. " +
        "Do you wish to continue? (y/N)")
}

if(modifyStandaloneXml) {
    println "Please provide the following information:"
    println "(If blank, the default value will apply)"
    println ""

    def reader = new BufferedReader(new InputStreamReader(System.in))

    println "Database Host (default: 'localhost'):"
    dbHost = reader.readLine()
    if(dbHost.isEmpty()) dbHost = 'localhost'

    println "Database port (default: '3306'):"
    dbPort = reader.readLine()
    if(dbPort.isEmpty()) dbPort = '3306'

    println "Database schema (default: 'zanata'):"
    dbSchema = reader.readLine()
    if(dbSchema.isEmpty()) dbSchema = 'zanata'

    println "Database Username (default: ''):"
    dbUsername = reader.readLine()

    println "Database Password (default: ''):"
    dbPassword = reader.readLine()

    xmlFile.renameTo(xmlBackupFile) // backup original file
    Files.copy(customXmlFile.toPath(), xmlFile.toPath()) // standalone-zanata.xml -> standalone.xml

    // Update zanata datasource
    def dsXml = new XmlParser().parse(CUSTOM_STANDALONE_XML_LOC)

    def zanataDs =
        dsXml.profile.subsystem.
            find { s -> s.datasources }.datasources.datasource
            .find {
            it.@'jndi-name' == "java:jboss/datasources/zanataDatasource"
        }

    zanataDs.'connection-url'.replaceNode {
        'connection-url'(
            "jdbc:mysql://${dbHost}:${dbPort}/${dbSchema}?characterEncoding=UTF-8")
    }
    zanataDs.security.replaceNode { node ->
        security {
            'user-name'(dbUsername)
            password(dbPassword)
        }
    }

    XmlUtil.serialize(dsXml, new FileOutputStream(CUSTOM_STANDALONE_XML_LOC))
    println "Configured zanata in $CUSTOM_STANDALONE_XML_LOC"
}
println "Done!"
