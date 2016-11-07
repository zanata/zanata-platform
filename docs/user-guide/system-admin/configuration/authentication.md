# Integrating with external authentication via JAAS

# Introduction

## Configure Zanata security
Open `$JBOSS_HOME/standalone/configuration/standalone.xml` and look for the `system-properties` section. Add the following properties like this:

```xml
<system-properties>
  ...
  <property name="zanata.security.authpolicy.<authtype>" value="<policy-name>"/>
  <property name="zanata.security.adminusers" value="myusername"/>
  ...
</system-properties>
```

... where `<authtype>` is the authentication type enabled for the Zanata instance.
Accepted values are: internal, jaas, openid, kerberos

... and `<policy-name>` is the authentication policy name as defined in standalone.xml (see sections below).

In most cases, only a single authentication mechanism should be active at any given time, and Zanata will refuse to start if these settings are incorrect. However, the 'internal' and 'openid' mechanisms can be enabled simultaneously.

The `zanata.security.adminusers` property above must contain a comma-separated list of user names. Zanata will check that these users have administrator privileges, when they are created. This feature is recommended for the first time Zanata is started, and to avoid being locked out of the system at any time. However it is not meant to be used to manage administrator users system wide.

Use the 'register' page to add the admin users, with user names exactly matching the names listed above. The accounts will have to be activated using the activation links in the activation emails sent during the registration process before login is possible. If there is an issue with delivery of activation emails, accounts can be activated manually by running the following scripts directly on the Zanata database:

```sql
UPDATE HAccount SET enabled = true WHERE username = 'myusername';
```

1. After first login, you will be able to make yourself an admin in the database by running the following database scripts:

```sql
 insert into HAccountMembership(accountId, memberOf) values((select id from HAccount where username = 'myusername'), (select id from HAccountRole where name = 'admin'));
```

- Check current membership using:

```sql
SELECT r.name FROM HAccountRole AS r, HAccountMembership AS m WHERE m.accountId = (SELECT id FROM HAccount WHERE username = 'myusername') AND m.memberOf = r.id;
```

# Central Login Module (for all authentication mechanisms)

Zanata requires a central authentication module through which all authentication requests go. This central login module works in tandem with one or more additional authentication modules to provide the different supported authentication mechanisms. To activate this module, modify the `security-domains` section of `standalone.xml` and add the following snippet:

```xml
<security-domains>
...
    <security-domain name="zanata">
        <authentication>
            <login-module code="org.zanata.security.ZanataCentralLoginModule" flag="required"/>
        </authentication>
    </security-domain>
...
</security-domains>
```

After this, you will need to configure one (or more) of the following modules for the different types of authentication.

## Internal Authentication

Make sure `standalone.xml` has the following system property defined in the xml:

```xml
<property name="zanata.security.authpolicy.internal" value="zanata.internal"/>
```

Alternatively, you can pass this and other system properties to JBoss when starting it (see JBoss documentation for details on how to do this).

(Note the value of the property matches the security-domain name below).

standalone.xml

```xml
      ...
      <security-domain name="zanata.internal">
         <authentication>
            <login-module
               code="org.zanata.security.jaas.InternalLoginModule"
               flag="required">
            </login-module>
         </authentication>
      </security-domain>
      ...
```

## Pure JAAS

Make sure `standalone.xml` has the following system property

```xml
<property name="zanata.security.authpolicy.jaas" value="zanata.jaas"/>
```
(Note the value of the property matches the security-domain name below).

