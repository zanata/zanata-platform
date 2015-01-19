As of Zanata 3.6? (https://github.com/zanata/zanata-server/pull/633) email configuration is taken directly from JBoss/WildFly's standalone.xml configuration, using the mail session configured with the JNDI name "java:jboss/mail/Default".  In the default configuration of JBoss/WildFly, this expects an SMTP server on localhost port 25.

The JNDI strings starting with "java:global/zanata/smtp/" are now obsolete.  If you have previously configured these values, you will need to change the configuration of the mail session "java:jboss/mail/Default" to include your preferred SMTP settings.

JBoss's mail session configuration is described on these pages: https://developer.jboss.org/wiki/JBossAS720EmailSessionConfigurtion-EnglishVersion and http://www.mastertheboss.com/jboss-server/jboss-configuration/jboss-mail-service-configuration
