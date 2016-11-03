FROM jboss/wildfly:10.1.0.Final

ENV DB_HOSTNAME=zanatadb

# create mysql module
USER root
COPY conf/mysql-module/ /opt/jboss/wildfly/modules/
RUN yum -y install mysql-connector-java && yum clean all && \
    ln -sf /usr/share/java/mysql-connector-java.jar /opt/jboss/wildfly/modules/com/mysql/main/mysql-connector-java.jar

COPY conf/zanata-config.cli /opt/jboss/wildfly/standalone/configuration/

USER jboss

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/standalone/configuration/zanata-config.cli

# Enable debugging of the appserver: --debug
# Use standandlone-full: -c standalone-full.xml
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "--debug", "-b", "0.0.0.0", "-c", "standalone-full.xml"]
