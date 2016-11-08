To delete glossary entry in Zanata, the command-line client's `glossary-delete` command can be used.

```bash
zanata-cli glossary-delete --id 1005
```

This command will:

1. Look up the server config from `zanata.xml`.
2. Delete glossary entry with id `1005`

To delete all system glossary entries

```bash
zanata-cli glossary-delete --all
```

To delete all project glossary entries
```
zanata-cli glossary-delete --all --project project1
```

To see all options available for `glossary-delete` option:
```bash
zanata-cli help glossary-delete
```
