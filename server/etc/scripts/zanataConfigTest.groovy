/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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

import org.jboss.as.cli.scriptsupport.CLI
import groovy.transform.Field

/**
 * Derived from zanata-config-test-common.cli and zanata-config-arq-test.cli
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
String serverConfig = "standalone-full-ha.xml"

def cli = new CliBuilder(usage: 'runJbossCli.groovy -[hndcv]')
// Create the list of options.
cli.with {
  h longOpt: 'help',          'Show usage information'
  n longOpt: 'dry-run',       'Show CLI commands without executing'
  d longOpt: 'datasource',    'Create a datasource (for Arquillian tests)'
  c longOpt: 'list-commands', 'Output a list of commands instead of executing them (use with jboss-cli.sh --commands'
  v longOpt: 'verbose',       'Report errors for tryExec commands'
}

@Field def options
options = cli.parse(args)
if (!options) {
  println "error"
  return
}
if (options.help) {
  cli.usage()
  return
}

boolean createDatasource = options.datasource

@Field CLI jbossCli = CLI.newInstance()
void exec(String command) {
  if (options.'list-commands') {
    println command
    return
  }
  println("exec: ${command}")
  if (!options.'dry-run') {
    def res = jbossCli.cmd(command)
    if (!res.isSuccess) throw new Exception(res.response.toString())
    //return res.response
  }
}
void tryExec(String command) {
  if (options.'list-commands') {
    println command
    return
  }
  try {
    println("tryExec: ${command}")
    if (!options.'dry-run') {
      def res = jbossCli.cmd(command)
      if (res.isSuccess) {
        return //res.response
      } else if (options.verbose) {
        if (res.isLocalCommand) {
          println("Ignoring error")
        } else {
          println("Ignoring error: ${res.response}")
        }
      }
    }
  } catch (IllegalArgumentException e) {
    if (options.verbose) {
      println("Ignoring exception: $e")
    }
  }
}

// jboss cli specification for an operation request
// [/node-type=node-name (/node-type=node-name)*] : operation-name [( [parameter-name=parameter-value (,parameter-name=parameter-value)*] )]

void systemProperty(String name, def value) {
  tryExec '/system-property="' + name + '":remove'
  exec '/system-property="' + name + '":add(value="' + value + '")'
}

void logger(String logger, String level) {
  tryExec '/subsystem=logging/logger=' + logger + ':remove'
  exec '/subsystem=logging/logger=' + logger + ':add'
  exec '/subsystem=logging/logger=' + logger + ':write-attribute(name=level,value=' + level + ')'
}


exec "embed-server --std-out=echo --server-config=$serverConfig"

try {

  // ==== system properties /system-property=foo:add(value=bar) ====
  systemProperty 'zanata.javaScriptTestHelper', true
  systemProperty 'jboss.as.management.blocking.timeout', 1000
  systemProperty 'javamelody.storage-directory', '${jboss.server.data.dir}/zanata/stats'
  systemProperty 'hibernate.search.default.indexBase', '${jboss.server.data.dir}/zanata/indexes'
  systemProperty 'zanata.home', '${jboss.server.data.dir}/zanata'
  systemProperty 'zanata.security.authpolicy.internal', 'zanata.internal'
  systemProperty 'zanata.security.authpolicy.openid', 'zanata.openid'
  systemProperty 'zanata.email.defaultfromaddress', 'no-reply@zanata.org'
  systemProperty 'zanata.file.directory', './target/documents'
  systemProperty 'zanata.support.oauth', true
  systemProperty "zanata.security.adminusers", 'admin'
  systemProperty "virusScanner", 'DISABLED'

  // ==== cached connection manager ====
  exec '/subsystem=jca/cached-connection-manager=cached-connection-manager/:write-attribute(name=debug,value=true)'
  // NB we don't want error=true in production, but we use it here to help catch connection leaks during tests
  exec '/subsystem=jca/cached-connection-manager=cached-connection-manager/:write-attribute(name=error,value=true)'

  // ==== logging configuration ====
  // /subsystem=logging/logger=LOG_CATEGORY:add
  // /subsystem=logging/logger=LOG_CATEGORY:write-attribute(name=level,value=DEBUG)
  // /subsystem=logging/logger=LOG_CATEGORY:write-attribute(name=filter-spec, value=FILTER_EXPRESSION)

  // default file logging (server.log) unchanged
  // Allow stdout handler to handle INFO logging
  // We use tryExec because CONSOLE may not exist (eg standalone-full-ha.xml on EAP 7)
  tryExec '/subsystem=logging/console-handler=CONSOLE:write-attribute(name=level,value=INFO)'

  // Disable some startup warnings triggered by third-party jars

  logger('org.jboss.as.server.deployment', 'INFO')
  exec '/subsystem=logging/logger=org.jboss.as.server.deployment:write-attribute(name=filter-spec, value="not(any( match(\\"JBAS015960\\"), match(\\"JBAS015893\\") ))")'

  // Disable WARN about GWT's org.hibernate.validator.ValidationMessages
  logger('org.jboss.modules', 'ERROR')

  // Disable WARN: "RP discovery / realm validation disabled;"
  logger('org.openid4java.server.RealmVerifier', 'ERROR')

  // Disable WARN: "JMS API was found on the classpath...
  logger('org.richfaces.log.Application', 'ERROR')
  exec '/subsystem=logging/logger=org.richfaces.log.Application:write-attribute(name=filter-spec, value="not( match(\\"JMS API was found on the classpath\\") )")'

  // Disable WARN: "Queue with name '...' has already been registered"
  logger('org.richfaces.log.Components', 'ERROR')

  // Enable more CDI weld error logging when exceptions are caught
  logger('org.jboss.weld', 'DEBUG')
  exec '/subsystem=logging/logger=org.jboss.weld:write-attribute(name=filter-spec, value="any(match(\\".*Catching.*\\"), levelRange[INFO, FATAL])")'

  // Log Hibernate SQL statements to server.log
  // /subsystem=logging/logger=org.hibernate.SQL:add
  // /subsystem=logging/logger=org.hibernate.SQL:write-attribute(name=level,value=DEBUG)
  // Log Hibernate SQL parameter values to server.log
  // /subsystem=logging/logger=org.hibernate.type.descriptor.sql:add
  // /subsystem=logging/logger=org.hibernate.type.descriptor.sql:write-attribute(name=level,value=TRACE)
  // /subsystem=logging/logger=jboss.jdbc.spy:add
  // /subsystem=logging/logger=jboss.jdbc.spy:write-attribute(name=level,value=TRACE)


  // ==== infinispan ====
  tryExec '/subsystem=infinispan/cache-container=zanata:remove'
  exec '/subsystem=infinispan/cache-container=zanata:add(module="org.jboss.as.clustering.web.infinispan",default-cache="default",jndi-name="java:jboss/infinispan/container/zanata",statistics-enabled="true")'
  exec '/subsystem=infinispan/cache-container=zanata/local-cache=default:add(statistics-enabled="true")'
  exec '/subsystem=infinispan/cache-container=zanata/local-cache=default/transaction=TRANSACTION:add(mode="NON_XA")'
  exec '/subsystem=infinispan/cache-container=zanata/local-cache=default/eviction=EVICTION:add(max-entries="10000",strategy="LRU")'
  exec '/subsystem=infinispan/cache-container=zanata/local-cache=default/expiration=EXPIRATION:add(max-idle="100000")'

  // ==== message queue ====
  tryExec 'jms-queue remove --queue-address=MailsQueue'
  exec 'jms-queue add --queue-address=MailsQueue --durable=true --entries=["java:/jms/queue/MailsQueue"]'

  // ==== security ====
  // add zanata security domain
  tryExec '/subsystem=security/security-domain=zanata:remove'
  exec '/subsystem=security/security-domain=zanata:add'
  exec '/subsystem=security/security-domain=zanata/authentication=classic:add'
  exec '/subsystem=security/security-domain=zanata/authentication=classic/login-module=ZanataCentralLoginModule:add(code="org.zanata.security.ZanataCentralLoginModule",flag="required")'
  // add zanata.internal security domain
  tryExec '/subsystem=security/security-domain=zanata.internal:remove'
  exec '/subsystem=security/security-domain=zanata.internal:add'
  exec '/subsystem=security/security-domain=zanata.internal/authentication=classic:add'
  exec '/subsystem=security/security-domain=zanata.internal/authentication=classic/login-module=ZanataInternalLoginModule:add(code="org.zanata.security.jaas.InternalLoginModule",flag="required")'
  // add zanata.openid security domain
  tryExec '/subsystem=security/security-domain=zanata.openid:remove'
  exec '/subsystem=security/security-domain=zanata.openid:add'
  exec '/subsystem=security/security-domain=zanata.openid/authentication=classic:add'
  exec '/subsystem=security/security-domain=zanata.openid/authentication=classic/login-module=ZanataOpenIdLoginModule:add(code="org.zanata.security.OpenIdLoginModule",flag="required")'

  // ==== mail session ====
  exec '/socket-binding-group=standard-sockets/remote-destination-outbound-socket-binding=mail-smtp:write-attribute(name="port", value=${smtp.port,env.SMTP_PORT:2552})'
  exec '/socket-binding-group=standard-sockets/socket-binding=management-http:write-attribute(name="port", value=${jboss.management.http.port,env.JBOSS_MANAGEMENT_HTTP_PORT:10090})'
  exec '/socket-binding-group=standard-sockets/socket-binding=management-https:write-attribute(name="port", value=${jboss.management.http.port,env.JBOSS_MANAGEMENT_HTTPS_PORT:10093})'
  exec '/socket-binding-group=standard-sockets/socket-binding=ajp:write-attribute(name="port", value=${jboss.ajp.port,env.JBOSS_AJP_PORT:8109})'
  exec '/socket-binding-group=standard-sockets/socket-binding=http:write-attribute(name="port", value=${jboss.http.port,env.JBOSS_HTTP_PORT:8180})'
  exec '/socket-binding-group=standard-sockets/socket-binding=https:write-attribute(name="port", value=${jboss.https.port,env.JBOSS_HTTPS_PORT:8543})'
//  exec '/socket-binding-group=standard-sockets/socket-binding=iiop:write-attribute(name="port", value=${jboss.iiop.port,env.JBOSS_IIOP_PORT:3628})'
//  exec '/socket-binding-group=standard-sockets/socket-binding=iiop-ssl:write-attribute(name="port", value=${jboss.iiop.ssl.port,env.JBOSS_IIOP_SSL_PORT:3629})'
  exec '/socket-binding-group=standard-sockets/socket-binding=txn-recovery-environment:write-attribute(name="port", value=${jboss.txn.recovery.port,env.JBOSS_TXN_RECOVERY_PORT:4812})'
  exec '/socket-binding-group=standard-sockets/socket-binding=txn-status-manager:write-attribute(name="port", value=${jboss.txn.status.port,env.JBOSS_TXN_STATUS_PORT:4813})'

  //  ==== disable CORBA ====
  tryExec '/socket-binding-group=standard-sockets/socket-binding=iiop:remove'
  tryExec '/socket-binding-group=standard-sockets/socket-binding=iiop-ssl:remove'
  tryExec '/subsystem=iiop-openjdk:remove'
  tryExec '/extension=org.wildfly.iiop-openjdk:remove'

  if (createDatasource) {
    // we are running arquillian tests. need to add the datasource
    tryExec "data-source remove --name=zanataDatasource"
    exec """data-source add --name=zanataDatasource \
      --jndi-name=java:jboss/datasources/zanataDatasource --driver-name=h2 \
      --connection-url=jdbc:h2:mem:zanata;DB_CLOSE_DELAY=-1 \
      --user-name=sa --password=sa \
      --validate-on-match=false --background-validation=false \
      --valid-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.novendor.JDBC4ValidConnectionChecker \
      --exception-sorter-class-name=org.jboss.jca.adapters.jdbc.extensions.novendor.NullExceptionSorter \
      --use-ccm=true"""
  }

} finally {
  exec "stop-embedded-server"
}
