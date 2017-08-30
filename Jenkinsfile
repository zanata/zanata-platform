#!/usr/bin/env groovy

/**
 * Jenkinsfile for zanata-platform
 */

// Import pipeline library for utility methods & classes:
// ansicolor(), Notifier, PullRequests, Strings
@Library('zanata-pipeline-library@master')
import org.zanata.jenkins.Notifier
import org.zanata.jenkins.PullRequests
import static org.zanata.jenkins.StackTraces.getStackTrace

import groovy.transform.Field

// The first milestone step starts tracking concurrent build order
milestone()

PullRequests.ensureJobDescription(env, manager, steps)

@Field
def notify
// initialiser must be run separately (bindings not available during compilation phase)
notify = new Notifier(env, steps, currentBuild, 'https://github.com/zanata/zanata-platform.git', 'Jenkinsfile')

// we can't set these values yet, because we need a node to look at the environment
@Field
def defaultNodeLabel
@Field
def jobName

// Define project properties: general properties for the Pipeline-defined jobs.
// 1. discard old artifacts and build logs
// 2. configure build parameters (eg requested labels for build nodes)
//
// Normally the project properties wouldn't go inside a node, but
// we need a node to access env.DEFAULT_NODE.
node {
  echo "running on node ${env.NODE_NAME}"
  defaultNodeLabel = env.DEFAULT_NODE ?: 'master || !master'
  // eg github-zanata-org/zanata-platform/update-Jenkinsfile
  jobName = env.JOB_NAME
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
      $class: 'GithubProjectProperty',
      projectUrlStr: 'https://github.com/zanata/zanata-platform'
    ],
    [
      $class: 'ParametersDefinitionProperty',
      parameterDefinitions: [
        [
                $class: 'LabelParameterDefinition',
                // Specify the default node in Jenkins env var DEFAULT_NODE
                // (eg kvm), or leave blank to build on any node.
                defaultValue: defaultNodeLabel,
                description: 'Jenkins node label to use for build',
                name: 'LABEL'
        ],
        [
                $class: 'BooleanParameterDefinition',
                defaultValue: false,
                description: 'Run all functional tests?',
                name: 'allFuncTests'
        ],
      ]
    ],
  ]

  properties(projectProperties)
}

String getLabel() {
  def labelParam = null
  try {
    labelParam = params.LABEL
  } catch (e1) {
    // workaround for https://issues.jenkins-ci.org/browse/JENKINS-38813
    echo '[WARNING] unable to access `params`'
    echo getStackTrace(e1)
    try {
      labelParam = LABEL
    } catch (e2) {
      echo '[WARNING] unable to access `LABEL`'
      echo getStackTrace(e2)
    }
  }

  if (labelParam == null) {
    echo "LABEL param is null; using default value."
  }
  def result = labelParam ?: defaultNodeLabel
  echo "Using build node label: $result"
  return result
}

// NB: don't call this getAllFuncTests() or isAllFuncTests(), because it would
// shadow the global parameter allFuncTests.
boolean resolveAllFuncTests() {
  def paramVal = null
  try {
    paramVal = params.allFuncTests
  } catch (e1) {
    // workaround for https://issues.jenkins-ci.org/browse/JENKINS-38813
    echo '[WARNING] unable to access `params`'
    echo getStackTrace(e1)
    try {
      paramVal = allFuncTests
    } catch (e2) {
      echo '[WARNING] unable to access `allFuncTests`'
      echo getStackTrace(e2)
    }
  }

  if (paramVal == null) {
    echo "allFuncTests param is null; using default value."
  }
  // paramVal may be a String, so compare as Strings
  def result = (paramVal ?: false).toString().equals("true")
  echo "allFuncTests: $result"

  return result
}

@Field
def mainlineBranches = ['master', 'release', 'legacy']
@Field
def gwtOpts = '-Dchromefirefox'
if (env.BRANCH_NAME in mainlineBranches) {
  gwtOpts = ''
}

String getLockName() {
  if (env.BRANCH_NAME in mainlineBranches) {
    // Each mainline branch pipeline will have its own lock.
    return jobName
  } else if (env.BRANCH_NAME.startsWith('PR-')) {
    // Pull requests 1, 101, 201, xx01 will all share the same lock name.
    // Barring collisions, this means each PR gets its own lock, with a
    // maximum of 100 lock names. This is a workaround for
    // https://issues.jenkins-ci.org/browse/JENKINS-38906
    String digits = '00' + env.BRANCH_NAME.substring('PR-'.length())
    String last2Digits = digits.substring(digits.length() - 2)
    return 'zanata-platform-PR-x' + last2Digits
  } else {
    // All miscellaneous branches (across all repos) will share the same lock.
    return 'GitHubMiscBranch'
  }
}

