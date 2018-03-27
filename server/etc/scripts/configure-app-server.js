//@ts-check
/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */


/*
 * Script name: configure-app-server.js
 * Run it like this:
 * jboss-cli-jjs --language=es6 configure-app-server.js -- [OPTION]...
 *
 * This script uses the JBoss CLI API to configure JBoss EAP or WildFly
 * to run Zanata. Run with option --help to see all available options.
 *
 * Derived from zanata-config-test-common.cli and zanata-config-arq-test.cli
 * by Patrick Huang. Converted to Nashorn JavaScript by Sean Flanigan.
 *
 * NB this script uses jjs Nashorn scripting extensions (not vanilla ES5/ES6)
 * and should be run with these jjs options: --language=es6 -scripting
 *
 * Requires org.jboss.as.cli on the module-path (preferably) or
 * wildfly-cli-4.0.0.Final-client.jar on the classpath (some cli commands
 * won't work). Running via jboss-cli-jjs will provide a suitable module
 * environment.
 *
 * API Ref: https://github.com/wildfly/wildfly-core/blob/4.0.0.Final/cli/src/main/java/org/jboss/as/cli/scriptsupport/CLI.java
 *
 * Author: Patrick Huang <pahuang@redhat.com>
 * Author: Sean Flanigan <sflaniga@redhat.com>
 */

'use strict';

// This always points to the "real" stdout, even if we reassign System.out
const stdout = java.lang.System.out

/**
 * @param {object} o
 */
function echo(o) {
  // sometimes Nashorn seems to pass 'o' as a java.lang.Character
  stdout.println(o.toString())
}

/**
 * @param {string} str
 * @param {string} paddingValue
 */
function padRight(str, paddingValue) {
  return String(str + paddingValue).slice(0, paddingValue.length)
}

const serverConfig = 'standalone-full-ha.xml'

const help = {
  '--help':                 'Show usage information',
  '--dry-run':              'Show CLI commands without executing',
  '--datasource-h2':        'Create a datasource (for Arquillian tests)',
  '--auth-internal':        "Enable Zanata's internal authentication",
  '--auth-openid':          'Enable OpenID authentication',
  '--auth-openid-provider': 'Enable OpenID and restrict to a single provider URL. eg for Fedora Account System: --auth-openid-provider https://id.fedoraproject.org/openid/',
  '--auth-saml2':           'Enable SAML2 authentication (SSO)',
  '--integration-test':     'Enable integration test configuration',
  '--list-commands':        'Output a list of commands instead of executing them (use with jboss-cli.sh --commands',
  '--oauth':                'Enable OAuth logins for Zanata clients (experimental)',
  '--rundev':               'Enable options for development with rundev.sh',
  '--verbose':              'Report errors for tryExec commands',
  '--quiet':                "Don't output anything unless there is an error"
}

function usage() {
  echo('Usage: configure-app-server.js [OPTIONS]')
  echo('Note that you must enable at least one authentication option.')
  echo('Some combinations are not supported by Zanata.')
  for (let opt in help) {
    const nameWithPad = padRight(opt, '                       ')
    const desc = help[opt]
    echo('  ' + nameWithPad + desc)
  }
}

/**
 * @template T
 * @param {T[]} arr
 * @param {T[]} elems
 */
function pushAll(arr, elems) {
  Array.prototype.push.apply(arr, elems)
}

/**
 *
 * @param {string[]} args
 */
