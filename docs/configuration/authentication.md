# Integrating with external authentication via JAAS

# Introduction

## Configure Zanata security 
Open `$JBOSS_HOME/standalone/configuration/standalone.xml` and look for the `naming` subsystem. Add the following sections to the `bindings` section:

```xml
<subsystem xmlns="urn:jboss:domain:naming:1.3">
    ...
    <bindings>
        <simple name="java:global/zanata/security/auth-policy-names/<authtype>" value="<policy-name>"/>
        <simple name="java:global/zanata/security/admin-users" value="<list of usernames>"/>
    </bindings>
    ...
</subsystem>
```

... where `<authtype>` is the authentication type enabled for the Zanata instance.
Accepted values are: internal, jaas, openid, kerberos

... and `<policy-name>` is the authentication policy name as defined in standalone.xml (see sections below).

In most cases, only a single authentication mechanism should be active at any given time, and Zanata will refuse to start if these settings are incorrect. However, the 'internal' and 'openid' mechanisms can be enabled simultaneously.

The `java:global/zanata/security/admin-users` property above must contain a comma-separated list of user names. Zanata will check that these users have administrator privileges, when they are created. This feature is recommended for the first time Zanata is started, and to avoid being locked out of the system at any time. However it is not meant to be used to manage administrator users system wide.

Use the 'register' page to add the admin users, with user names exactly matching the names in the zanata.properties file. The accounts will have to be activated using the activation links in the activation emails sent during the registration process before login is possible. If there is an issue with delivery of activation emails, accounts can be activated manually using:

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

Make sure `standalone.xml` has the following jndi property 

```xml
<simple name="java:global/zanata/security/auth-policy-names/internal" value="zanata.internal"/>
```

(Note the value of the property matches the security-domain name below).

standalone.xml

```xml
      ...
      <security-domain name="zanata.internal">
         <authentication>
            <login-module
               code="org.jboss.seam.security.jaas.SeamLoginModule"
               flag="required">
            </login-module>
         </authentication>
      </security-domain>
      ...
```

## Pure JAAS

Make sure `standalone.xml` has the following jndi property 

```xml
<simple name="java:global/zanata/security/auth-policy-names/jaas" value="zanata.jaas"/>
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

Make sure `standalone.xml` has the following jndi property 

```xml
<simple name="java:global/zanata/security/auth-policy-names/kerberos" value="zanata.kerberos"/>
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

Make sure `standalone.xml` has the following jndi property 

```xml
<simple name="java:global/zanata/security/auth-policy-names/openid" value="zanata.openid"/>
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

_( As of version 3.5.1 )_
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