/* Build/Unit Tests should fail fast:
 * We want to stop the build, but first create JUnit/surefire reports
 */

// use timestamps for Jenkins logs
timestamps {
  // allocate a node for build+unit tests
  node(getLabel()) {
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
            -Dbuildtime.output.log \
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
          sh """./run-clean.sh ./mvnw -e -V -T 1 \
            -Dbuildtime.output.log \
            clean install jxr:aggregate \
            --batch-mode \
            --update-snapshots \
            -DstaticAnalysis \
            $gwtOpts \
            -DskipFuncTests \
            -DskipArqTests \
            -Dmaven.test.failure.ignore \
            -Dvictims \
          """

          def surefireTestReports = 'target/surefire-reports/TEST-*.xml'

          setJUnitPrefix('UNIT', surefireTestReports)
          // gather surefire results; mark build as unstable in case of failures
          junit(testResults: "**/${surefireTestReports}")

          // TODO try https://github.com/jenkinsci/github-pr-coverage-status-plugin

          // send test coverage data to codecov.io
          try {
            withCredentials(
                    [[$class: 'StringBinding',
                      credentialsId: 'codecov_zanata-platform',
                      variable: 'CODECOV_TOKEN']]) {
              // NB the codecov script uses CODECOV_TOKEN
              sh "curl -s https://codecov.io/bash | bash -s - -K"
            }
          } catch (InterruptedException e) {
            throw e
          } catch (hudson.AbortException e) {
            throw e
          } catch (e) {
            echo "[WARNING] Ignoring codecov error: $e"
          }

          // notify if compile+unit test successful
          // TODO update notify (in pipeline library) to support Rocket.Chat webhook integration
          notify.testResults('UNIT', currentBuild.result)

          // TODO publish coverage for jest (cobertura format)
          // https://issues.jenkins-ci.org/browse/JENKINS-30700 https://github.com/jenkinsci/cobertura-plugin/pull/62

          // TODO publish reports:
          // appserver/browser/flaky warnings

          // TODO more static analysis:
          // task scanner (for TODOs), DRY, PMD
          // victims, OWASP, ossindex: scan dependencies for vulnerabilities

          // https://wiki.jenkins-ci.org/display/JENKINS/Static+Code+Analysis+Plug-ins
          // https://philphilphil.wordpress.com/2016/12/28/using-static-code-analysis-tools-with-jenkins-pipeline-jobs/

          // archive build artifacts (and cross-referenced source code)
          archive "**/${jarFiles},**/${warFiles},**/target/site/xref/**"

          // parse Jacoco test coverage
          step([$class: 'JacocoPublisher'])

          if (env.BRANCH_NAME == 'master') {
            step([$class: 'MasterCoverageAction'])
          } else if (env.BRANCH_NAME.startsWith('PR-')) {
            step([$class: 'CompareCoverageAction'])
          }

          // ref: https://philphilphil.wordpress.com/2016/12/28/using-static-code-analysis-tools-with-jenkins-pipeline-jobs/
          step([$class: 'CheckStylePublisher',
                pattern: '**/target/checkstyle-result.xml',
                unstableTotalAll: '0'])

          // TODO set up maven-pmd-plugin for PMD/CPD
          //step([$class: 'PmdPublisher', pattern: '**/target/pmd.xml', unstableTotalAll:'0'])
          //step([$class: 'DryPublisher', canComputeNew: false, defaultEncoding: '', healthy: '', pattern: '**/cpd/cpdCheck.xml', unHealthy: ''])

          // TODO reduce unstableTotal thresholds as bugs are eliminated
          step([$class: 'FindBugsPublisher',
                pattern: '**/findbugsXml.xml',
                unstableTotalAll: '0'])

          step([$class: 'WarningsPublisher',
                consoleParsers: [
                        [parserName: 'Java Compiler (javac)'],
                        [parserName: 'kotlin'],
//                        [parserName: 'JavaDoc'],
//                        [parserName: 'Maven'], // ~279 warnings, but too variable
                        // TODO check integration test warnings (EAP and WildFly)
                        //[parserName: 'appserver log messages'], // 119 warnings
                        //[parserName: 'browser warnings'],       // 0 warnings
                ],
                unstableTotalAll: '350',
                unstableTotalHigh: '0',
          ])
          // TODO check integration test warnings (EAP and WildFly)
//          step([$class: 'WarningsPublisher',
//                parserConfigurations: [
//                        [parserName: 'Flaky Tests',
//                         pattern: '**/target/*-reports/TEST-*.xml']
//                ],
//                // there is exactly one deliberately flaky test
//                unstableTotalAll: '1',
//          ])
          // this should appear after all other static analysis steps
          step([$class: 'AnalysisPublisher'])
        }

        // gather built files to use in following pipeline stages (on
        // other build nodes)
        stage('Stash') {
          stash name: 'generated-files',
                  includes: '**/target/**, **/src/main/resources/**,**/.zanata-cache/**'
        }
        // Reduce workspace size
        sh "git clean -fdx"
      } catch (e) {
        echo("Caught exception: " + e)
        notify.error(e)
        currentBuild.result = 'FAILURE'
        // abort the rest of the pipeline
        throw e
      }
    }
  }

  // if the build is still green:
  if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
    // Only one build per lock name is allowed to run integration tests at a
    // time (unless we define multiple identical locks).
    // When there are more potential builds than locks available,
    // *newer* builds are pulled off the queue first. When a build reaches the
    // milestone at the beginning of the lock, all jobs (from the same pipeline)
    // started prior to the current build that are still waiting for the lock
    // will be aborted when they reach the milestone.
    // Note that the milestone is at the beginning of the lock, because we
    // want to abort concurrent tests before they are executed.
    // Ref: https://jenkins.io/blog/2016/10/16/stage-lock-milestone/
    // You can probably allow increased concurrency by defining more locks
    // in Jenkins system config. q.v. JENKINS-42339
    lock(resource: getLockName(), inversePrecedence: true, quantity: 1) {
      milestone()
      // NB all the parallel tasks must be in one stage, because you can't use `stage` inside `parallel`.
      // See https://issues.jenkins-ci.org/browse/JENKINS-34696 and https://issues.jenkins-ci.org/browse/JENKINS-33185
      stage('Integration tests') {
        // define tasks which will run in parallel
        def tasks = [
                "WILDFLY" : { integrationTests('wildfly8') },
                "JBOSSEAP": { integrationTests('jbosseap6') },
                // abort other tasks (for faster feedback) as soon as one fails
                // disabled; not currently handled by pipeline-unit
//              failFast: true
        ]
        // run integration test tasks in parallel
        parallel tasks

        node() {
            // when build is *still* green after running integration tests
            // GitHubCommitStatusSetter need to be inside a node
            notify.finish()
            currentBuild.result = (currentBuild.result) ?: 'SUCCESS'
        }
        // TODO in case of failure, notify culprits via IRC, email and/or Rocket.Chat
        // https://wiki.jenkins-ci.org/display/JENKINS/Email-ext+plugin#Email-extplugin-PipelineExamples
        // http://stackoverflow.com/a/39535424/14379
        // IRC: https://issues.jenkins-ci.org/browse/JENKINS-33922
      }
    }
  }
}

