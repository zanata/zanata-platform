If your project uses Apache Maven, you can use Zanata's Maven Plugin rather than the command line client. The Maven Plugin can be used for non-Maven projects, but the same functionality is available in zanata-cli without the need to install or configure Maven.

The following instructions assume that you have installed and configured the Zanata Maven Plugin. Instructions for installation and configuration are available at [Installing the Maven Plugin](maven-plugin/installation) and [Configuring the Maven Plugin](maven-plugin/configuration)

## Basic Maven Plugin commands

Commands and options for the Maven Plugin are similar to commands and options for zanata-cli, but with different syntax.

### Help

To see an overview of commands, use

```bash
mvn zanata:help
```

and for detailed help for a particular command, use something like

```
mvn zanata:help -Ddetail=true -Dgoal=push
```

These are equivalent to commands `zanata-cli help` and `zanata-cli help push` in zanata-cli.

*Note:* an online view of the same help information can be viewed at [Maven Plugin Reference](https://zanata.ci.cloudbees.com/job/zanata-client-site/site/zanata-maven-plugin/plugin-info.html).

### Push

The basic push command is

```bash
mvn zanata:push
```

This will look for source documents in the location for `srcDir` specified in `pom.xml` and upload them to the server. If `srcDir` is not specified in `pom.xml`, the current directory will be used.

The source directory can be overridden on the command line as shown here:

```bash
mvn zanata:push -Dzanata.srcDir="src/messages"
```

This will look for source strings in "src/messages".

More detail on the push command can be found at [Document Upload with Client](commands/push)

### Pull

The basic pull command is

```bash
mvn zanata:pull
```

This will download translated documents from the server and save them in the location for `transDir` specified in `pom.xml`. If `transDir` is not specified in `pom.xml`, the current directory will be used.

To download the source documents as well, specify pull type 'both' as shown here:

```bash
mvn zanata:pull -Dzanata.pullType="both"
```

More detail on the pull command can be found at [Document Download with Client](commands/pull)
