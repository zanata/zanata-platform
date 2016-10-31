FROM jboss/wildfly:10.1.0.Final

ENV DB_HOSTNAME=zanatadb

# create mysql module
USER root
COPY conf/mysql-module/ /opt/jboss/wildfly/modules/
RUN yum -y install mysql-connector-java wget && yum clean all && \
    ln -sf /usr/share/java/mysql-connector-java.jar /opt/jboss/wildfly/modules/com/mysql/main/mysql-connector-java.jar

COPY conf/zanata-config.cli.script /opt/jboss/wildfly/standalone/configuration/

USER jboss

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/standalone/configuration/zanata-config.cli.script

# Enable debugging of the appserver
#RUN echo "JAVA_OPTS=\"\$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n\"" >> /opt/jboss/wildfly/bin/standalone.conf

# Use standandlone-full
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "--debug", "-b", "0.0.0.0", "-c", "standalone-full.xml"]
