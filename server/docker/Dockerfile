# TODO rename Dockerfile to Dockerfile.zanata-base

FROM jboss/wildfly:10.1.0.Final

# MAIL_USERNAME and MAIL_PASSWORD can not be empty as they are used in wildfly standalone-full*.xml.
# If the smtp server does not require authentication, these single space values will be used
ENV DB_HOSTNAME=zanatadb MAIL_HOST=localhost MAIL_USERNAME=' ' MAIL_PASSWORD=' '

# create mysql module
USER root
COPY conf/mysql-module/ /opt/jboss/wildfly/modules/
RUN yum -y install mysql-connector-java && yum clean all && \
    ln -sf /usr/share/java/mysql-connector-java.jar /opt/jboss/wildfly/modules/com/mysql/main/mysql-connector-java.jar

USER jboss

COPY target/jboss-cli-jjs target/configure-app-server.js /tmp/

# Uses JAVA_HOME and JBOSS_HOME vars from base image
RUN /tmp/jboss-cli-jjs --language=es6 /tmp/configure-app-server.js -- \
    --auth-internal --auth-openid --auth-saml2 --datasource \
    --disable-file-logger --oauth --rundev --machine-translation

# override the sun jdk module file to support java melody (com.sun.management.* for heap dump)
# TODO remove this when https://github.com/javamelody/javamelody/issues/585 is resolved
RUN sed -i '/<\/paths>/ i <path name="com/sun/management"/>' /opt/jboss/wildfly/modules/system/layers/base/sun/jdk/main/module.xml

# Fix for: WFLYCTL0056: Could not rename /opt/jboss/wildfly/standalone/configuration/standalone_xml_history/current to
RUN rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history

# Enable debugging of the appserver: --debug
# Use standandlone-full: -c standalone-full.xml
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "--debug", "-b", "0.0.0.0", "-c", "standalone-full-ha.xml"]
