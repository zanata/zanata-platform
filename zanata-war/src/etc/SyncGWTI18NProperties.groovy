import com.google.common.io.Files

// This script will be executed by gmaven plugin.
// Although gmaven supports inline scripting in pom, it will escape / in the
// regex and the script therefore won't work.
log.info "===== Synchronize GWT generated properties files ====="

def baseDir = new File(project.build.directory + "/gwt-extra/webtrans")

assert baseDir.isDirectory()

def nameFilter = { dir, name ->
    name.endsWith(".properties")
} as FilenameFilter

def properties = baseDir.listFiles(nameFilter)


if (!properties) {
    log.info "no properties found. quit."
    return
}
// scrip off the file name part to get packge name
def packageName = properties[0].name.replaceAll(/\.\w+\.properties/, "")
def packagePath = packageName.replaceAll(/\./, "/")
def destDir = new File(pom.basedir.absolutePath + "/src/main/resources/$packagePath")
destDir.mkdirs()

int sourceCount = 0
int targetCount = 0

properties.each {
    def fileName = (it.name - "$packageName.")
    def destFile = new File(destDir, fileName)
    if (it.name.endsWith("_default.properties")) {
        log.debug "   * found source: $it.name"
        // we always copy over source file
        log.debug "     copy over to: $destFile"
        // copy the file with _default to make GWT happy
        Files.copy(it, destFile)
        sourceCount++
    } else {
        log.debug "   * found target: $it.name"
        // we ALWAYS copy generated target skeleton to make sure target is in sync if source has changed.
        // It rely on zanata's merge auto feature.
        // Merge type import will override everything!!
        log.debug "     copy over to :$destFile"
        Files.copy(it, destFile);
        targetCount++
    }
}

log.info "Copied $sourceCount source(s) and $targetCount target(s) in $baseDir"
log.info "===== Synchronize GWT generated properties files ====="
