To search for glossary entries in Zanata, the command-line client's `glossary-search` command can be used.

```bash
zanata-cli glossary-search --filter test
```

This command will:

1. Look up the server config from `zanata.xml`.
2. Search for and display glossary entries with content containing `test`

To search for glossary entries in a project glossary:
```bash
zanata-cli glossary-search --filter test --project myproject
```

To see all options available for the `glossary-search` command:
```bash
zanata-cli help glossary-search
```