function parseArgs(args) {
  const opts = {
    dryRun: false,
    listCommands: false,
    verbose: false,
    quiet: false,
    // rundev options for rundev.sh
    runDev: false,
    /** @type {(() => void)[]} */
    configCallbacks: [],
    /** @type {'WARN'|'INFO'|'TRACE'|undefined} */
    consoleLogLevel: undefined,
    // The system properties are expanded by the app server (not by JS).
    zanataHomeDir: '${jboss.server.data.dir}/zanata',
    searchIndexDir: '${zanata.home}/indexes',
    javamelodyDir: '${zanata.home}/stats',
    fileDir: '${zanata.home}/files'
  }
  let enabledAuth = false

  for (let i = 0; i < args.length; i++) {
    const arg = args[i]
    switch(arg) {
      case '--dry-run':
        echo('Dry run: no configuration will actually be changed')
        opts.dryRun = true
        break
      case '--auth-internal':
        echo('Enabling internal authentication')
        opts.configCallbacks.push(configureAuthInternal)
        enabledAuth = true
        break
      case '--auth-openid':
        echo('Enabling OpenID authentication')
        opts.configCallbacks.push(configureAuthOpenId)
        enabledAuth = true
        break
      case '--auth-openid-provider': {
        echo('Enabling OpenID authentication (single Provider)')
        ++i
        if (i >= args.length) {
          echo('Missing argument\nTry --help for help')
          exit(1)
        }
        const url = args[i]
        opts.configCallbacks.push(
          configureAuthOpenId,
          function() {
            return configureOpenIdProvider(url)
          })
        enabledAuth = true
        break
      }
      case '--auth-saml2':
        echo('Enabling SAML2 authentication')
        opts.configCallbacks.push(configureAuthSaml2)
        enabledAuth = true
        break
      case '--datasource-h2':
        echo('Creating an H2 datasource for Arquillian tests')
        opts.configCallbacks.push(configureDatasourceH2)
        break
      case '--integration-test':
        echo('Enabling test-only configuration')
        opts.consoleLogLevel = 'INFO'
        opts.fileDir = './target/documents'
        pushAll(opts.configCallbacks, configCallbacksForIntegrationTest())
        break
      case '--list-commands':
        // echo('Outputting jboss cli commands to stdout')
        opts.listCommands = true
        break
      case '--oauth':
        echo('Enabling OAuth logins')
        opts.configCallbacks.push(configureOAuth)
        break
      case '--rundev':
        echo('Enabling options for development with rundev.sh')
        opts.runDev = true
        opts.consoleLogLevel = 'TRACE'
        opts.zanataHomeDir = '${user.home}/zanata'
        pushAll(opts.configCallbacks, configCallbacksForRunDev())
        break
      case '--verbose':
        echo('Verbose mode')
        opts.verbose = true
        break
      case '--quiet':
        // echo('Quiet mode')
        opts.quiet = true
        break
      case '--help':
        usage()
        exit()
      default:
        echo('Bad option: ' + arg + '\nTry --help for help')
        exit(1)
    }
  }
  if (!enabledAuth) {
    echo('Please enable at least one authentication method\nTry --help for help')
    exit(1)
  }
  return opts
}

function configCallbacksForRunDev() {
  return [
    configureDatasourceMysqlForRunDev,
    disableFileLogger,
    enableJcaConnectionErrors,
    enableLoggingForWeldExceptions,
    // enableMoreHibernateLogging,
    configureSMTPForRunDev,
    function() {
      // Allows the editor development server to use the docker server as a backend.
      // The editor development server is run with `make watch` in zanata-frontend/src/editor
      systemProperty('zanata.origin.whitelist', 'http://localhost:8000 http://localhost:8001')
    }]
}

function configCallbacksForIntegrationTest() {
  return [
    configureSocketsForIntegrationTest,
    enableJcaConnectionErrors,
    configureSMTPForIntegrationTest,
    function() {
      systemProperty('zanata.javaScriptTestHelper', true)
      systemProperty('zanata.security.adminusers', 'admin')
    }]
}

// parse the command line arguments
const options = parseArgs(arguments)

const jbossCli = org.jboss.as.cli.scriptsupport.CLI.newInstance()

/**
 * @param {string} command
 * @return {void}
 */
function exec(command) {
  if (options.listCommands) {
    echo(command)
    return
  }
  if (options.dryRun || options.verbose) echo('exec: ' + command)
  if (!options.dryRun) {
    const res = jbossCli.cmd(command)
    if (!res.success) throw new Error(res.response.toString())
    //return res.response
  }
  return
}

/**
 * @param {string} command
 * @return {void}
 */
