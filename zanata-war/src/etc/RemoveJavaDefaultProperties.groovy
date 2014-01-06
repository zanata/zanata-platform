// This script will remove java default locale properties file so that GWT can compile.
// GWT only expect *_default.properties as default locale file.
// It's used in zanata.xml as command hook

File pomBase = pom.basedir
def baseDir = new File(pomBase.absolutePath + "/src/main/resources/org/zanata/webtrans/client/resources/")

assert baseDir.isDirectory()

// find properties file without underscore
def nameFilter = { dir, name ->
    !name.contains("_")
} as FilenameFilter

def properties = baseDir.listFiles(nameFilter)

properties.each {
    it.delete()
}
