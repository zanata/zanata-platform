import com.google.common.io.Files

// This script will used combine pulled translation files from Zanata server and GWT generated skeleton file.
// This will enable GWT to compile locale permutation for not 100% translated locales.
// It's used in zanata.xml as command hook.

File gwtGenDir = new File(project.build.directory + "/gwt-extra/webtrans")
assert gwtGenDir.isDirectory() :"target/gwt-extra/webtrans does not exist. You need to have source available. Either pull with source or re-generate source again."

File basedir = pom.basedir
// zanata pull target dir is here
File pulledTargetDir = new File(basedir.absolutePath + "/src/main/resources/zanata-editor")

// find properties file with underscore (translation properties)
def targetNames = new FileNameByRegexFinder().
    getFileNames(pulledTargetDir.absolutePath, /.+_.+\.properties$/)

def targetProps = targetNames.collect {
    new File(it)
}

def ln = System.getProperty("line.separator")

Map<String, Properties> translationMap = targetProps.collectEntries {
    Properties source = new Properties()
    it.withReader("UTF-8") {
        source.load(it)
    }
    [it.name, source]
}

log.info("translation map: {} \n\n\n", translationMap.keySet())

// get all GWT generated translation skeletons
def genPropNames = new FileNameByRegexFinder().
    getFileNames(gwtGenDir.absolutePath, /\w+\.properties$/, /_default.properties$/)

genPropNames.each {
    // we move GWT generated skeletons to project build class path but not in source tree
    def relativePath = it - gwtGenDir
    def skeleton = new File(it)
    def finalOutput = new File("$project.build.outputDirectory/$relativePath")
    finalOutput.getParentFile().mkdirs()
    log.debug("finalOutput: {}", finalOutput.absolutePath)
    Files.copy(skeleton, finalOutput)

    def translation = translationMap.get(skeleton.name)
    if (!translation) {
        log.info("no translation found for {}", skeleton.name)
        return
    }
    def lines = finalOutput.readLines("UTF-8")
    lines.eachWithIndex { line, index ->
        def pair = line.split(/=/)
        if (pair.size() != 2) {
            return
        }
        String key = pair[0]
        def textFlowTarget = translation.get(key)
        if (textFlowTarget) {
            log.debug("fill in {} with translation: {}", key, textFlowTarget)
            lines.set(index, "$key=$textFlowTarget")
        }
    }

    finalOutput.withPrintWriter("UTF-8") { writer ->
        lines.each {
            writer.append("$it$ln")
        }
    }
    log.info("processed {}", skeleton.name)
}