function tryExec(command) {
  if (options.listCommands) {
    echo(command)
    return
  }
  if (options.dryRun || options.verbose) echo('tryExec: ' + command)
  if (!options.dryRun) {
    cmdIgnoreError(command)
  }
}

/**
 * @param {string} command
 * @return {void}
 */
function cmdIgnoreError(command) {
  try {
    const res = jbossCli.cmd(command)
    if (res.success) {
      return //res.response
    } else {
      ignoreResponse(res)
    }
  } catch (e) {
    if (options.verbose) {
      echo('Ignoring exception: ' + e)
    }
  }
}

/**
 * @param {org.jboss.as.cli.scriptsupport.CLI.Result} res
 * @return {void}
 */
function ignoreResponse(res) {
  if (options.verbose) {
    if (res.localCommand) {
      echo('Ignoring error')
    } else {
      echo('Ignoring error: ' + res.response)
    }
  }
}

// jboss cli specification for an operation request
// [/node-type=node-name (/node-type=node-name)*] : operation-name [( [parameter-name=parameter-value (,parameter-name=parameter-value)*] )]

/**
 * Converts 'value' to a string and uses it as the value for the specified
 * system property.
 * @param {string} name
 * @param {object} value
 * @return void
 */
function systemProperty(name, value) {
  tryExec('/system-property="' + name + '":remove')
  exec('/system-property="' + name + '":add(value="' + value + '")')
}

// These are all the log levels supported by JBoss Logger (derived from Log4J and JUL)
// https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/6.4/html/administration_and_configuration_guide/chap-the_logging_subsystem#Supported_Log_Levels

//#region log levels
const ALL = 'ALL'
const CONFIG = 'CONFIG'
const DEBUG = 'DEBUG'
const ERROR = 'ERROR'
const FATAL = 'FATAL'
const FINE = 'FINE'
const FINER = 'FINER'
const FINEST = 'FINEST'
const INFO = 'INFO'
const OFF = 'OFF'
const TRACE = 'TRACE'
const WARN = 'WARN'
const WARNING = 'WARNING'
//#endregion log levels

/**
 * @param {string} logger
 * @param {'ALL'|'CONFIG'|'DEBUG'|'ERROR'|'FATAL'|'FINE'|'FINER'|'FINEST'|'INFO'|'OFF'|'TRACE'|'WARN'|'WARNING'} level
 * @param {string=} filterSpec
 * @return {void}
 */
function logger(logger, level, filterSpec) {
  tryExec('/subsystem=logging/logger=' + logger + ':remove')
  exec('/subsystem=logging/logger=' + logger + ':add')
  exec('/subsystem=logging/logger=' + logger + ':write-attribute(name=level,value=' + level + ')')
  if (filterSpec) {
    exec('/subsystem=logging/logger=' + logger + ':write-attribute(name=filter-spec,value="' + filterSpec + '")')
  }
}

function startEmbeddedServer() {
  if (!options.quiet) echo('Starting embedded server')
  const echoOrDiscard = options.verbose ? 'echo' : 'discard'
  exec('embed-server --std-out='+echoOrDiscard+' --server-config=' + serverConfig)
}

function stopEmbeddedServer() {
  if (!options.quiet) echo('Stopping embedded server')
}

// standalone-full comes with CORBA, but we don't need it
function removeIIOP() {
  tryExec('/socket-binding-group=standard-sockets/socket-binding=iiop:remove')
  tryExec('/socket-binding-group=standard-sockets/socket-binding=iiop-ssl:remove')
  tryExec('/subsystem=iiop-openjdk:remove')
  tryExec('/extension=org.wildfly.iiop-openjdk:remove')
}

function configureJmsQueues() {
  tryExec('jms-queue remove --queue-address=MailsQueue')
  exec('jms-queue add --queue-address=MailsQueue --durable=true --entries=["java:/jms/queue/MailsQueue"]')
}

function configureJcaConnectionDebugging() {
  exec('/subsystem=jca/cached-connection-manager=cached-connection-manager/:write-attribute(name=debug,value=true)')
}

