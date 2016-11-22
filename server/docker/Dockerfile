FROM jboss/wildfly:10.1.0.Final

# MAIL_USERNAME and MAIL_PASSWORD can not be empty as they are used in wildfly standalone-full.xml.
# If the smtp server does not require authentication, these single space values will be used
ENV DB_HOSTNAME=zanatadb MAIL_HOST=localhost MAIL_USERNAME=' ' MAIL_PASSWORD=' '

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
RUN sed -i '/<\/paths>/ i <path name="com/sun/management"/>' /opt/jboss/wildfly/modules/system/layers/base/sun/jdk/main/module.xml

# Enable debugging of the appserver: --debug
# Use standandlone-full: -c standalone-full.xml
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "--debug", "-b", "0.0.0.0", "-c", "standalone-full.xml"]
