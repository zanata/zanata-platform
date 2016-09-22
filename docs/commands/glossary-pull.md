To download glossary entries from Zanata, the command-line client's `glossary-pull` command can be used.

### Pull from System glossary

```bash
zanata-cli glossary-pull
```

This command will:

1. Look up the server config from `zanata.xml`.
2. Download all glossary entries in server
3. The file will be in `.csv` format. (default)

### Pull from Project glossary

```bash
zanata-cli glossary-pull --project project1
```

This command will:

1. look up the server config from `zanata.xml`.
2. Download all glossary entries from `project1` in server
3. The file will be in `.csv` format. (default)

### Pull in different format

To download in different format, use the `--file-type` options (csv or po). 
For example:

```bash
zanata-cli glossary-pull --file-type po
```

To download only specific locales for `--file-type po`, use the ` --trans-lang` options. 
For example to download `de` and `fr` locales only:

```bash
zanata-cli glossary-pull --file-type po --trans-lang de,fr
```

To see all options available for `glossary-pull` option:
```bash
zanata-cli help glossary-pull
```