eg DatabaseServerLoginModule (you'll need to deploy a datasource too)

standalone.xml:

```xml
      ...
      <security-domain name="zanata.jaas">
          <authentication>
              <login-module code="org.jboss.security.auth.spi.DatabaseServerLoginModule" flag="required">
                  <module-option name="dsJndiName" value="java:authdb" />
                  <module-option name="principalsQuery" value="SELECT password FROM users WHERE username = ?" />
                  <module-option name="rolesQuery" value="select '','' FROM users WHERE username = ?" />
                  <module-option name="hashAlgorithm" value="md5" />
                  <module-option name="hashEncoding" value="hex" />
              </login-module>
          </authentication>
      </security-domain>
      ...
```

## Kerberos/SPNEGO

Kerberos authentication allows for both ticket based and form based authentication. Zanata will first check for a valid Kerberos ticket (if the browser supports it). If it is not possible to obtain a valid ticket, then Zanata will show a form to enter a user name and password to be authenticated using Kerberos.
**Note:** It is recommended to use SSL when dealing with form based Kerberos authentication.

Make sure `standalone.xml` has the following system property

```xml
<property name="zanata.security.authpolicy.kerberos" value="zanata.kerberos"/>
```
(Note the value of the property matches the security-domain name below).

**Configure Kerberos**

Kerberos configuration is located at /etc/krb5.conf
A kerberos keytab should be obtanined for HTTP and the server where the applications is being deployed.

Make sure you use OpenJDK, not Oracle/Sun JDK, unless you have the [appropriate JCE policy file ](http://www.oracle.com/technetwork/java/javase/downloads/jce-6-download-429243.html) (untested).


**Application Descriptors**

standalone.xml:

```xml
      ...
      <security-domain name="zanata.kerberos">
        <authentication>
          <login-module code="org.jboss.security.negotiation.spnego.SPNEGOLoginModule" flag="sufficient">
            <module-option name="password-stacking" value="useFirstPass" />
            <module-option name="serverSecurityDomain" value="host" />
            <module-option name="removeRealmFromPrincipal" value="true" />
            <module-option name="usernamePasswordDomain" value="krb5"/>
          </login-module>
        </authentication>
      </security-domain>

      <security-domain name="krb5">
        <authentication>
          <login-module code="com.sun.security.auth.module.Krb5LoginModule" flag="sufficient">
            <module-option name="storePass" value="false"/>
            <module-option name="clearPass" value="true"/>
            <module-option name="debug" value="true"/>
            <module-option name="doNotPrompt" value="false"/>
          </login-module>
        <authentication>
      </security-domain>

      <security-domain name="host">
        <authentication>
          <login-module code="com.sun.security.auth.module.Krb5LoginModule" flag="required">
            <module-option name="storeKey" value="true" />
            <module-option name="useKeyTab" value="true" />
            <module-option name="principal" value="HTTP/hostname@KERBEROS.DOMAIN" />
            <module-option name="keyTab" value="/etc/jboss.keytab" />
            <module-option name="doNotPrompt" value="true" />
          </login-module>
        </authentication>
      </security-domain>
      ...
```


The principal and keyTab attributes in the example above should be replaced with appropriate values for the principal as stored in the keytab and the location of the keytab file.

## OpenID

Important note: If you are running your server on Java 1.7, it will not be able to contact some OpenID providers (eg Fedora's) unless you upgrade the security provider.  See the section "Upgrading the Java security provider".

Java 1.8 on *some* Fedora-based systems has a similar problem contacting some OpenID providers (related to https://bugzilla.redhat.com/show_bug.cgi?id=1167153).  See the section "Disabling the SunEC provider".


Make sure `standalone.xml` has the following system property:

```xml
<property name="zanata.security.authpolicy.openid" value="zanata.openid"/>
```

(Note the value of the property matches the security-domain name below).

standalone.xml:

```xml
      ...
      <security-domain name="zanata.openid">
         <authentication>
           <login-module code="org.zanata.security.OpenIdLoginModule"
             flag="required">
           </login-module>
         </authentication>
       </security-domain>
      ...
```

### Single Provider

It's possible to configure Zanata to use a single pre-defined Open Id authentication provider. To do this, just add an extra `module-option` to the `login-module` element, like this:

```xml
      ...
      <security-domain name="zanata.openid">
         <authentication>
           <login-module code="org.zanata.security.OpenIdLoginModule"
             flag="required">
               <module-option name="providerURL" value="http://my.provider.org" />
           </login-module>
         </authentication>
       </security-domain>
      ...
```

### Enforce username for new user

System admin can enforce username to match with username returned from openId server for new user registration.
Open `$JBOSS_HOME/standalone/configuration/standalone.xml` and look for the `system-properties` section. Add the following property like this:

```xml
<system-properties>
  ...
  <property name="zanata.enforce.matchingusernames" value="true"/>
  ...
</system-properties>
```

### Attribute Exchange

By default Zanata will try to fetch the user's full name, email and username from the Open Id provider using [Attribute Exchange 1.0](https://openid.net/specs/openid-attribute-exchange-1_0.html) and [Simple Registration 1.1](https://openid.net/specs/openid-simple-registration-extension-1_0.html). If successful, Zanata will make suggestions when a new user signs up.

### Upgrading the Java security provider (for OpenID on Java 1.7)

Java 1.7 is unable to connect to some websites, such as the [Fedora OpenID provider](https://id.fedoraproject.org/).  See [Fedora bug 1163501](https://bugzilla.redhat.com/show_bug.cgi?id=1163501) for details.

**If you have the option, you will probably find it easier to use Java 1.8 for your server.**

This workaround should allow Zanata to contact such OpenID providers.  You will need to know your JAVA_HOME, which may be `/usr/lib/jvm/jre-1.7.0/` or similar.

1. Obtain Bouncy Castle and any dependencies: the package/jar you need may be named `bouncycastle`, `bcprov`, `bcprov-jdk15on` or similar.  For instance:

       yum install bouncycastle

2. Put a copy of (or link to) bcprov.jar into $JAVA_HOME/jre/lib/ext`.  For instance:

       ln -s /usr/share/java/bcprov.jar /usr/lib/jvm/jre-1.7.0/jre/lib/ext/

3. Add a reference to Bouncy Castle in the file `$JAVA_HOME/jre/lib/security/java.security`.  Assuming the last `security.provider` entry was `security.provider.9`, add this line:

       security.provider.10=org.bouncycastle.jce.provider.BouncyCastleProvider

### Disabling the SunEC provider (for Java 1.8 on some Fedora-based systems)

#### Option 1:

Delete sunec.jar from the Java installation, for instance `$JAVA_HOME/jre/lib/ext/sunec.jar`.

#### Option 2:

Add this line to $JBOSS_HOME/bin/standalone.conf:

    JAVA_OPTS="$JAVA_OPTS -Dcom.sun.net.ssl.enableECC=false"
