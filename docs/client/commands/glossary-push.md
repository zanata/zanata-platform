To push glossary entries to Zanata, the command-line client's `glossary-push` command can be used.

The source language of the glossary file should be in `en-US`

### Push to System glossary

```bash
zanata-cli glossary-push --file glossary.csv
```

This command will:

1. Look up the server config from `zanata.xml`.
2. Push glossary.csv file to Zanata.

### Push to Project glossary

```bash
zanata-cli glossary-push --file glossary.csv --project project1
```

This command will:

1. Look up the server config from `zanata.xml`.
2. Push glossary.csv file to `project1` in Zanata.


### Push po file

`--trans-lang` option will be needed.
For example:

```bash
zanata-cli glossary-push --file german-glossary.po --trans-lang de
```

To see all options available for `glossary-push` option:
```bash
zanata-cli help glossary-push
```