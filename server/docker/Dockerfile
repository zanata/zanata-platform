FROM jboss/wildfly:10.1.0.Final

ENV DB_HOSTNAME=zanatadb

# create mysql module
USER root
COPY conf/mysql-module/ /opt/jboss/wildfly/modules/
RUN yum -y install mysql-connector-java && yum clean all && \
    ln -sf /usr/share/java/mysql-connector-java.jar /opt/jboss/wildfly/modules/com/mysql/main/mysql-connector-java.jar

COPY conf/zanata-config.cli /tmp/

USER jboss

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/zanata-config.cli

# override the sun jdk module file to support java melody (com.sun.management.* for heap dump)
# TODO remove this when https://github.com/javamelody/javamelody/issues/585 is resolved
COPY conf/sun/jdk/module.xml /opt/jboss/wildfly/modules/system/layers/base/sun/jdk/main/module.xml

# Enable debugging of the appserver: --debug
# Use standandlone-full: -c standalone-full.xml
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "--debug", "-b", "0.0.0.0", "-c", "standalone-full.xml"]