function enableJcaConnectionErrors() {
  // NB we don't want error=true in production, but we use it here to help catch connection leaks during tests
  // only for dev/test
  exec('/subsystem=jca/cached-connection-manager=cached-connection-manager/:write-attribute(name=error,value=true)')
}

function configureSystemProperties() {
  // TODO create options for all system properties.

  // this is used by several other properties, so it's first
  systemProperty('zanata.home', options.zanataHomeDir)

  // ==== system properties /system-property=foo:add(value=bar) ====
  systemProperty('hibernate.search.default.indexBase', options.searchIndexDir)
  systemProperty('javamelody.storage-directory', options.javamelodyDir)
  systemProperty('jboss.as.management.blocking.timeout', 1000)
  systemProperty('virusScanner', 'DISABLED')
  systemProperty('zanata.email.defaultfromaddress', 'no-reply@zanata.org')
  systemProperty('zanata.file.directory', options.fileDir)
}

// TODO turn this into a more general option, eg for docker?
function disableFileLogger() {
  // Remove default file logging (server.log)
  tryExec('/subsystem=logging/root-logger=ROOT:remove-handler(name=FILE)')
  tryExec('/subsystem=logging/periodic-rotating-file-handler=FILE:remove')
}

/**
 * @param {'WARN'|'INFO'|'TRACE'|undefined} [consoleLogLevel='WARN']
 */
function configureLogHandlers(consoleLogLevel) {
  if (!consoleLogLevel) consoleLogLevel = 'WARN'
  // Create the CONSOLE handler if it doesn't exist (eg standalone-full-ha.xml
  // on EAP 7) - ZNTA-2281
  // We don't remove the existing CONSOLE handler in case this script is ever
  // used against running servers in future. Plus the default CONSOLE handler
  // is colour.
  tryExec('/subsystem=logging/console-handler=CONSOLE:add(formatter=PATTERN,level='+consoleLogLevel+')')
}

function suppressAnnoyingLogging() {
  // Disable some startup warnings triggered by third-party jars

  logger('org.jboss.as.server.deployment', 'INFO')
  exec('/subsystem=logging/logger=org.jboss.as.server.deployment:write-attribute(name=filter-spec, value="not(any( match(\\"JBAS015960\\"), match(\\"JBAS015893\\") ))")')

  // Disable WARN about GWT's org.hibernate.validator.ValidationMessages
  logger('org.jboss.modules', 'ERROR')

  // Disable WARN: "RP discovery / realm validation disabled;"
  logger('org.openid4java.server.RealmVerifier', 'ERROR')

  // Disable WARN: "JMS API was found on the classpath...
  logger('org.richfaces.log.Application', 'INFO')
  exec('/subsystem=logging/logger=org.richfaces.log.Application:write-attribute(name=filter-spec, value="not( match(\\"JMS API was found on the classpath\\") )")')

  // Disable WARN: "Queue with name '...' has already been registered"
  logger('org.richfaces.log.Components', 'ERROR')
}

const weldExceptionLoggers = [
  'org.jboss.weld.environment.logging.WeldEnvironmentLogger',
  'org.jboss.weld.logging.BeanLogger',
  'org.jboss.weld.logging.BeanManagerLogger',
  'org.jboss.weld.logging.BootstrapLogger',
  'org.jboss.weld.logging.ConfigurationLogger',
  'org.jboss.weld.logging.ContextLogger',
  'org.jboss.weld.logging.ConversationLogger',
  'org.jboss.weld.logging.ElLogger',
  'org.jboss.weld.logging.EventLogger',
  'org.jboss.weld.logging.InterceptorLogger',
  'org.jboss.weld.logging.MetadataLogger',
  'org.jboss.weld.logging.ReflectionLogger',
  'org.jboss.weld.logging.ResolutionLogger',
  'org.jboss.weld.logging.SerializationLogger',
  'org.jboss.weld.logging.UtilLogger',
  'org.jboss.weld.logging.ValidatorLogger',
  'org.jboss.weld.logging.VersionLogger',
  'org.jboss.weld.logging.XmlLogger',
  'org.jboss.weld.probe.ProbeLogger'
]

