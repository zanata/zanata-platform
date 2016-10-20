Custom commands can be specified in zanata.xml. Custom commands can be almost any command that can run on the command line, and can be configured to run before or after a Zanata client command. This feature was added in version 3.3.0 of the CLI client and the Maven Plugin, and cannot be used in earlier versions.

To specify one or more custom commands:

 1. Add a `<hooks>` element in zanata.xml within the `<config>` element.
 1. For each Zanata command that will have custom commands attached, add a `<hook>` element that specifies the command as an attribute.
 1. For each custom command to run before a Zanata command, add a `<before>` element with the command as its body.
 1. For each custom command to run after a Zanata command, add an `<after>` element with the command as its body.

For example, the following elements within the top-level <config> element will generate a .pot file from a man page before push, clean up the generated file after push, and will generate a translated German man page after pull then remove all downloaded .po files.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<config xmlns="http://zanata.org/namespace/config/">

...

  <hooks>
    <hook command="push">
        <before>po4a-gettextize -f man -m manpage.1 -p manpage.pot</before>
        <after>rm -f manpage.pot</after>
    </hook>
    <hook command="pull">
        <after>po4a-translate -f man -m manpage.1 -p trans/de/manpage.po -l manpage.de.1 --keep 1</after>
        <after>rm -rf trans</after>
    </hook>
  </hooks>

...

</config>
```

Multiple commands of the same type (i.e. "before" or "after") within a hook will be run in the order that they are specified in zanata.xml. e.g. when running pull with the above config, po4a will always run before rm. If any of these commands fails, the whole operation is aborted. e.g. when running push, if po4a fails then push and rm will not be run, and if push fails then rm will not be run.

Note that some commands (such as 'cd') do not work as custom commands. The ranges of commands that work and that do not work as custom commands have not yet been thoroughly investigated.

The return value of each custom command is displayed after the command is run. A return value of 0 usually indicates success, and any other number usually indicates an error. Console output from custom commands is not yet displayed or logged.