void integrationTests(String appserver) {
  def failsafeTestReports='target/failsafe-reports/TEST-*.xml'
  node(getLabel()) {
    echo "running on node ${env.NODE_NAME}"
    echo "WORKSPACE=${env.WORKSPACE}"
    checkout scm
    // Clean the workspace
    sh "git clean -fdx"

    unstash 'generated-files'

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
        if (env.BRANCH_NAME in mainlineBranches || resolveAllFuncTests()) {
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
            ./run-clean.sh ./mvnw -e -V -T 1 \
            -Dbuildtime.output.log \
            install \
            --batch-mode \
            --update-snapshots \
            -Dappserver=$appserver \
            -Dwebdriver.display=${env.DISPLAY} \
            -Dwebdriver.type=chrome \
            -Dwebdriver.chrome.driver=/opt/chromedriver \
            ${ftOpts}
        """
        /* TODO
        -Dassembly.skipAssembly \
        -DskipAppassembler \
        -DskipShade \
         */

        // retain traceability report
        archive(includes: "server/functional-test/target/**/traceability.json")

        if (mvnResult != 0) {
          notify.testResults(appserver, 'UNSTABLE', 'Failed maven build for integration tests')
          currentBuild.result = 'UNSTABLE'

          // gather db/app logs and screenshots to help debugging
          archive(
                  includes: 'server/functional-test/target/**/*.log,server/functional-test/target/screenshots/**',
                  excludes: '**/BACKUP-*.log')
        } else {
          notify.testResults(appserver, 'SUCCESS')
        }

        echo "Capturing JUnit results"
        if (setJUnitPrefix(appserver, failsafeTestReports)) {
          junit(testResults: "**/${failsafeTestReports}",
                  // NB: if this is enabled, make sure (a) max history in Jenkins
                  // Configuration is small (eg 3) or
                  // (b) https://issues.jenkins-ci.org/browse/JENKINS-33168 is fixed.
                  testDataPublishers: [[$class: 'StabilityTestDataPublisher']]
          )
          // Reduce workspace size
          sh "git clean -fdx"
        } else {
          notify.error("No integration test result for $appserver")
          currentBuild.result = 'FAILURE'
          error "no integration test results for $appserver"
        }
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
