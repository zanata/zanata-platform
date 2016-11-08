# Zanata

Zanata is a web-based system for translators to translate
documentation and software online using a web-browser. It is
written in Java and uses modern web technologies like JBoss EAP,
CDI, GWT, Hibernate, and a REST API. It currently supports
translation of DocBook/Publican documentation through PO
files, and a number of other formats. Projects can be uploaded
to and downloaded from a Zanata server using a Maven plugin or
a command line client.

For *developers and writers*: By using Zanata for
your document translations, you can open up your project for
translations without opening your entire project in version
control.

For *translators*: No need to deal with PO files,
gettext or a version control system - just log in to the website, join
a language team and start translating, with translation memory (history
of similar translations) and the ability to see updates from other
translators in seconds.

Find out about Zanata here: http://zanata.org/


Zanata is Free software, licensed under the [LGPL][].

[LGPL]: http://www.gnu.org/licenses/lgpl-2.1.html

## Source code note
Please note that any references to pull request numbers in commit
messages (eg merge nodes) prior to 20 October 2016 are referring to the
old repositories (before they were merged into the zanata-platform
repository):

* https://github.com/zanata/zanata-api/pulls/
* https://github.com/zanata/zanata-client/pulls/
* https://github.com/zanata/zanata-common/pulls/
* https://github.com/zanata/zanata-parent/pulls/
* https://github.com/zanata/zanata-server/pulls/

GitHub tries to auto-link numbers to pull requests, but such links will
generally be incorrect for old commit messages.
