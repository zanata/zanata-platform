package org.zanata

import groovy.util.slurpersupport.NodeChildren
import org.junit.Test

class GroovyScriptFunctionTest {
def xml = '''<?xml version='1.0' encoding='UTF-8'?>
<faces-config version="2.1"
  xmlns="http://java.sun.com/xml/ns/javaee"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-facesconfig_2_1.xsd">

  <application>
    <locale-config>
      <default-locale>en</default-locale>
      <!-- English -->
      <supported-locale>en</supported-locale>
      <!-- Ukranian -->
      <supported-locale>uk</supported-locale>
      <!-- Traditional Chinese -->
      <supported-locale>zh-TW-Hant</supported-locale>
      <!-- Japanese -->
      <supported-locale>ja</supported-locale>
      <!-- Hungarian -->
      <supported-locale>hu</supported-locale>
      <!-- Breton -->
      <supported-locale>br</supported-locale>

      <!--
      These languages are yet to translated to an acceptable coverage
      See https://translate.zanata.org/zanata/iteration/view/zanata-server/master

      Bulgarian
      <supported-locale>bg</supported-locale>
      Deutsch
      <supported-locale>de</supported-locale>
      Francais
      <supported-locale>fr</supported-locale>
      Italian
      <supported-locale>it</supported-locale>
      Turkish
      <supported-locale>tr</supported-locale>
      Pseudo - not to be enabled for release
      <supported-locale>qc</supported-locale>
      -->
    </locale-config>

    <!-- Workaround for RichFaces 3.3.3 bug.
         See http://stackoverflow.com/questions/5030182/illegal-attempt-to-set-viewhandler-after-a-response-has-been-rendered
         https://issues.jboss.org/browse/JBAS-8375?focusedCommentId=12550006&page=com.atlassian.jira.plugin.system.issuetabpanels%3acomment-tabpanel#comment-12550006
    -->
    <!--<view-handler>org.ajax4jsf.application.AjaxViewHandler</view-handler>-->

  </application>
  <lifecycle>
    <phase-listener>org.zanata.security.FedoraOpenIdPhaseListener</phase-listener>
  </lifecycle>
</faces-config>
'''

    def changed = '''<?xml version='1.0' encoding='UTF-8'?>
<faces-config version="2.1"
  xmlns="http://java.sun.com/xml/ns/javaee"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-facesconfig_2_1.xsd">

  <application>
    <locale-config>
      <default-locale>en</default-locale>
      <!-- English -->
      <supported-locale>en</supported-locale>
      <!-- Ukranian -->
      <supported-locale>uk</supported-locale>
      <!-- Traditional Chinese -->
      <supported-locale>zh-TW-Hant</supported-locale>
      <!-- Japanese -->
      <supported-locale>ja</supported-locale>
      <!-- Hungarian -->
      <supported-locale>hu</supported-locale>

      <!--
      These languages are yet to translated to an acceptable coverage
      See https://translate.zanata.org/zanata/iteration/view/zanata-server/master

      Bulgarian
      <supported-locale>bg</supported-locale>
      Deutsch
      <supported-locale>de</supported-locale>
      Francais
      <supported-locale>fr</supported-locale>
      Italian
      <supported-locale>it</supported-locale>
      Turkish
      <supported-locale>tr</supported-locale>
      Pseudo - not to be enabled for release
      <supported-locale>qc</supported-locale>
      -->
    </locale-config>

    <!-- Workaround for RichFaces 3.3.3 bug.
         See http://stackoverflow.com/questions/5030182/illegal-attempt-to-set-viewhandler-after-a-response-has-been-rendered
         https://issues.jboss.org/browse/JBAS-8375?focusedCommentId=12550006&page=com.atlassian.jira.plugin.system.issuetabpanels%3acomment-tabpanel#comment-12550006
    -->
    <!--<view-handler>org.ajax4jsf.application.AjaxViewHandler</view-handler>-->

  </application>
  <lifecycle>
    <phase-listener>org.zanata.security.FedoraOpenIdPhaseListener</phase-listener>
  </lifecycle>
</faces-config>
'''

    @Test
    public void test() {
        NodeChildren supportedLocalesBefore = readLocales(xml)

        def before = supportedLocalesBefore.nodeIterator().toList().collect {
            it.text()
        }
        println before

        NodeChildren supportedLocalesAfter = readLocales(changed)


        def after = supportedLocalesAfter.nodeIterator().toList().collect {
            it.text()
        }
        assert before - after == ["br"]
    }

    private def readLocales(String xml) {
        def config = new XmlSlurper().parseText(xml)
        config.application."locale-config"."supported-locale"
    }
}
