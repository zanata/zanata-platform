import com.google.common.io.Files

// This script will rename GWT generated properties for Zanata to use.
log.info "===== Preparing GWT generated properties files ====="

def gwtGenDir = new File(project.build.directory + "/gwt-extra/webtrans")

assert gwtGenDir.isDirectory()

def nameFilter = { dir, name ->
    name.endsWith(".properties")
} as FilenameFilter

def properties = gwtGenDir.listFiles(nameFilter)


if (!properties) {
    log.info "no properties found. quit."
    return
}
// scrip off the file name part to get package name
def packageName = properties[0].name.replaceAll(/\.\w+\.properties/, "")
def packagePath = packageName.replaceAll(/\./, "/")

def resourceDir = new File("$gwtGenDir.absolutePath/$packagePath")
resourceDir.mkdirs()

int sourceCount = 0
int targetCount = 0

properties.each {
    if (it.name.endsWith("_default.properties")) {
        log.debug "   * found source: $it.name"
        // change the name of the file
        def fileName = (it.name - "$packageName.").replace("_default", "")
        def destFile = new File(resourceDir, fileName)
        log.info "     moved to: $destFile"
        Files.move(it, destFile)
        sourceCount++
    } else {
        log.debug "   * found target: $it.name"
        def fileName = (it.name - "$packageName.")
        def destFile = new File(resourceDir, fileName)
        // we ALWAYS copy generated target skeleton to make sure target is in sync if source has changed.
        // Also to make sure enabled locale (in Application.gwt.xml) has something to display if translation is not yet available.
        // Later we will base on pulled translation to repopulate the file.
        // see FillInTranslationGap.groovy
        log.info "     moved to :$destFile"
        Files.move(it, destFile);
        targetCount++
    }
}

log.info "Generated $sourceCount source(s) and $targetCount target(s) in $resourceDir"
log.info "===== Prepared GWT generated properties files ====="

// below procedure is to fix GWT's bizarre behavior.
// if we have properties files on classpath (i.e. compile with extra already),
// the second time GWT compiler produces properties file will output plural forms in properties but some of them are empty.
// It will be empty if:
//  1.  the required plural form for that language is not defined in java interface. i.e. in Ukranian you ought to have "one", "few", "other" defined in @AlternateMessage.
//      see com.google.gwt.i18n.client.impl.plurals.DefaultRule_x1_x234_n
//  2.  in java interface it uses complex plural combination. i.e. having multiple @PluralCount in parameters.
//      see org.zanata.webtrans.client.resources.WebTransMessages.showingResultsForProjectWideSearch
// First one won't cause any trouble. We can ignore it.
// Second one may not be fixable. The GWT doc says the plural form is still a work in progress. I don't know how it works either.
// We can either add extra plural count in the [] i.e. turn [one] to [one|one]. But Zanata doesn't support mismatch source and target.
// So I removing those extra plural entries which allows GWT to compile again (with a warning).
// see https://github.com/zanata/zanata-server/wiki/Localize-Zanata for more detail.

def filter = {
    it.name.endsWith(".properties")
} as FileFilter
properties = resourceDir.listFiles(filter)

def ln = System.getProperty("line.separator")

properties.each {
    def lines = it.readLines("UTF-8")
    boolean touched = false
    lines.eachWithIndex { line, index ->
        if (line.matches(/.+=$/)) {
            log.info("found and removed empty plural entry: {}", line)
            lines.set(index, "")
            touched = true
        }
    }
    if (touched) {
        log.info("processed {}", it.name)
        it.withPrintWriter("UTF-8") { writer ->
            lines.each {
                writer.append("$it$ln")
            }
        }
    }
}
