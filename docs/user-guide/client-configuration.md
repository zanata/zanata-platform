# Common configuration files for Flies clients

# Introduction

In general, Zanata clients should get their configuration from user config, project config and command line args (or similar).

Command line arguments should override values found in the user/project config files.  Project config should override values found in the user config (if they overlap).  The user config in the user's home directory are used for any values which haven't been supplied elsewhere.  In other words values are taken in order from:

1. Command line (or similar, eg maven pom)
1. Zanata project configuration (zanata.xml)
1. Zanata user configuration (zanata.ini)

Note that some versions of Maven have a [bug](http://jira.codehaus.org/browse/MNG-1992) where pom values can't be overridden by the command line.  If you want to specify a value on the command line, it may be best to leave it out of the pom.


## Zanata user configuration

File location: `$HOME/.config/zanata.ini` (under a user's home directory)

This file contains the user's general preferences and credentials for Zanata servers.

Java/Maven/Python Example:

    [defaults]
    debug = false
    #errors = true
    #NB: 'server' key will be removed in next release
    server = local
    
    ###
    # Server section
    # Format: ServerName.Attribute=Value
    # ServerName is a literature string for yourself to identify that Zanata Server
    # Attributes:
    #    url: URL of Zanata Server
    #    username: Your username on that Zanata Server
    #    key: Your API key   
    #
    [servers]
    
    # In the example below, "example", "jboss" and "localdevelinstance"
    # are arbitrary identifiers for servers. 
    # The url value is matched with a project's zanata.xml <url> element
    # to provide the username and API key for the project
    
    example.url = http://translate.example.com/zanata/
    example.username = bob
    example.key = 21654321651303467618640511051515
    
    jboss.url = https://translate.jboss.org/
    jboss.username = robert
    jboss.key =  13246817676681768761687196246543
    
    localdevelinstance.url = http://localhost:8080/zanata/
    localdevelinstance.username = admin
    localdevelinstance.key = 89374905823750973249502873490888

## Zanata project configuration

File location: `./zanata.xml` (in a project's base directory)

This file might be checked into a version control system along with the source files.  It tells clients which Zanata server the project is linked to, and which project/version within that server.

Example:

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <config xmlns="http://zanata.org/namespace/config/">
        <url>http://localhost:8080/zanata/</url> 
        <project>sample-project</project> 
        <project-version>1.1</project-version>
        <locales>
            <locale map-from="ja-JP">ja</locale>
            <locale map-from="zh-CN">zh-Hans</locale>
            <locale map-from="zh-TW">zh-Hant</locale>
        </locales>
    </config>

This file also specifies that the local publican PO directory "ja-JP" will be mapped to the server-side BCP47 locale "ja", and also specifies that the "zh-CN" directory will be mapped to "zh-Hanz" on the server.


URLs in project configuration should be matched against the [servers] defined in zanata.ini, so that the user's credentials can be used.

(In future, it may also be used to describe the type of project, or the mapping between source filenames and Zanata document names.)

## Command line or other mechanism (eg Maven properties)

The client should also provide the ability to override user/project configuration values, perhaps with command line arguments or GUI options, etc.

See ZanataMavenIntegration for the Maven configuration properties and example pom.xml.