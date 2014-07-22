#!/bin/env groovy

def usage = '''Add ONE langugage to faces-config.xml.
-p to perform a zanata pull. -v to supply client version to use.
First argument is locale code optional second argument is the langugage name.
Example:
enableLanguage.groovy -p -v 3.3.2 hu Hungarian
'''

def cli = new CliBuilder(usage: usage)
cli.p(longOpt: "pull", 'invoke zanata pull for the locale')
cli.v(longOpt: "client-version", args: 2, 'the client version to use')

def options = cli.parse(args)
List<String> arguments = options.arguments()
assert arguments.size() >= 1 :"takes at least one argument (the locale)"


def locale = arguments.get(0)
def language = arguments.size() == 2 ? arguments[1] : locale


File scriptDir = new File(
    getClass().protectionDomain.codeSource.location.path).parentFile
File zanataWar = scriptDir.parentFile.parentFile
File server = zanataWar.parentFile
File serverConfig = new File(server, "zanata.xml")

def cliVer = options.v ? options.v : '3.3.2'

if (options.p) {
    def command = "mvn -B org.zanata:zanata-maven-plugin:$cliVer:pull -Dzanata.locales=$locale -Dzanata.projectConfig=$serverConfig"
    println "** about to execute: $command"
    Process process = command.execute(null, server)
    process.consumeProcessOutput(System.out, System.out)
    process.waitFor()
    assert process.exitValue() == 0 :"pull returns error"
}


File facesConfig = new File(
    zanataWar.absolutePath + "/src/main/webapp/WEB-INF/faces-config.xml")

assert facesConfig.exists() :"faces-config.xml exists at $facesConfig"

def lines = facesConfig.readLines("UTF-8")
int lastSupportedLocaleIndex = 0;
boolean isComment = false
lines.eachWithIndex { String line, int index ->
    // this is not very robust.
    if (line.contains("<!--") && !line.contains("-->")) {
        isComment = true
    }
    if (!line.contains("<!--") && line.contains("-->")) {
        isComment = false
    }
    if (!isComment && line.contains("<supported-locale>")) {
        lastSupportedLocaleIndex = index
    }
//    println "$isComment $lastSupportedLocaleIndex $line"
}

assert lastSupportedLocaleIndex > 0 :"last supported locale tag not found"

def indent = { int it ->
    " " * it
}

// method to get supported locales in xml
def supportedLocalesInXml = {
    def config = new XmlSlurper().parseText(it)
    def supportedLocales = config.application."locale-config"."supported-locale"
    def locales = supportedLocales.nodeIterator().toList().collect {
        it.text()
    }
    println ">> supported locales $locales"
    locales
}

println "Before change:"
def beforeChange = supportedLocalesInXml(lines.join(""))

def indent6 = indent(6)
lines.add(lastSupportedLocaleIndex + 1, "$indent6<!-- $language -->")
lines.add(lastSupportedLocaleIndex + 2, "$indent6<supported-locale>$locale</supported-locale>" )

def xml = lines.join("\n")

println "After change:"
def afterChange = supportedLocalesInXml(xml)

// verify xml content
assert afterChange - beforeChange == [locale] :"$locale is not added to faces-config.xml"

println("==== new faces-config.xml content ====")
facesConfig.withPrintWriter("UTF8") {
    it.println(xml)
}
println("======================================")
println("Make sure faces-config.xml content is correct!! Run git diff")