function enableLoggingForWeldExceptions() {
  // Enable more CDI weld error logging when exceptions are caught

  // This won't work (yet) because filters aren't inherited by sub-categories
  // logger('org.jboss.weld', DEBUG, 'any(match(\\"Catching\\"), levelRange[INFO, FATAL])')

  // These are all the Logger classes (as of weld-core 3.0.1.Final) which include
  // catchingDebug() or catchingTrace() (mostly by extending org.jboss.weld.logging.WeldLogger):

  for (let i = 0; i < weldExceptionLoggers.length; i++) {
    // logger(logger, level, filterSpec)
    logger(weldExceptionLoggers[i], DEBUG, 'any(match(\\"Catching\\"), levelRange[INFO, FATAL])')
  }

}

function enableMoreHibernateLogging() {
  throw Error()
    // This is probably too verbose.
    // Log Hibernate SQL statements to server.log
    // /subsystem=logging/logger=org.hibernate.SQL:add
    // /subsystem=logging/logger=org.hibernate.SQL:write-attribute(name=level,value=DEBUG)
    // Log Hibernate SQL parameter values to server.log
    // /subsystem=logging/logger=org.hibernate.type.descriptor.sql:add
    // /subsystem=logging/logger=org.hibernate.type.descriptor.sql:write-attribute(name=level,value=TRACE)
    // /subsystem=logging/logger=jboss.jdbc.spy:add
    // /subsystem=logging/logger=jboss.jdbc.spy:write-attribute(name=level,value=TRACE)
}

function configureCaches() {
  // ==== infinispan ====
  tryExec('/subsystem=infinispan/cache-container=zanata:remove')
  // TODO WFLYCTL0028: Attribute 'jndi-name' in the resource at address '/subsystem=infinispan/cache-container=zanata' is deprecated, and may be removed in a future version. See the attribute description in the output of the read-resource-description operation to learn more about the deprecation.
  exec('/subsystem=infinispan/cache-container=zanata:add(module="org.jboss.as.clustering.web.infinispan",jndi-name="java:jboss/infinispan/container/zanata",statistics-enabled="true")')

  // NB for HA, we should probably use replicated-cache, not local-cache
  exec('/subsystem=infinispan/cache-container=zanata/local-cache=default:add(statistics-enabled="true")')
  exec('/subsystem=infinispan/cache-container=zanata/local-cache=default/transaction=TRANSACTION:add(mode="NON_XA")')
  // TODO WFLYCTL0028: Attribute 'strategy' in the resource at address '/subsystem=infinispan/cache-container=zanata/local-cache=default/memory=object' is deprecated, and may be removed in a future version. See the attribute description in the output of the read-resource-description operation to learn more about the deprecation.
  exec('/subsystem=infinispan/cache-container=zanata/local-cache=default/eviction=EVICTION:add(max-entries="10000",strategy="LRU")')
  exec('/subsystem=infinispan/cache-container=zanata/local-cache=default/expiration=EXPIRATION:add(max-idle="100000")')

  exec('/subsystem=infinispan/cache-container=zanata:write-attribute(name="default-cache",value="default")')
}

// add top-level zanata security domain
function configureSecurity() {
  tryExec('/subsystem=security/security-domain=zanata:remove')
  exec('/subsystem=security/security-domain=zanata:add')
  exec('/subsystem=security/security-domain=zanata/authentication=classic:add')
  exec('/subsystem=security/security-domain=zanata/authentication=classic/login-module=ZanataCentralLoginModule:add(code="org.zanata.security.ZanataCentralLoginModule",flag="required")')
  if (!options.noAuthInternal) configureAuthInternal()
}

// add zanata.internal security domain
function configureAuthInternal() {
  const name = 'zanata.internal'
  systemProperty('zanata.security.authpolicy.internal', name)
  const domainPath = '/subsystem=security/security-domain='+name

  tryExec(domainPath+':remove')
  exec(domainPath+':add')
  exec(domainPath+'/authentication=classic:add')
  exec(domainPath+'/authentication=classic/login-module=ZanataInternalLoginModule:add(code="org.zanata.security.jaas.InternalLoginModule",flag="required")')
}

