This document will provide help when importing or migrating projects into Zanata from other translation platforms. 
You need a user account on the Zanata instance you will migrate to.

See [signup account](user-guide/account/sign-up) and [activate account](user-guide/account/account-activate) for more information.

## Create a project in Zanata

The first thing to do is to create a project and version to host your content in Zanata. You can do this using the command line clients (See [Initialising a Project](http://zanata-client.readthedocs.org/en/latest/commands/init/) ), or Zanata's web interface (See [Project Creation](user-guide/projects/create-project) ).

Please note that the `init` command might not be available on older platforms (like Fedora 19). Alternatively, you can use the Zanata Ivy client. For more information see [Installing the Client](http://zanata-client.readthedocs.org/en/latest/installation/) and [Configuring the Client](http://zanata-client.readthedocs.org/en/latest/configuration/).

## Configure your Project

If you used the client's `init` command, you should already have a `zanata.xml` in your local project directory. If not, then you can download one from the Zanata web interface. See [Configuring the Client](http://zanata-client.readthedocs.org/en/latest/configuration/) for more information.

## Extract your content

Extract the project's content from the other platform. Below are some examples on how to extract your content from several other translation platforms. This list will be updated with more examples in the future.

+ Transifex: `tx pull -s -a`. This command will pull all translation and source files from the server. More information available [Here](http://docs.transifex.com/developer/client/pull).

## Prepare your content

Depending on your project type, you might need to modify your Zanata configuration or your project's directory structure. Here are some examples on how to set up a project locally to be pushed to Zanata.

+ [Gettext project](user-guide/projects/gettext-example)

Gettext projects consist of a single source file (.pot), and several translation files (.po) named after their corresponding locale.

+ [Podir project](user-guide/projects/podir-example)

Podir projects consist of multiple source files (.pot) in a common source directory, and several translation files (.po) located in their corresponding locale's directory.

## Push your content to Zanata

Move your content over to the Zanata project's directory created previously, and run the following client command from the same directory:

```bash
zanata-cli push -s . -t . --push-type both
```

This will push your content into the Zanata project that you previously created. For more information on pushing your content using the zanata client see [Document Upload with Client](http://zanata-client.readthedocs.org/en/latest/commands/push/).
