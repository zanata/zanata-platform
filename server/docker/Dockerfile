FROM jboss/wildfly:10.1.0.Final

USER root
RUN yum -y install mysql-connector-java wget && yum clean all

RUN ln -sf /usr/share/java/mysql-connector-java.jar /opt/jboss/wildfly/standalone/deployments/mysql-connector-java.jar

# ENV ZANATA_VERSION 3.9.0

# Zanata war (this is not needed for dev)
# RUN wget --no-verbose -O /opt/jboss/wildfly/standalone/deployments/ROOT.war \
#     https://github.com/zanata/zanata-server/releases/download/server-${ZANATA_VERSION}/zanata-war-${ZANATA_VERSION}.war \
#     && chmod o+rw /opt/jboss/wildfly/standalone/deployments/ROOT.war

USER jboss

ADD conf/standalone.xml /opt/jboss/wildfly/standalone/configuration/

# Enable debugging of the appserver
RUN echo "JAVA_OPTS=\"\$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n\"" >> /opt/jboss/wildfly/bin/standalone.conf
