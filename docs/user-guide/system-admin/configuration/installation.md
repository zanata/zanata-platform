Zanata can be installed by downloading a web archive (war) file, and configuring Jboss EAP or Wildfly according to this guide, or by downloading a handy installer.

## What you need

- JBoss Enterprise Application Platform 6 (EAP 6), version 6.4.6 or later. This is the recommended container for Zanata, and it can be [downloaded here](https://www.jboss.org/products/eap/download/).
- ... OR WildFly (version 10.0.0.Final) which can be [downloaded here](http://wildfly.org/downloads/)
- A suitable MySQL database. This is NOT included in the Zanata archive. You can [download MySQL here](http://dev.mysql.com/downloads/mysql/).
- An email (SMTP) server for email verification and notifications.
- JDK version 1.8 or later. [OpenJDK](http://openjdk.java.net/install/) is recommended, but you can also download [Oracle's JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

The following packages are optional, but recommended:

- clamav for virus protection.

## Setting up the Zanata Database

 1. Download and install MySQL 5.5 from the [MySQL download page](http://dev.mysql.com/downloads/mysql/).
 Zanata has been thoroughly tested against MySQL 5.5 and the Zanata team therefore recommends that you install and use this version with Zanata.

 1. Start MySQL service and create a database schema for Zanata.
    ```sql
    CREATE DATABASE zanata /**!40100 DEFAULT CHARACTER SET utf8 **/
    ```

 1. Create `zanata` user with the password `zanatapw` and grant `zanata` user full permissions
    ```sql
    CREATE USER 'zanata'@'localhost' IDENTIFIED BY 'zanatapw'
    GRANT ALL ON zanata.* TO 'zanata'@'localhost'
    ```

## Installing Zanata

You can run Zanata on JBoss EAP 6 or Wildfly. Just download one of the installer archives below for your platform, and then extract it on top of your JBoss or Wildfly installation.

- [Zanata for JBoss EAP or Wildfly](https://github.com/zanata/zanata-server/releases)

You'll find zip files for each platform, for example: `zanata-<version>-eap-6.zip` or `zanata-<version>-wildfly.zip`

You will also find `war` archives. Don't download these unless you wish to manually configure JBoss, or if you are upgrading an already set up Zanata server.

## Modify the configuration properties

Zanata comes bundled with a configuration file to make it easy to set initial properties. You should modify these properties in:

`<JBOSS>/standalone/configuration/zanata.properties`

## Editing standalone.xml yourself

This is not generally recommended, but if you want to create the configuration yourself, please ensure you start with `standalone-full.xml`, not `standalone.xml`, since `standalone-full.xml` enables subsystems which are required by Zanata. Either copy `standalone-full.xml` to `standalone.xml`, or make sure `standalone.sh` is always launched with arguments to activate the desired configuration, for instance `$JBOSS_HOME/bin/standalone.sh --server-config=standalone-full.xml`.


## Some advanced configuration

Zanata does not create an admin user by default. You need to register specific users to have administrative privileges.

 1. Open the `<JBOSS>/standalone/configuration/standalone.xml` file.

 1. Locate the following line, and replace `admin` with a comma-separated list of users that require administrator privileges on the system if necessary.

```xml
<system-properties>
  ...
  <property name="zanata.security.adminusers" value="admin"/>
  ...
</system-properties>
```

 1. Register a user under a name on this list, and it will automatically have administrator privileges. Any number of users may be added to this list in a comma-separated format.

 1. In the same file, configure other system properties to your particular setup by adding more lines if necessary. The following properties must be configured in order for Zanata to run properly:
```xml
<system-properties>
  ...
  <property name="zanata.email.defaultfromaddress" value="admin@example.com"/>
  ...
</system-properties>
```

 This is the default email address that will appear as the sender on Zanata emails.

 Alternatively, you can pass this and other system properties to JBoss when starting it (see JBoss documentation for details on how to do this).

## Enabling Cross-Origin Resource Sharing (advanced)

If you have an alternative front-end for Zanata where the browser needs direct access to Zanata REST
APIs (and authenticated user sessions), you will need to enable Cross-Origin Resource Sharing, by
adding a system property like this to `<JBOSS>/standalone/configuration/standalone.xml`:

    <property name="zanata.origin.whitelist" value="http://localhost:8000" />

You should adjust the protocol, hostname and port to suit your use case. Multiple origins should
be separated via space characters.

This setting will cause Zanata to output CORS headers for REST requests, including
`Access-Control-Allow-Origin` (if Origin is in the whitelist above),
`Access-Control-Allow-Credentials: true` and
`Access-Control-Allow-Methods: PUT,POST,DELETE,GET,OPTIONS`.
### Email configuration (Zanata 3.6 or earlier)

In Zanata 3.6 or earlier, email is configured by admin in the server settings screen.  By default, an SMTP server on localhost port 25 is expected.

 1. The following properties relate to the SMTP email server that Zanata uses to send emails. It defaults to a locally installed server using port 25. Add values to suit your configuration. If a particular property does not apply to the email server being used, you can comment it out or remove it completely.

```xml
<simple name="java:global/zanata/smtp/host" value="" />
<simple name="java:global/zanata/smtp/port" value="" />
<simple name="java:global/zanata/smtp/username" value="" />
<simple name="java:global/zanata/smtp/password" value="" />
<simple name="java:global/zanata/smtp/tls" value="" />
<simple name="java:global/zanata/smtp/ssl" value="" />
```

### Email configuration (Zanata 3.7 and later)

Email configuration is taken directly from JBoss/WildFly's `standalone.xml` configuration (*not* from the server settings screen), using the mail session configured with the JNDI name `java:jboss/mail/Default`.  In the default configuration of JBoss/WildFly, this expects an SMTP server on localhost port 25.

The JNDI strings starting with `java:global/zanata/smtp/` are now obsolete.  If you have previously configured these values, you will need to change the configuration of the mail session `java:jboss/mail/Default` to include your preferred SMTP settings.

JBoss's mail session configuration is described on these pages: https://developer.jboss.org/wiki/JBossAS720EmailSessionConfigurtion-EnglishVersion and http://www.mastertheboss.com/jboss-server/jboss-configuration/jboss-mail-service-configuration


## Installing virus scanner (optional)

To prevent virus infected document being uploaded, Zanata is capable of working with clamav.
If clamav is not installed, a warning will be logged when files are uploaded.
If clamav is installed but `clamd` is not running,
Zanata may reject all uploaded files (depending on file type).  To install and run clamav:
```
# Assuming the function install_missing() is still available
if [ -e /usr/bin/systemctl ];then
    install_missing clamav-server clamav-scanner-systemd
    sudo systemctl enable clamd@scan
    sudo systemctl start clamd@scan
else
    install_missing clamd
    sudo chkconfig clamd on
    if ! service clamd status ;then
	sudo service clamd start
    fi
fi
```

You should probably also ensure that freshclam is set to run at least once per day,
to keep virus definitions up to date.
The clamav package will probably do this for you, but you can check by looking for `/etc/cron.daily/freshclam`.
To override the default behaviour above, you can set the system property `virusScanner` when running the server.
`DISABLED` means no virus scanning will be performed; all files will be assumed safe.
Any other value will be treated as the name of a virus scanner command: the command will be called with the name of a file to scan.

## Running Zanata

Go to the `<JBOSS>/bin` directory and run

* `standalone-zanata.sh` for Linux, Mac
* `standalone-zanata.bat` for Windows

_Please make sure these are ran from the `<JBOSS>/bin` directory. These scripts are shortcuts which internally use JBoss' standalone running scripts._

## Using Zanata

To start using your Zanata server, open a browser and navigate to `http://localhost:8080/zanata`

You can now upload some source strings and start translating. To get started, see [Adding Source Documents](/user-guide/documents/upload-documents).
