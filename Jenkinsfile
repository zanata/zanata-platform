#!/usr/bin/env groovy

/**
 * Jenkinsfile for zanata-platform
 */

import groovy.transform.Field

// Import pipeline library for utility methods & classes:
// ansicolor(), Notifier, PullRequests, Strings
@Library('zanata-pipeline-library@pipelineunit')
import org.zanata.jenkins.Notifier
import org.zanata.jenkins.PullRequests

PullRequests.ensureJobDescription(env, manager, steps)

@Field
def notify
// initialiser must be run separately (bindings not available during compilation phase)
notify = new Notifier(env, steps)

// Define project properties: general properties for the Pipeline-defined jobs.
// 1. discard old artifacts and build logs
// 2. configure build parameters (eg requested labels for build nodes)
//
// Normally the project properties wouldn't go inside a node, but
// we need a node to access env.DEFAULT_NODE.
node {
  echo "running on node ${env.NODE_NAME}"
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
           * (eg kvm) or default to build on any node
           */
          defaultValue: env.DEFAULT_NODE ?: 'master || !master',
          description: 'Jenkins node label to use for build',
          name: 'LABEL'
        ]
      ]
    ],
  ]

  properties(projectProperties)
}

@Field
def mainlineBranches = ['master', 'release', 'legacy']
@Field
def gwtOpts = '-Dchromefirefox'
if (env.BRANCH_NAME in mainlineBranches) {
  gwtOpts = ''
}

/* Build/Unit Tests should fail fast:
 * We want to stop the build, but first create JUnit/surefire reports
 */

// use timestamps for Jenkins logs
timestamps {
  // allocate a node for build+unit tests
  node(LABEL) {
    echo "running on node ${env.NODE_NAME}"
    // generate logs in colour
    ansicolor {
      try {

        stage('Checkout') {
          // notify methods send instant messages about the build progress
          notify.started()

          // Shallow Clone does not work with RHEL7, which uses git-1.8.3
          // https://issues.jenkins-ci.org/browse/JENKINS-37229
          checkout scm

          // Clean the workspace
          sh "git clean -fdx"
        }

        // Build and Unit Tests
        // The built files are stashed for integration tests in other nodes.
        stage('Build') {

          // validate translations
          sh """./run-clean.sh ./mvnw -e -V \
              com.googlecode.l10n-maven-plugin:l10n-maven-plugin:1.8:validate \
              -pl :zanata-war -am -DexcludeFrontend \
          """

          def jarFiles = 'target/*.jar'
          def warFiles = 'target/*.war'

          // run-clean.sh: ensure that child processes will be cleaned up
          // (eg surefirebooter)
          // -T 1: run build without multi-threading (workaround for
          // suspected concurrency problems)
          // -Dmaven.test.failure.ignore: Continue building other modules
          // even after test failures.
          sh """./run-clean.sh ./mvnw -e -V -T 1 clean install jxr:aggregate\
              --batch-mode \
              --settings .travis-settings.xml \
              --update-snapshots \
              -DstaticAnalysis \
              $gwtOpts \
              -DskipFuncTests \
              -DskipArqTests \
              -Dmaven.test.failure.ignore \
          """
          // TODO add -Dvictims
          // TODO should we remove -Dmaven.test.failure.ignore (and catch
          // exception) to fail fast in case an early unit test fails?

          def surefireTestReports = 'target/surefire-reports/TEST-*.xml'

          setJUnitPrefix("UNIT", surefireTestReports)
          // gather surefire results; mark build as unstable in case of failures
          junit(testResults: "**/${surefireTestReports}")

          // TODO send to codecov.io (NB: need to get correct token for zanata-platform, configured by env var)

          // notify if compile+unit test successful
          // TODO update notify (in pipeline library) to support Rocket.Chat webhook integration
          notify.testResults("UNIT", currentBuild.result)

          // TODO ensure the pipeline aborts in case of test failures

          // archive build artifacts (and cross-referenced source code)
          archive "**/${jarFiles},**/${warFiles},**/target/site/xref/**"

          // parse Jacoco test coverage
          step([ $class: 'JacocoPublisher' ])
          // TODO push Jest/Jacoco results to codecov.io (with correct token)
        }

        // gather built files to use in following pipeline stages (on
        // other build nodes)
        stage('Stash') {
          stash name: 'generated-files', includes: '**/target/**, **/src/main/resources/**,**/.zanata-cache/**'
        }
      } catch (e) {
        echo("Caught exception: " + e)
        notify.failed()
        currentBuild.result = 'FAILURE'
        // abort the rest of the pipeline
        throw e
      }
    }
  }

  // if the build is still green:
  if ( currentBuild.result == null ) {
    // NB all the parallel tasks must be in one stage, because you can't use `stage` inside `parallel`.
    // See https://issues.jenkins-ci.org/browse/JENKINS-34696 and https://issues.jenkins-ci.org/browse/JENKINS-33185
    stage('Integration tests') {
      // define tasks which will run in parallel
      def tasks = [
              "WILDFLY": { integrationTests('wildfly8') },
              "JBOSSEAP": { integrationTests('jbosseap6') },
              // abort other tasks (for faster feedback) as soon as one fails
              // disabled; not currently handled by pipeline-unit
//              failFast: true
      ]
      // run integration test tasks in parallel
      parallel tasks

      // if the build is *still* green after running integration tests:
      if ( currentBuild.result == null ) {
        echo 'marking build as successful'
        currentBuild.result = 'SUCCESS'
      }

      // TODO notify finish
      // TODO in case of failure, notify culprits via IRC, email and/or Rocket.Chat
      // https://wiki.jenkins-ci.org/display/JENKINS/Email-ext+plugin#Email-extplugin-PipelineExamples
      // http://stackoverflow.com/a/39535424/14379
      // IRC: https://issues.jenkins-ci.org/browse/JENKINS-33922
    }
  }
}

