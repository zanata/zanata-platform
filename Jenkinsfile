#!/usr/bin/env groovy
@Library('github.com/zanata/zanata-pipeline-library@master')

/* Only keep the 10 most recent builds. */
def projectProperties = [
[$class: 'BuildDiscarderProperty',strategy: [$class: 'LogRotator', numToKeepStr: '10']],
    ]

properties(projectProperties)


def dummyForLibrary = ""

try {
  pullRequests.ensureJobDescription()

  timestamps {
    node {
      ansicolor {
        stage('Checkout') {
          info.printNode()
          notify.started()

          // Shallow Clone does not work with RHEL7, which use git-1.8.3
          // https://issues.jenkins-ci.org/browse/JENKINS-37229
            checkout scm
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
                     -pl :zanata-war -am -DexcludeFrontend
             """
        }

        // TODO build and archive binaries with unit tests, then in parallel, unarchive and run:
        //   mysql 5.6, functional tests (on wildfly)
        //   and (later) mariadb 10 functional tests (on wildfly)

        stage('Build') {
          info.printNode()
          info.printEnv()
          def testReports = '**/target/surefire-reports/TEST-*.xml'
          def warFiles = '**/target/*.war'
          sh "shopt -s globstar && rm -f $testReports $warFiles"
          sh """./run-clean.sh ./mvnw -e clean package jxr:aggregate\
                     --batch-mode \
                     --settings .travis-settings.xml \
                     --update-snapshots \
                     -DstaticAnalysis \
                     -Dchromefirefox \
                     -DskipFuncTests \
                     -DskipArqTests \
             """
          setJUnitPrefix("UNIT", testReports)
          junit testResults: testReports, testDataPublishers: [[$class: 'StabilityTestDataPublisher']]

          // notify if compile+unit test successful
          notify.testResults("UNIT")

          archive warFiles
        }

        stage('stash') {
          stash name: 'workspace', includes: '**'
        }
      }
    }

    // TODO limit parallel runs of integration tests
    stage('Integration tests') {
      def tasks = [:]
      tasks['Integration tests: WILDFLY'] = {
        node {
          ansicolor {
            info.printNode()
            info.printEnv()
            debugChromeDriver()
            unstash 'workspace'
            integrationTests('wildfly8')
          }
        }
      }
      tasks['Integration tests: JBOSSEAP'] = {
        node {
          ansicolor {
            info.printNode()
            info.printEnv()
            debugChromeDriver()
            unstash 'workspace'
            integrationTests('jbosseap6')
          }
        }
      }
      tasks.failFast = true
      parallel tasks
    }
    // TODO in case of failure, notify culprits via IRC and/or email
    // https://wiki.jenkins-ci.org/display/JENKINS/Email-ext+plugin#Email-extplugin-PipelineExamples
    // http://stackoverflow.com/a/39535424/14379
    // IRC: https://issues.jenkins-ci.org/browse/JENKINS-33922
    // possible alternatives: Slack, HipChat, RocketChat, Telegram?
    notify.successful()
  }
} catch (e) {
  notify.failed()
  throw e
}

// TODO factor these out into zanata-pipeline-library too

void xvfb(Closure wrapped) {
  wrap([$class: 'Xvfb']) {
    wrapped.call()
  }
}

void debugChromeDriver() {
  sh returnStatus: true, script: 'which chromedriver google-chrome'
  sh returnStatus: true, script: 'ls -l /opt/chromedriver /opt/google/chrome/google-chrome'
}

void integrationTests(def appserver) {
  sh "find . -path */target/failsafe-reports/TEST-*.xml -print -delete"
  xvfb {
    withPorts {
      // Run the maven build
      sh """./run-clean.sh ./mvnw -e verify \
                   --batch-mode \
                   --settings .travis-settings.xml \
                   -DstaticAnalysis=false \
                   -Dcheckstyle.skip \
                   -Dappserver=$appserver \
                   -Dcargo.debug.jvm.args= \
                   -DskipUnitTests \
                   -Dmaven.test.failure.ignore \
                   -Dmaven.main.skip \
                   -Dgwt.compiler.skip \
                   -Dwebdriver.display=${env.DISPLAY} \
                   -Dwebdriver.type=chrome \
                   -Dwebdriver.chrome.driver=/opt/chromedriver \
                   -DallFuncTests
      """
// FIXME put this back
      // TODO add -Dmaven.war.skip (but we need zanata-test-war)

      // TODO try to remove cargo.debug.jvm.args webdriver.*
    }
  }
  setJUnitPrefix(appserver.toUpperCase(), testReports)
  junit testReports
  archiveTestFilesIfUnstable()
  notify.testResults(appserver.toUpperCase())
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
  sh "shopt -s globstar && sed -i \"s/\\(<testcase .*classname=['\\\"]\\)\\([a-z]\\)/\\1${prefix}.\\2/g\" $files"
}

void archiveTestFilesIfUnstable() {
  // if tests have failed currentBuild.result will be 'UNSTABLE'
  if (currentBuild.result != null) {
    archive(
        includes: '*/target/**/*.log,*/target/screenshots/**,**/target/site/jacoco/**,**/target/site/xref/**',
        excludes: '**/BACKUP-*.log')
  }
}
