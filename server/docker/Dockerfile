FROM jboss/wildfly:16.0.0.Final

EXPOSE 8080
# Version should be assigned in build time
# Take care editing the ZANATA_VERSION line: just version numbers,
# otherwise runapp.sh cannot parse it.
ARG ZANATA_VERSION=4.7.4-SNAPSHOT
ARG MYSQL_CONNECTOR_JAVA_VERSION=5.1.26
ARG ZANATA_CONTEXT=zanata

# Database Setup
ENV DB_HOSTNAME zanatadb
ENV DB_PORTNUMBER 3306

# MAIL_USERNAME and MAIL_PASSWORD can not be empty as they are used in wildfly standalone-full.xml.
# If the smtp server does not require authentication, these single space values will be used
ENV MAIL_HOST=localhost MAIL_USERNAME=' ' MAIL_PASSWORD=' '

# Fedora Containers Packaging may need this
LABEL Name="zanata-platform"\
    Version=$ZANATA_VERSION\
    Release="2"

USER root

ENV ZANATA_HOME /var/lib/zanata
RUN yum update -y \
 && yum clean all && rm -rf /var/cache/yum \
 && mkdir -p $ZANATA_HOME && chown -R jboss.jboss $ZANATA_HOME
VOLUME $ZANATA_HOME

# Make the image Openshift compatible by granting root group (not user) access to the necessary directories
RUN chgrp -R 0 $ZANATA_HOME /opt/jboss/wildfly
RUN chmod -R g+rw $ZANATA_HOME /opt/jboss/wildfly
RUN find $ZANATA_HOME -type d -exec chmod g+x {} +
RUN find /opt/jboss/wildfly -type d -exec chmod g+x {} +

USER jboss

RUN curl -L -o /opt/jboss/wildfly/standalone/deployments/mysql-connector-java.jar https://repo1.maven.org/maven2/mysql/mysql-connector-java/${MYSQL_CONNECTOR_JAVA_VERSION}/mysql-connector-java-${MYSQL_CONNECTOR_JAVA_VERSION}.jar

COPY conf/standalone.xml /opt/jboss/wildfly/standalone/configuration/

# Zanata war
COPY zanata.war /opt/jboss/wildfly/standalone/deployments/${ZANATA_CONTEXT}.war