void integrationTests(String appserver) {
  def failsafeTestReports='target/failsafe-reports/TEST-*.xml'
  node(LABEL) {
    echo "running on node ${env.NODE_NAME}"
    echo "WORKSPACE=${env.WORKSPACE}"
    checkout scm
    // Clean the workspace
    sh "git clean -fdx"

    unstash 'generated-files'
    // TODO: Consider touching the target files for test, so it won't recompile

    /* touch all target */
    //sh "find `pwd -P` -path '*/target/*' -print -exec touch '{}' \\;"

    xvfb {
      withPorts {
        echo "Running maven build for integration tests"

        // Run the maven build
        echo "env.DISPLAY=${env.DISPLAY}"
        echo "env.JBOSS_HTTP_PORT=${env.JBOSS_HTTP_PORT}"
        echo "env.JBOSS_HTTPS_PORT=${env.JBOSS_HTTPS_PORT}"

        // Build up Maven options for running functional tests:

        // avoid port conflict for debugger:
        def ftOpts = '-Dcargo.debug.jvm.args= '

        // run *all* functional tests in these branches only:
        if (env.BRANCH_NAME in mainlineBranches) {
          ftOpts += '-DallFuncTests '
        }

        // skip recompilation, unit tests, static analysis
        // (done in Build stage):
        ftOpts += """\
              -Dgwt.compiler.skip \
              -Dmaven.main.skip \
              -Dskip.npminstall \
              -DskipUnitTests \
              -Danimal.sniffer.skip \
              -Dcheckstyle.skip \
              -Dfindbugs.skip \
              -DstaticAnalysis=false \
              $gwtOpts \
          """

        def mvnResult = sh returnStatus: true, script: """\
              ./run-clean.sh ./mvnw -e -V -T 1 install \
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

        if (mvnResult != 0) {
          currentBuild.result = 'UNSTABLE'

          // gather db/app logs and screenshots to help debugging
          archive(
                  includes: '*/target/**/*.log,*/target/screenshots/**',
                  excludes: '**/BACKUP-*.log')
        }

        echo "Capturing JUnit results"
        if (setJUnitPrefix(appserver, failsafeTestReports)) {
          junit(testResults: "**/${failsafeTestReports}"
                  // TODO enable after https://issues.jenkins-ci.org/browse/JENKINS-33168 is fixed
                  // , testDataPublishers: [[$class: 'StabilityTestDataPublisher']]
          )
        } else {
          currentBuild.result = 'FAILED'
        }
        notify.testResults(appserver.toUpperCase(), currentBuild.result)
        // TODO ensure the pipeline aborts in case of test failures
      }
    }
  }
}

void xvfb(Closure wrapped) {
  wrap([$class: 'Xvfb', debug: true, timeout: 30, displayName: (10 + env.EXECUTOR_NUMBER.toInteger())]) {
    wrapped.call()
  }
}

// Run enclosed steps after allocating ports and adding them to the environment
void withPorts(Closure wrapped) {
  def ports = sh(script: 'server/etc/scripts/allocate-jboss-ports', returnStdout: true)
  withEnv(ports.trim().readLines()) {
    wrapped.call()
  }
}

// Modify classnames of tests, to avoid collision between EAP and WildFly test runs.
// We also use this to distinguish unit tests from integration tests.
// from https://issues.jenkins-ci.org/browse/JENKINS-27395?focusedCommentId=256459&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-256459
boolean setJUnitPrefix(prefix, files) {
  // add prefix to qualified classname
  def reportFiles = findFiles glob: "**/${files}"
  if (reportFiles.size() > 0) {
    sh "sed -i \"s/\\(<testcase .*classname=['\\\"]\\)\\([a-z]\\)/\\1${prefix.toUpperCase()}.\\2/g\" ${reportFiles.join(" ")}"
    return true
  } else {
    echo "[WARNING] Failed to find JUnit report files **/${files}"
    return false
  }
}
