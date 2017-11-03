# Translation Messages

This directory contains translation messages packaged for translation.
Upload en-us.json as source, and download translations as [locale-id].json

## Lifecycle

How strings are defined, extracted, translated and consumed in this app.

1. Messages are defined in the app code. They are defined using components or
   methods from the react-intl package (e.g. FormattedMessage)
2. Messages are output to multiple JSON files during build, in build/messages.
   This is done by babel-plugin-react-intl, configured in .babelrc.
3. Messages from build/messages are compiled into en-us.json in this directory.
   This is done by react-intl-aggregate-webpack-plugin configured in
   webpack.config.js
4. en-us.json can be uploaded for translation. The translated version should be
   saved in this directory as [locale-id].json
5. Translations are read from this directory and transformed ready for the app
   to consume, output in in dist/messages/ with the same filename.

dist/messages can be included in the zanata-frontend .jar file, and locale data
can be loaded from that directory into the app at runtime based on the user's
UI locale setting.