// add zanata.openid security domain
function configureAuthOpenId() {
  const name = 'zanata.openid'
  systemProperty('zanata.security.authpolicy.openid', name)
  const domainPath = '/subsystem=security/security-domain='+name

  tryExec(domainPath+':remove')
  exec(domainPath+':add')
  exec(domainPath+'/authentication=classic:add')
  exec(domainPath+'/authentication=classic/login-module=ZanataOpenIdLoginModule:add(code="org.zanata.security.OpenIdLoginModule",flag="required")')
}

// add zanata.saml2 security domain
function configureAuthSaml2() {
  systemProperty('picketlink.file', '${zanata.home}/picketlink.xml')
  const name = 'zanata.saml2'
  systemProperty('zanata.security.authpolicy.saml2', name)
  const domainPath = '/subsystem=security/security-domain='+name
  tryExec(domainPath+':remove')
  exec(domainPath+':add(cache-type=default)')
  exec(domainPath+'/authentication=classic:add')
  tryExec(domainPath+'/authentication=classic/login-module=org.picketlink.identity.federation.bindings.jboss.auth.SAML2LoginModule:remove')
  exec(domainPath+'/authentication=classic/login-module=org.picketlink.identity.federation.bindings.jboss.auth.SAML2LoginModule:add(code=org.picketlink.identity.federation.bindings.jboss.auth.SAML2LoginModule,flag=required)')
}

/**
 * Put the instance into single OpenID provider mode. Sign in will go straight to the OpenID provider.
 * @param {string} url
 */
function configureOpenIdProvider(url) {
  exec('/subsystem=security/security-domain=zanata.openid/authentication=classic/login-module=ZanataOpenIdLoginModule:write-attribute(name=module-options,value={providerURL="' + url + '"})')
}

function configureOAuth() {
  systemProperty('zanata.support.oauth', true)
}

function configureSMTPForRunDev() {
  tryExec('/socket-binding-group=standard-sockets/remote-destination-outbound-socket-binding=my-smtp:remove')
  exec('/socket-binding-group=standard-sockets/remote-destination-outbound-socket-binding=my-smtp:add(host=${env.MAIL_HOST:localhost}, port=${MAIL_PORT:25})')
  exec('/subsystem=mail/mail-session=default/server=smtp:write-attribute(name=outbound-socket-binding-ref,value=my-smtp)')
  // The env.MAIL_USERNAME and env.MAIL_PASSWORD value can not be empty otherwise jboss will fail to enable the mail session resource.
  // Default value set by docker will be ' ' (single space string).
  exec('/subsystem=mail/mail-session=default/server=smtp:write-attribute(name=username,value=${env.MAIL_USERNAME})')
  exec('/subsystem=mail/mail-session=default/server=smtp:write-attribute(name=password,value=${env.MAIL_PASSWORD})')
}

function configureSMTPForIntegrationTest() {
  exec('/socket-binding-group=standard-sockets/remote-destination-outbound-socket-binding=mail-smtp:write-attribute(name="port", value=${smtp.port,env.SMTP_PORT:2552})')
}

