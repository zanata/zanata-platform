FROM jboss/wildfly:10.1.0.Final

ENV DB_HOSTNAME=zanatadb DB_USERNAME=zanata DB_PASSWORD=zanatapw DB_NAME=zanata

# create mysql module
USER root
COPY conf/mysql-module/ /opt/jboss/wildfly/modules/
RUN yum -y install mysql-connector-java wget && yum clean all && \
    ln -sf /usr/share/java/mysql-connector-java.jar /opt/jboss/wildfly/modules/com/mysql/main/mysql-connector-java.jar

USER jboss

ADD conf/standalone.xml /opt/jboss/wildfly/standalone/configuration/

# Enable debugging of the appserver
RUN echo "JAVA_OPTS=\"\$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n\"" >> /opt/jboss/wildfly/bin/standalone.conf
