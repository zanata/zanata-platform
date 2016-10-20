To download documents from your project-version, the command-line client's `pull` command can be used.

These instructions assume that you have installed Zanata-CLI as shown in [Installing the Client](/#installation), and have saved user and project configuration as shown in [Configuring the Client](/configuration).


## Translation Document Download

The basic command for downloading documents is `zanata-cli pull`. The pull command should always be run from the directory that contains `zanata.xml` for your project (find information about `zanata.xml` at [Configuring the Client](/configuration)).

At the time of writing, you will have to specify source and translation directories with the command, even though the default pull command will pull only translated documents from the server. This will be fixed in a future version. This means that the simplest pull command is:

```bash
zanata-cli pull -s src -t trans
```


This command will:

 1. look up the locales to pull from `zanata.xml` (all the enabled locales by default).
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
zanata-cli pull --pull-type source -s src -t source
```

To pull source and translation documents together, use `--pull-type both`.
