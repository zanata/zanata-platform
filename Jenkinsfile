#!/usr/bin/env groovy
@Library('github.com/zanata/zanata-pipeline-library@master')
def dummyForLibrary

/**
 * Jenkinsfile for zanata-platform
 */

// It seems that we need a node to access env.DEFAULT_NODE
node {
  def projectProperties = [
    [
      $class: 'BuildDiscarderProperty',
      strategy: [$class: 'LogRotator',
        daysToKeepStr: '30',       // keep records no more than X days
        numToKeepStr: '20',        // keep records for at most X builds
        artifactDaysToKeepStr: '', // keep artifacts no more than X days
        artifactNumToKeepStr: '4'] // keep artifacts for at most X builds
    ],
    [
      $class: 'ParametersDefinitionProperty',
      parameterDefinitions: [
        [
          $class: 'LabelParameterDefinition',
          /* Specify the default node in Jenkins environment DEFAULT_NODE
           * or default to build on any node
           */
          defaultValue: env.DEFAULT_NODE ?: 'master || !master',
          description: 'Node label that allow to build',
          name: 'LABEL'
        ]
      ]
    ],
  ]

  properties(projectProperties)
}

def surefireTestReports='target/surefire-reports/TEST-*.xml'

/* Upto stash stage should fail fast:
 * Failed and stop the build
 * Yet able to create report
 */
timestamps {
  node(LABEL) {
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

        // Build and Unit Tests
        // The built files are stashed for integration tests in other nodes.
        stage('Build') {
          info.printNode()
          info.printEnv()

          // validate translations
          sh """./run-clean.sh ./mvnw -e -V \
                     com.googlecode.l10n-maven-plugin:l10n-maven-plugin:1.8:validate \
                     -pl :zanata-war -am -DexcludeFrontend \
             """

          def jarFiles = 'target/*.jar'
          def warFiles = 'target/*.war'

          // Continue building even when test failure
          // Thus -Dmaven.test.failure.ignore is required
          sh """./run-clean.sh ./mvnw -e -V -T 1 clean install jxr:aggregate\
                      --batch-mode \
                      --settings .travis-settings.xml \
                      --update-snapshots \
                      -DstaticAnalysis \
                      -Dchromefirefox \
                      -DskipFuncTests \
                      -DskipArqTests \
                      -Dmaven.test.failure.ignore \
          """
          // TODO add -Dvictims
          // TODO should we remove -Dmaven.test.failure.ignore (and catch
          // exception) to fail fast in case an early unit test fails?

          setJUnitPrefix("UNIT", surefireTestReports)
          junit([
              testResults: "**/${surefireTestReports}"
              ])

          // TODO run codecov (NB: need to get correct token for zanata-platform, configured by env var)

          // notify if compile+unit test successful
          // TODO update notify (in pipeline library) to support Rocket.Chat webhook integration
          notify.testResults("UNIT")
          archive "**/${jarFiles},**/${warFiles},**/target/site/xref/**"

          step([ $class: 'JacocoPublisher' ])
        }

        stage('Stash') {
          stash name: 'generated-files', includes: '**/target/**, **/src/main/resources/**,**/.zanata-cache/**'
        }
      } catch (e) {
        notify.failed()
        currentBuild.result = 'FAILURE'
        throw e
      }
    }
  }

  if ( currentBuild.result == null ) {
    // NB all the parallel tasks must be in one stage, because you can't use `stage` inside `parallel`.
    // See https://issues.jenkins-ci.org/browse/JENKINS-34696 and https://issues.jenkins-ci.org/browse/JENKINS-33185
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

    // TODO notify finish
    // TODO in case of failure, notify culprits via IRC and/or email
    // https://wiki.jenkins-ci.org/display/JENKINS/Email-ext+plugin#Email-extplugin-PipelineExamples
    // http://stackoverflow.com/a/39535424/14379
    // IRC: https://issues.jenkins-ci.org/browse/JENKINS-33922
    // possible alternatives: Slack, HipChat, RocketChat, Telegram?
    }
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
  node(LABEL) {
    info.printNode()
    info.printEnv()
    echo "WORKSPACE=${env.WORKSPACE}"
    checkout scm
    // Clean the workspace
    sh "git clean -fdx"
    debugChromeDriver()

    unstash 'generated-files'
    // TODO: Consider touching the target files for test, so it won't recompile

    /* touch all target */
    //sh "find `pwd -P` -path '*/target/*' -print -exec touch '{}' \\;"

    try {
      xvfb {
        withPorts {
          // Run the maven build
          echo "env.DISPLAY=${env.DISPLAY}"
          echo "env.JBOSS_HTTP_PORT=${env.JBOSS_HTTP_PORT}"
          echo "env.JBOSS_HTTPS_PORT=${env.JBOSS_HTTPS_PORT}"
          // avoid port conflict for debugger:
          def ftOpts = '-Dcargo.debug.jvm.args= '
          // run all functional tests in these branches:
          if (env.BRANCH_NAME in ['master', 'release', 'legacy']) {
            ftOpts += '-DallFuncTests '
          }
          // skip recompilation, unit tests, static analysis:
          ftOpts += """\
              -Dgwt.compiler.skip \
              -Dmaven.main.skip \
              -Dskip.npminstall \
              -DskipUnitTests \
              -Danimal.sniffer.skip \
              -Dcheckstyle.skip \
              -Dfindbugs.skip \
              -DstaticAnalysis=false \
          """
          sh """./run-clean.sh ./mvnw -e -V -T 1 install \
                     --batch-mode \
                     --settings .travis-settings.xml \
                     --update-snapshots \
                     -Dappserver=$appserver \
                     -Dwebdriver.display=${env.DISPLAY} \
                     -Dwebdriver.type=chrome \
                     -Dwebdriver.chrome.driver=/opt/chromedriver \
                     ${ftOpts}
              """
              // TODO skip npm/yarn (but don't -DexcludeFrontend; we need the version in target/ )
              /* TODO
              -Dassembly.skipAssembly \
              -DskipAppassembler \
              -DskipShade \
               */
        }
      }
    } catch(e) {
      // Exception will be thrown if maven fails fast, i.e. a test fails
      echo "ERROR integrationTests(${appserver}): ${e.toString()}"
      currentBuild.result = 'UNSTABLE'
      archive(
        includes: '*/target/**/*.log,*/target/screenshots/**',
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

