import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

def cli = new CliBuilder(usage: "test-trace")
cli.p(args: 1, argName: 'polarionXml', 'Polarion workitems file')
cli.a(args: 1, argName: 'autotestJson', 'Trace autotest data')
cli.o(args: 1, argName: 'outputFile', 'Output Json file location')
def options = cli.parse(args)

options || System.exit(1)

String polarionFile = options.p
String autoTestFile = options.a
String outputFileName = options.o == false ? System.getProperty("user.dir")+"/results-collated.json" : options.o

String polarionData = new File(polarionFile).text
String autoTestData = new File(autoTestFile).text
println(polarionFile + "+" + autoTestFile + "=" + outputFileName)
Map<String, List<String>> data = new HashMap<>()
def dataMap = [:]

def parseDataFiles = {
  def rootNode = new XmlSlurper().parseText(polarionData)

  rootNode.workItem.each {
    String testId = it.fields.id
    String testSummary = it.fields.title
    if (it.fields.type.@id != "testcase") {
      // Not a testcase
      return
    }
    if (data.containsKey(testId)) {
      println("Error: key " + testId + " already exists")
      List<String> newData = data.get(testId)
      newData.add("[DUP]") + testSummary
      data.put(testId, newData)
    } else {
      dataMap.put((testId), [id:testId, summary:testSummary, autoTest:"untested", autotestDesc:"untested", result:'untested'])
    }
  }

  def jsonObject = new JsonSlurper().parseText(autoTestData)
  jsonObject.each {
    Object test = it
    it.testIds.each {
      String id = "ZAN-"+it
      if (dataMap.containsKey(id)) {
        def map = dataMap.get(id)
        map.put("autoTest", test.testName)
        map.put("autotestDesc", test.summary)
        map.put("result", test.testResult)
        dataMap.put(id, map)
      } else {
        println("Found rogue test case " + id)
      }
    }
  }

  File jsonOutFile = new File(outputFileName)
  assert jsonOutFile.canWrite()
  jsonOutFile.write(new JsonBuilder(dataMap.values()).toPrettyString())
}

parseDataFiles()
