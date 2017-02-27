#!/usr/bin/env groovy
@Library('github.com/zanata/zanata-pipeline-library@master')

def dummyForLibrary = ""

try {
  pullRequests.ensureJobDescription()

  timestamps {
    node {
      ansicolor {
        stage('Checkout') {
          info.printNode()
          notify.started()

          // TODO use reference repo instead of shallow clone
          // see https://issues.jenkins-ci.org/browse/JENKINS-33273?focusedCommentId=268631&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-268631
          // and https://issues.jenkins-ci.org/browse/JENKINS-33273?focusedCommentId=273644&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-273644

          // This doesn't work on RHEL 7: https://issues.jenkins-ci.org/browse/JENKINS-37229

          // Checkout code from repository
          // Based on https://issues.jenkins-ci.org/browse/JENKINS-33022?focusedCommentId=248530&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-248530
          // This should be equivalent to checkout scm (noTags: true, shallow: true)
          checkout([
             $class: 'GitSCM',
             branches: scm.branches,
             doGenerateSubmoduleConfigurations: scm.doGenerateSubmoduleConfigurations,
             extensions: scm.extensions + [[$class: 'CloneOption', noTags: true, reference: '', shallow: true]],
             submoduleCfg: [],
             userRemoteConfigs: scm.userRemoteConfigs + [ ssh://git@github.com:zanata/zanata-platform.git ]
           ])

          //checkout scm
        }

        stage('Install build tools') {
          info.printNode()
          // TODO yum install the following
          // Xvfb libaio xorg-x11-server-Xvfb wget unzip git-core
          // https://dl.google.com/linux/direct/google-chrome-stable_current_x86_64.rpm
          // gcc-c++
          // java-devel; set alternatives for java
          // groovy firefox rpm-build docker
          // download chromedriver

          // Note: if next step happens on another node, mvnw might have to download again
          sh "./mvnw --version"
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
          sh """./run-clean.sh ./mvnw -e clean package \
                     --batch-mode \
                     --settings .travis-settings.xml \
                     --update-snapshots \
                     -Dmaven.test.failure.ignore \
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
  def testReports = '**/target/failsafe-reports/TEST-*.xml'
  sh "shopt -s globstar && rm -f $testReports"
  xvfb {
    withPorts {
      // Run the maven build
      sh """./run-clean.sh ./mvnw -e verify \
                   --batch-mode \
                   --settings .travis-settings.xml \
                   -DstaticAnalysis=false \
                   -Dcheckstyle.skip \
                   -Dappserver=$appserver \
                   -DskipUnitTests \
                   -Dmaven.test.failure.ignore \
                   -Dmaven.main.skip \
                   -Dgwt.compiler.skip \
                   -Dwebdriver.display=${env.DISPLAY} \
                   -Dcargo.debug.jvm.args= \
                   -Dwebdriver.type=chrome \
                   -Dwebdriver.chrome.driver=/opt/chromedriver \
                   -DallFuncTests \\
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
