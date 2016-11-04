To download documents from your project-version, the command-line client's `pull` command can be used.

These instructions assume that you have installed Zanata-CLI as shown in [Installing the Client](/client#installation), and have saved user and project configuration as shown in [Configuring the Client](/client/configuration).


## Translation Document Download

The basic command for downloading documents is `zanata-cli pull`. The pull command should always be run from the directory that contains `zanata.xml` for your project (find information about `zanata.xml` at [Configuring the Client](/client/configuration)).

The simplest pull command is:

```bash
zanata-cli pull
```


This command will:

 1. look up the project version locales to pull from the server (unless specified in zanata.xml or from command line option).
 1. display the current settings and list of locales that will be downloaded.
 1. confirm that you want to proceed with the download.
 1. download translated versions of any documents that have any translations.

Documents with no translations will not be downloaded unless specifically requested by adding the `--create-skeletons` option.

To download only a few locales, use the `-l` or `--locales` option. For example, to download only Japanese and Russian translations, I might run `zanata-cli pull -s src -t trans -l ja,ru`. You can also modify the locales in `zanata.xml` if you will be consistently specifying a different set of locales.

For a full list of the available options for pull, run `zanata-cli help pull`


## Source Document Download

The pull command can also download source documents. This is generally only for reference purposes since source documents cannot be changed on the server.

To pull translations instead of source documents, add the option `--pull-type source`, like so:

```
zanata-cli pull --pull-type source --src-dir src
```

To pull source and translation documents together, use `--pull-type both`.
