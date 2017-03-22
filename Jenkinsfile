#!/usr/bin/env groovy
@Library('github.com/zanata/zanata-pipeline-library@master')

/* Only keep the 10 most recent builds. */
def projectProperties = [
  [
    $class: 'BuildDiscarderProperty',
    strategy: [$class: 'LogRotator', numToKeepStr: '10']
  ],
]

properties(projectProperties)

def surefireTestReports='target/surefire-reports/TEST-*.xml'

/* Upto stash stage should fail fast:
 * Failed and stop the build
 * Yet able to create report
 */
timestamps {
  node {
    ansicolor {
      try {
        stage('Checkout') {
          info.printNode()
          notify.started()

          // Shallow Clone does not work with RHEL7, which use git-1.8.3
          // https://issues.jenkins-ci.org/browse/JENKINS-37229
          checkout scm
          // Clean the workspace
          sh "git clean -fdx"
        }

        stage('Check build tools') {
          info.printNode()
          // TBD: whether install tools using Jenkinsfile

          // Note: if next step happens on another node, mvnw might have to download again
          sh "./mvnw --version"
        }

        stage('Pre-build'){
          info.printNode()
          /* Check Translation */
          sh """./run-clean.sh ./mvnw -e \
                     com.googlecode.l10n-maven-plugin:l10n-maven-plugin:1.8:validate \
                     -pl :zanata-war -am -DexcludeFrontend \
             """
        }

        // Build and Unit Tests
        // The result is archived for integration tests in other nodes.
        stage('Build') {
          info.printNode()
          info.printEnv()
          def jarFiles = 'target/*.jar'
          def warFiles = 'target/*.war'

          // Continue building even when test failure
          // Thus -Dmaven.test.failure.ignore is required
          sh """./run-clean.sh ./mvnw -e clean install jxr:aggregate\
                      --batch-mode \
                      --settings .travis-settings.xml \
                      --update-snapshots \
                      -DstaticAnalysis \
                      -Dchromefirefox \
                      -DskipFuncTests \
                      -DskipArqTests \
                      -Dmaven.test.failure.ignore \
             """
          setJUnitPrefix("UNIT", surefireTestReports)
          junit([
              testResults: "**/${surefireTestReports}"
              ])

          // notify if compile+unit test successful
          notify.testResults("UNIT")
          archive "**/${jarFiles},**/${warFiles}"
          step([ $class: 'JacocoPublisher' ])
        }

        stage('stash') {
          stash name: 'workspace', includes: '**/target/**, server/zanata-frontend/src/**,server/'
        }
      } catch (e) {
        notify.failed()
        currentBuild.result = 'FAILURE'
        throw e
      }
    }
  }

  if ( currentBuild.result == null ) {
    stage('Integration tests') {
      try {
        def tasks = [:]

        tasks["Integration tests: WILDFLY"] = {
          integrationTests('wildfly8')
        }
        tasks["Integration tests: JBOSSEAP"] = {
          integrationTests('jbosseap6')
        }
        tasks.failFast = true
        parallel tasks
        //   notify.successful()
      } catch (e) {
        // When it cannot find the failfast report
        echo "ERROR integrationTests: ${e.toString()}"
      }
    }

  // TODO notify finish
  // TODO in case of failure, notify culprits via IRC and/or email
  // https://wiki.jenkins-ci.org/display/JENKINS/Email-ext+plugin#Email-extplugin-PipelineExamples
  // http://stackoverflow.com/a/39535424/14379
  // IRC: https://issues.jenkins-ci.org/browse/JENKINS-33922
  // possible alternatives: Slack, HipChat, RocketChat, Telegram?
  }
}

// TODO factor these out into zanata-pipeline-library too

void xvfb(Closure wrapped) {
wrap([$class: 'Xvfb', debug: true, timeout: 30, displayName: (10+ "${env.EXECUTOR_NUMBER}".toInteger())]) {
    wrapped.call()
  }
}

void debugChromeDriver() {
  sh returnStatus: true, script: 'which chromedriver google-chrome'
  sh returnStatus: true, script: 'ls -l /opt/chromedriver /opt/google/chrome/google-chrome'
}

void integrationTests(String appserver) {
  def failsafeTestReports='target/failsafe-reports/TEST-*.xml'
  node {
    info.printNode()
    info.printEnv()
    echo "WORKSPACE=${env.WORKSPACE}"
    checkout scm
    // Clean the workspace
    sh "git clean -fdx"
    debugChromeDriver()

    unstash 'workspace'
    // TODO: Consider touching the target files for test, so it won't recompile

    /* touch all target */
    sh "find `pwd -P` -path '*/target/*' -print -exec touch '{}' \\;"

    try {
      xvfb {
        withPorts {
          // Run the maven build
          echo "DISPLAY=${env.DISPLAY}"
          sh """./run-clean.sh ./mvnw -e -T 1 install \
                     --batch-mode \
                     --settings .travis-settings.xml \
                     -Danimal.sniffer.skip=true \
                     -DstaticAnalysis=false \
                     -Dcheckstyle.skip \
                     -Dappserver=$appserver \
                     -Dcargo.debug.jvm.args= \
                     -DskipUnitTests \
                     -Dmaven.main.skip \
                     -Dgwt.compiler.skip \
                     -Dwebdriver.display=${env.DISPLAY} \
                     -Dwebdriver.type=chrome \
                     -Dwebdriver.chrome.driver=/opt/chromedriver \

               """
// TODO              -DallFuncTests

        }
      }
    } catch(e) {
      // Exception will be thrown if maven fails fast, i.e. a test fails
      echo "ERROR integrationTests(${appserver}): ${e.toString()}"
      currentBuild.result = 'UNSTABLE'
      archive(
        includes: '*/target/**/*.log,*/target/screenshots/**,**/target/site/xref/**',
        excludes: '**/BACKUP-*.log')
    } finally {
      setJUnitPrefix(appserver, failsafeTestReports)
      junit([
        testResults: "**/${failsafeTestReports}"
//      testDataPublishers: [[$class: 'StabilityTestDataPublisher']]
      ])
      notify.testResults(appserver.toUpperCase())
    }
  }
}

void withPorts(Closure wrapped) {
  def ports = sh(script: 'server/etc/scripts/allocate-jboss-ports', returnStdout: true)
    withEnv(ports.trim().readLines()) {
      wrapped.call()
    }
}

// Modify classnames of tests, to avoid collision between EAP and WildFly test runs.
// We also use this to distinguish unit tests from integration tests.
// from https://issues.jenkins-ci.org/browse/JENKINS-27395?focusedCommentId=256459&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-256459
void setJUnitPrefix(prefix, files) {
  // add prefix to qualified classname
  def reportFiles = findFiles glob: "**/${files}"
  if (reportFiles.size() > 0){
    sh "sed -i \"s/\\(<testcase .*classname=['\\\"]\\)\\([a-z]\\)/\\1${prefix.toUpperCase()}.\\2/g\" ${reportFiles.join(" ")}"
  }else{
    echo "[WARNING] Failed to find JUnit report files **/${files}"
  }
}