function configureSocketsForIntegrationTest() {
  exec('/socket-binding-group=standard-sockets/socket-binding=management-http:write-attribute(name="port", value=${jboss.management.http.port,env.JBOSS_MANAGEMENT_HTTP_PORT:10090})')
  exec('/socket-binding-group=standard-sockets/socket-binding=management-https:write-attribute(name="port", value=${jboss.management.http.port,env.JBOSS_MANAGEMENT_HTTPS_PORT:10093})')
  exec('/socket-binding-group=standard-sockets/socket-binding=ajp:write-attribute(name="port", value=${jboss.ajp.port,env.JBOSS_AJP_PORT:8109})')
  exec('/socket-binding-group=standard-sockets/socket-binding=http:write-attribute(name="port", value=${jboss.http.port,env.JBOSS_HTTP_PORT:8180})')
  exec('/socket-binding-group=standard-sockets/socket-binding=https:write-attribute(name="port", value=${jboss.https.port,env.JBOSS_HTTPS_PORT:8543})')
  // exec('/socket-binding-group=standard-sockets/socket-binding=iiop:write-attribute(name="port", value=${jboss.iiop.port,env.JBOSS_IIOP_PORT:3628})')
  // exec('/socket-binding-group=standard-sockets/socket-binding=iiop-ssl:write-attribute(name="port", value=${jboss.iiop.ssl.port,env.JBOSS_IIOP_SSL_PORT:3629})')
  exec('/socket-binding-group=standard-sockets/socket-binding=txn-recovery-environment:write-attribute(name="port", value=${jboss.txn.recovery.port,env.JBOSS_TXN_RECOVERY_PORT:4812})')
  exec('/socket-binding-group=standard-sockets/socket-binding=txn-status-manager:write-attribute(name="port", value=${jboss.txn.status.port,env.JBOSS_TXN_STATUS_PORT:4813})')
}

function configureDatasourceH2() {
  // we are running arquillian tests. need to add the datasource
  tryExec('data-source remove --name=zanataDatasource')
  exec(multiline(function() {/*
    data-source add --name=zanataDatasource
      --jndi-name=java:jboss/datasources/zanataDatasource --driver-name=h2
      --connection-url=jdbc:h2:mem:zanata;DB_CLOSE_DELAY=-1
      --user-name=sa --password=sa
      --validate-on-match=false --background-validation=false
      --valid-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.novendor.JDBC4ValidConnectionChecker
      --exception-sorter-class-name=org.jboss.jca.adapters.jdbc.extensions.novendor.NullExceptionSorter
      --use-ccm=true
  */}))
}

function configureDatasourceMysqlForRunDev() {
  // add driver & datasource for rundev.sh
  tryExec('/subsystem=datasources/jdbc-driver=mysql:remove')
  exec('/subsystem=datasources/jdbc-driver=mysql:add(driver-name="mysql",driver-module-name="com.mysql",driver-xa-datasource-class-name="com.mysql.jdbc.jdbc2.optional.MysqlXADataSource",driver-class-name="com.mysql.jdbc.Driver")')

  tryExec('data-source remove --name=zanataDatasource')
  exec(multiline(function() {/*
    data-source add --name=zanataDatasource
      --jndi-name=java:jboss/datasources/zanataDatasource --driver-name=mysql
      --connection-url=jdbc:mysql://${env.DB_HOSTNAME:zanatadb}:3306/${env.DB_NAME:zanata}
      --user-name=${env.DB_USER:zanata} --password=${env.DB_PASSWORD:zanatapw}
      --validate-on-match=true --background-validation=false
      --valid-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLValidConnectionChecker
      --exception-sorter-class-name=org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLExceptionSorter
      --min-pool-size=0 --max-pool-size=20 --flush-strategy=FailingConnectionOnly
      --track-statements=NOWARN --use-ccm=true
  */}))
}

/**
 * https://eli.thegreenplace.net/2013/11/09/javascript-es-5-hack-for-clean-multi-line-strings
 * @param {() => void} f
 */
function multiline(f) {
  const s = f.toString().split('\n').slice(1, -1).join('\n')
  // sanity check
  if (s.length == 0) throw new Error('empty multiline string')
  return s
}

startEmbeddedServer()
try {
  if (!options.quiet) echo('Applying configuration')
  configureSystemProperties()
  configureJcaConnectionDebugging()
  configureLogHandlers(options.consoleLogLevel)
  suppressAnnoyingLogging()
  configureCaches()
  configureJmsQueues()
  configureSecurity()
  removeIIOP()
  options.configCallbacks.forEach(function(cb) {
    cb()
  })
  if (!options.quiet) echo('Configuration complete')
} finally {
  stopEmbeddedServer()
}
// sometimes JBoss CLI seems to leave threads running, so we exit explicitly
exit()
