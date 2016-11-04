To initialise a project from the command line, the `init` command can be used. This command will guide you through the steps necessary to set up a new or existing project, and start using Zanata.

These instructions assume that you have installed Zanata-CLI version 3.4.0 or higher as shown in [Installing the Client](/#installation).



## Getting started

The first thing to do is to type the following command in the console: `zanata-cli init`

The client will proceed to ask which of your preferred Zanata servers it needs to use to register your new project (this information is taken from your `zanata.ini` file; if the desired server does not appear in the list, it should be added to this file).

```bash
[INFO] Loading project config from zanata.xml
[INFO] Loading user config from /home/camunoz/.config/zanata.ini
    Found servers in zanata.ini:
    1)    http://localhost:8080/zanata/
    2)    https://translate.jboss.org/
    3)    https://translate.zanata.org/zanata/
[?] Which Zanata server do you want to use?
```

Select the Zanata server to use by typing in the number and hiting ENTER. The client will now ask whether you want to create a new project, or use an existing one from your instance:

```bash
[?] Do you want to 1) select an existing project or 2) create a new one (1/2)?
```

According to your selection the client will ask for information about your new project and proceed to create it, or it will give you an option to select an existing one from the server.

```bash
[?] What project ID/slug do you want to have (must start and end with a letter or number, and contain only letters, numbers, underscores and hyphens):
$ my-project-id
[?] What project name do you want to have:
$ My Project Name
[?] What is your project type ([utf8properties, properties, gettext, podir, xliff, xml, file])?
$ podir
[>] Project created. Now it's time to create a version to host your files.

[?] What version ID/slug do you want to have:
$ master
[>] Version created.
```

the Zanata client will continue to ask questions and provide information on the next steps necessary to get you started with your project on Zanata.