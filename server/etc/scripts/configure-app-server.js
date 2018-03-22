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
 * jboss-cli-jjs --language=es6 -scripting -strict configure-app-server.js -- [OPTION]...
 *
 * This script uses the JBoss CLI API to configure JBoss EAP or WildFly
 * to run Zanata. Run with option --help to see all available options.
 *
 * Derived from zanata-config-test-common.cli and zanata-config-arq-test.cli
 * by Patrick Huang. Converted to Nashorn JavaScript by Sean Flanigan.
 *
 * NB this script uses jjs Nashorn scripting extensions (not vanilla ES5/ES6)
 * and should be run with these jjs options: --language=es6 -scripting -strict
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
function padRight(str, paddingValue) {
  return String(str + paddingValue).slice(0, paddingValue.length)
}

const serverConfig = 'standalone-full-ha.xml'

const help = {
  '--help':          'Show usage information',
  '--dry-run':       'Show CLI commands without executing',
  '--datasource':    'Create a datasource (for Arquillian tests)',
  '--list-commands': 'Output a list of commands instead of executing them (use with jboss-cli.sh --commands',
  '--verbose':       'Report errors for tryExec commands',
  '--quiet':         "Don't output anything unless there is an error"
}

function usage() {
  echo('Usage: zanataConfigTest.js [OPTIONS]')
  for (let opt in help) {
    const left = padRight(opt, '                 ')
    const text = help[opt]
    echo("  ${left}${text}")
  }
}

const options = {
  dryRun: false,
  createDataSource: false,
  listCommands: false,
  verbose: false,
  quiet: false
}

