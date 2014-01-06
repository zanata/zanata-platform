import com.google.common.io.Files

// This script will copy GWT generated *_default.properties source file to
// follow java convention. i.e. without the _default in file name.
// It's used in zanata.xml as command hook
File pomBase = pom.basedir
def baseDir = new File(pomBase.absolutePath + "/src/main/resources/org/zanata/webtrans/client/resources/")

assert baseDir.isDirectory()

def nameFilter = { dir, name ->
    name.endsWith("_default.properties")
} as FilenameFilter

def properties = baseDir.listFiles(nameFilter)


if (!properties) {
    log.info "no *_default.properties found. quit."
    return
}

properties.each {
    // we need a no locale file name for java properties file convention
    def noLocaleFileName = it.name.replace("_default", "")
    def noLocaleDestFile = new File(baseDir, noLocaleFileName)
    log.debug "   copy $it.name to: $noLocaleDestFile"
    Files.copy(it, noLocaleDestFile)
}