for each (let arg in $ARG) {
  switch(arg) {
    case '--dry-run':
      options.dryRun = true
      break
    case '--datasource':
      options.createDatasource = true
      break
    case '--list-commands':
      options.listCommands = true
      break
    case '--verbose':
      options.verbose = true
      break
    case '--quiet':
      options.quiet = true
      break
    case '--help':
      usage()
      exit()
    default:
      echo("bad option: ${arg}")
      usage()
      exit()
  }
}

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
  if (options.verbose) echo("exec: ${command}")
  if (!options.dryRun) {
    const res = jbossCli.cmd(command)
    if (!res.success) throw new Error(res.response.toString())
    //return res.response
  }
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
  if (options.verbose) echo("tryExec: ${command}")
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
  } catch (e if e instanceof java.lang.IllegalArgumentException) {
    if (options.verbose) {
      echo("Ignoring exception: ${e}")
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
      echo("Ignoring error: ${res.response}")
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

/**
 * @param {string} logger
 * @param {string} level
 * @return {void}
 */
function logger(logger, level) {
  tryExec('/subsystem=logging/logger=' + logger + ':remove')
  exec('/subsystem=logging/logger=' + logger + ':add')
  exec('/subsystem=logging/logger=' + logger + ':write-attribute(name=level,value=' + level + ')')
}

if (!options.quiet) echo("Starting embedded server")

if (options.verbose) {
  exec("embed-server --std-out=echo --server-config=${serverConfig}")
} else {
  exec("embed-server --server-config=${serverConfig}")
}

if (!options.quiet) echo("Applying configuration")

try {

  // ==== system properties /system-property=foo:add(value=bar) ====
  systemProperty('zanata.javaScriptTestHelper', true)
  systemProperty('jboss.as.management.blocking.timeout', 1000)
  systemProperty('javamelody.storage-directory', '${jboss.server.data.dir}/zanata/stats')
  systemProperty('hibernate.search.default.indexBase', '${jboss.server.data.dir}/zanata/indexes')
  systemProperty('zanata.home', '${jboss.server.data.dir}/zanata')
  systemProperty('zanata.security.authpolicy.internal', 'zanata.internal')
  systemProperty('zanata.security.authpolicy.openid', 'zanata.openid')
  systemProperty('zanata.email.defaultfromaddress', 'no-reply@zanata.org')
  systemProperty('zanata.file.directory', './target/documents')
  systemProperty('zanata.support.oauth', true)
  systemProperty('zanata.security.adminusers', 'admin')
  systemProperty('virusScanner', 'DISABLED')

  // ==== cached connection manager ====
  exec('/subsystem=jca/cached-connection-manager=cached-connection-manager/:write-attribute(name=debug,value=true)')
  // NB we don't want error=true in production, but we use it here to help catch connection leaks during tests
  exec('/subsystem=jca/cached-connection-manager=cached-connection-manager/:write-attribute(name=error,value=true)')

  // ==== logging configuration ====
  // /subsystem=logging/logger=LOG_CATEGORY:add
  // /subsystem=logging/logger=LOG_CATEGORY:write-attribute(name=level,value=DEBUG)
  // /subsystem=logging/logger=LOG_CATEGORY:write-attribute(name=filter-spec, value=FILTER_EXPRESSION)

  // default file logging (server.log) unchanged
  // Allow stdout handler to handle INFO logging
  // We use tryExec(because CONSOLE may not exist (eg standalone-full-ha.xml on EAP 7))
  tryExec('/subsystem=logging/console-handler=CONSOLE:write-attribute(name=level,value=INFO)')

  // Disable some startup warnings triggered by third-party jars

  logger('org.jboss.as.server.deployment', 'INFO')
  exec('/subsystem=logging/logger=org.jboss.as.server.deployment:write-attribute(name=filter-spec, value="not(any( match(\\"JBAS015960\\"), match(\\"JBAS015893\\") ))")')

  // Disable WARN about GWT's org.hibernate.validator.ValidationMessages
  logger('org.jboss.modules', 'ERROR')

  // Disable WARN: "RP discovery / realm validation disabled;"
  logger('org.openid4java.server.RealmVerifier', 'ERROR')

  // Disable WARN: "JMS API was found on the classpath...
  logger('org.richfaces.log.Application', 'ERROR')
  exec('/subsystem=logging/logger=org.richfaces.log.Application:write-attribute(name=filter-spec, value="not( match(\\"JMS API was found on the classpath\\") )")')

  // Disable WARN: "Queue with name '...' has already been registered"
  logger('org.richfaces.log.Components', 'ERROR')

  // Enable more CDI weld error logging when exceptions are caught
  logger('org.jboss.weld', 'DEBUG')
  exec('/subsystem=logging/logger=org.jboss.weld:write-attribute(name=filter-spec, value="any(match(\\".*Catching.*\\"), levelRange[INFO, FATAL])")')

  // Log Hibernate SQL statements to server.log
  // /subsystem=logging/logger=org.hibernate.SQL:add
  // /subsystem=logging/logger=org.hibernate.SQL:write-attribute(name=level,value=DEBUG)
  // Log Hibernate SQL parameter values to server.log
  // /subsystem=logging/logger=org.hibernate.type.descriptor.sql:add
  // /subsystem=logging/logger=org.hibernate.type.descriptor.sql:write-attribute(name=level,value=TRACE)
  // /subsystem=logging/logger=jboss.jdbc.spy:add
  // /subsystem=logging/logger=jboss.jdbc.spy:write-attribute(name=level,value=TRACE)


  // ==== infinispan ====
  tryExec('/subsystem=infinispan/cache-container=zanata:remove')
  exec('/subsystem=infinispan/cache-container=zanata:add(module="org.jboss.as.clustering.web.infinispan",jndi-name="java:jboss/infinispan/container/zanata",statistics-enabled="true")')

  // NB for HA, we should probably use replicated-cache, not local-cache
  exec('/subsystem=infinispan/cache-container=zanata/local-cache=default:add(statistics-enabled="true")')
  exec('/subsystem=infinispan/cache-container=zanata/local-cache=default/transaction=TRANSACTION:add(mode="NON_XA")')
  exec('/subsystem=infinispan/cache-container=zanata/local-cache=default/eviction=EVICTION:add(max-entries="10000",strategy="LRU")')
  exec('/subsystem=infinispan/cache-container=zanata/local-cache=default/expiration=EXPIRATION:add(max-idle="100000")')

  exec('/subsystem=infinispan/cache-container=zanata:write-attribute(name="default-cache",value="default")')

  // ==== message queue ====
  tryExec('jms-queue remove --queue-address=MailsQueue')
  exec('jms-queue add --queue-address=MailsQueue --durable=true --entries=["java:/jms/queue/MailsQueue"]')

  // ==== security ====
  // add zanata security domain
  tryExec('/subsystem=security/security-domain=zanata:remove')
  exec('/subsystem=security/security-domain=zanata:add')
  exec('/subsystem=security/security-domain=zanata/authentication=classic:add')
  exec('/subsystem=security/security-domain=zanata/authentication=classic/login-module=ZanataCentralLoginModule:add(code="org.zanata.security.ZanataCentralLoginModule",flag="required")')
  // add zanata.internal security domain
  tryExec('/subsystem=security/security-domain=zanata.internal:remove')
  exec('/subsystem=security/security-domain=zanata.internal:add')
  exec('/subsystem=security/security-domain=zanata.internal/authentication=classic:add')
  exec('/subsystem=security/security-domain=zanata.internal/authentication=classic/login-module=ZanataInternalLoginModule:add(code="org.zanata.security.jaas.InternalLoginModule",flag="required")')
  // add zanata.openid security domain
  tryExec('/subsystem=security/security-domain=zanata.openid:remove')
  exec('/subsystem=security/security-domain=zanata.openid:add')
  exec('/subsystem=security/security-domain=zanata.openid/authentication=classic:add')
  exec('/subsystem=security/security-domain=zanata.openid/authentication=classic/login-module=ZanataOpenIdLoginModule:add(code="org.zanata.security.OpenIdLoginModule",flag="required")')

  // ==== mail session ====
  exec('/socket-binding-group=standard-sockets/remote-destination-outbound-socket-binding=mail-smtp:write-attribute(name="port", value=${smtp.port,env.SMTP_PORT:2552})')
  exec('/socket-binding-group=standard-sockets/socket-binding=management-http:write-attribute(name="port", value=${jboss.management.http.port,env.JBOSS_MANAGEMENT_HTTP_PORT:10090})')
  exec('/socket-binding-group=standard-sockets/socket-binding=management-https:write-attribute(name="port", value=${jboss.management.http.port,env.JBOSS_MANAGEMENT_HTTPS_PORT:10093})')
  exec('/socket-binding-group=standard-sockets/socket-binding=ajp:write-attribute(name="port", value=${jboss.ajp.port,env.JBOSS_AJP_PORT:8109})')
  exec('/socket-binding-group=standard-sockets/socket-binding=http:write-attribute(name="port", value=${jboss.http.port,env.JBOSS_HTTP_PORT:8180})')
  exec('/socket-binding-group=standard-sockets/socket-binding=https:write-attribute(name="port", value=${jboss.https.port,env.JBOSS_HTTPS_PORT:8543})')
//  exec('/socket-binding-group=standard-sockets/socket-binding=iiop:write-attribute(name="port", value=${jboss.iiop.port,env.JBOSS_IIOP_PORT:3628})')
//  exec('/socket-binding-group=standard-sockets/socket-binding=iiop-ssl:write-attribute(name="port", value=${jboss.iiop.ssl.port,env.JBOSS_IIOP_SSL_PORT:3629})')
  exec('/socket-binding-group=standard-sockets/socket-binding=txn-recovery-environment:write-attribute(name="port", value=${jboss.txn.recovery.port,env.JBOSS_TXN_RECOVERY_PORT:4812})')
  exec('/socket-binding-group=standard-sockets/socket-binding=txn-status-manager:write-attribute(name="port", value=${jboss.txn.status.port,env.JBOSS_TXN_STATUS_PORT:4813})')

  //  ==== disable CORBA ====
  tryExec('/socket-binding-group=standard-sockets/socket-binding=iiop:remove')
  tryExec('/socket-binding-group=standard-sockets/socket-binding=iiop-ssl:remove')
  tryExec('/subsystem=iiop-openjdk:remove')
  tryExec('/extension=org.wildfly.iiop-openjdk:remove')

  if (options.createDatasource) {
    // we are running arquillian tests. need to add the datasource
    tryExec('data-source remove --name=zanataDatasource')
    exec(<<EOF
      data-source add --name=zanataDatasource
        --jndi-name=java:jboss/datasources/zanataDatasource --driver-name=h2
        --connection-url=jdbc:h2:mem:zanata;DB_CLOSE_DELAY=-1
        --user-name=sa --password=sa
        --validate-on-match=false --background-validation=false
        --valid-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.novendor.JDBC4ValidConnectionChecker
        --exception-sorter-class-name=org.jboss.jca.adapters.jdbc.extensions.novendor.NullExceptionSorter
        --use-ccm=true
    EOF)
  }
  if (!options.quiet) echo("Configuration complete")

} finally {
  if (!options.quiet) echo("Stopping embedded server")
  exec('stop-embedded-server')
}
