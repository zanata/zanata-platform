# Zanata change log summary

## zanata-1.5
 * Allow bookmarking of selected document, document list filter and current view: https://bugzilla.redhat.com/show_bug.cgi?id=757621
 * Add workspace query string parameters for generating a custom doclist with a custom title: https://bugzilla.redhat.com/show_bug.cgi?id=758587
  * e.g. &title=Custom%20title&doc=full/path/of/first/doc&doc=full/path/of/second/doc

## zanata-1.4.4
 * Ensure final reindex batch is properly flushed: https://bugzilla.redhat.com/show_bug.cgi?id=747836
 * Support UTF-8 Properties files, handle empty properties: https://bugzilla.redhat.com/show_bug.cgi?id=760390
 * Fix bug: Editor table stops working after 'Source and Target' search returns no results: https://bugzilla.redhat.com/show_bug.cgi?id=759994

## zanata-1.4.3
 * Show message context in editor info panel: https://bugzilla.redhat.com/show_bug.cgi?id=750690
 * Update gwteventservice to 1.2.0-RC1
 * Modify email templates to include server URL
 * Fix problems with editor table when searching or switching pages: https://bugzilla.redhat.com/show_bug.cgi?id=751264
 * Add failsafe editor in case of Seam Text problems: https://bugzilla.redhat.com/show_bug.cgi?id=727716
 * Change string similarity algorithm so that only identical strings (not substrings) can get 100%: https://bugzilla.redhat.com/show_bug.cgi?id=730189
 * Bugfix: 'J' and 'K' navigation keys trigger when entering text in the TM search box: https://bugzilla.redhat.com/show_bug.cgi?id=754637
 * Bugfix: Not able to work in parallel on the same workbench: https://bugzilla.redhat.com/show_bug.cgi?id=756293
 * Show progress during re-index operations; avoid timeout for large databases: https://bugzilla.redhat.com/show_bug.cgi?id=747836
 
## zanata-1.4.2
 * Language team coordinator: https://bugzilla.redhat.com/show_bug.cgi?id=742083
  * Users now have to ask before joining a language team
  * Coordinator can add and remove team members
  * Contact coordinators
 * Contact server admins: https://bugzilla.redhat.com/show_bug.cgi?id=742854
 * First/last entry button: https://bugzilla.redhat.com/show_bug.cgi?id=743783
 * Load project pages faster: https://bugzilla.redhat.com/show_bug.cgi?id=744114
 * Option for Enter to save translation: https://bugzilla.redhat.com/show_bug.cgi?id=744671
 * Sort projects by name, not ID: https://bugzilla.redhat.com/show_bug.cgi?id=746859
 * Make newlines visible to reduce newline mismatch errors in translations: https://bugzilla.redhat.com/show_bug.cgi?id=740122
 * Improve shortcut keys: https://bugzilla.redhat.com/show_bug.cgi?id=740191
 * Fix tab order: editor cell -> Save as Approved -> Save as Fuzzy -> Cancel
 * Save as Fuzzy now leaves the cell editor open: https://bugzilla.redhat.com/show_bug.cgi?id=746870
 * Modal navigation: next fuzzy, untranslated, fuzzy or untranslated: https://bugzilla.redhat.com/show_bug.cgi?id=743134
 * Rearrange various UI elements to be more logical (profile page, document stats, project search field)

## zanata-1.4.1
 * Fixed: % completed should be calculated with words, not messages: https://bugzilla.redhat.com/show_bug.cgi?id=741523
 * Fixed: Selecting Administration submenu items does not always highlight the parent menu: https://bugzilla.redhat.com/show_bug.cgi?id=724867
 * Fixed: Change of tile to list view on Language page, make project list sortable: https://bugzilla.redhat.com/show_bug.cgi?id=742111
 * Performance fix for projects with 1000+ documents: https://bugzilla.redhat.com/show_bug.cgi?id=743179

## zanata-1.4
 * add project-type to zanata.xml for generic push/pull commands
 * redirect to login from translation editor when required
 * if domain is left blank by admin, don't populate email address for new users
 * UI bug fixes

## zanata-1.4-alpha-1
 * create generic push/pull commands, with include/exclude filters
 * add support for Java Properties and XLIFF projects
  * bug fix: mark existing translations of modified XLIFF/Properties strings as fuzzy
 * modify keyboard shortcuts in editor
 * add new Zanata logo/favicon
 * various UI improvements
  * auto-size for translation text area
  * add icons to buttons and remove text
  * add option to hide editor buttons
  * remove Clone and Save button; move Copy button to middle
  * autosave when leaving a cell
  * remove Fuzzy checkbox; add Save as Fuzzy
  * better statistics graphs
  * display resource IDs for translation units
  * add ability to hide translation unit details
  * show translation states with coloured side bars, and italics for Fuzzy
  * recalculate missing word counts
 * bug fixes

## zanata-1.3.1 (never released)
 * add liquibase script
 * bug fix for search re-indexing by admin
 * copy translations of identical strings when importing new documents
 * bug fixes and improvements for UI
 * bug fix for word counts (thread safety)
 * remove email address from Language Team pages
 * enable stats for anonymous users
 * no need to enforce locales for source documents
 * bug fix for push/merge when PO files are missing some msgids

## zanata-1.3
 * bug fixes for authentication and for source comments

## zanata-1.3-alpha-3
 * finalise rebrand from flies->zanata: XML namespaces, media types, etc
 * more logging for authentication errors
 * bug fix for Kerberos authentication

## zanata-1.3-alpha-2
 * switch source control to git on github
 * rebrand from flies->zanata (maven artifacts, java packages, mailing lists)
 * Fedora authentication rhbz#692011
 * generate zanata.xml config file (http://code.google.com/p/flies/issues/detail?id=282)
 * merge translations on import (http://code.google.com/p/flies/issues/detail?id=28)
 * preserve and generate PO header comments for translator credits (http://code.google.com/p/flies/issues/detail?id=269)
 * bug fixes

## zanata-1.3-alpha-1
 * rebrand from flies->zanata (except URIs, maven artifacts and java packages)
 * specify locales per project/version (http://code.google.com/p/flies/issues/detail?id=261)
 * added tab for home page, removed project list, contents editable by admin (http://code.google.com/p/flies/issues/detail?id=279)
 * added help page/tab, contents editable by admin (http://code.google.com/p/flies/issues/detail?id=280)
 * removed name and description from project version (http://code.google.com/p/flies/issues/detail?id=281)
 * stats for all languages (http://code.google.com/p/flies/issues/detail?id=275)
 * workaround for form/login issue on Firefox 4.0 rhbz#691963
 * bug fixes

## flies-1.2
 * disabled bad key bindings (http://code.google.com/p/flies/issues/detail?id=262)
 * fixed python client issue with PotEntryHeader.extractedComment (http://code.google.com/p/flies/issues/detail?id=256)
 * web template redesign (new logo, CSS) (http://code.google.com/p/flies/issues/detail?id=238)
 * fixed Seam integration tests (http://code.google.com/p/flies/issues/detail?id=231)

## flies-1.2-alpha-3
 * improve notifications in editor (http://code.google.com/p/flies/issues/detail?id=191)
 * highlight search terms in editor (http://code.google.com/p/flies/issues/detail?id=227)

## flies-1.2-alpha-2
 * better messages
 * bug fixes

## flies-1.2-alpha-1
 * development change: re-arranged Maven modules into common, client and server

## flies-1.1.1
 * use word counts in translation statistics (http://code.google.com/p/flies/issues/detail?id=203)
 * bug fixes

## flies-1.1
 * Kerberos/JAAS fixes
 * require name & email address on first login for JAAS/Kerberos
 * validate changes to email address
 * use correct BCP-47 language tags (zh-CN-Hans is now zh-Hans-CN)

## flies-1.1-alpha-1
 * JAAS authentication
 * Kerberos authentication
 * remove communities tab and my communities UI (http://code.google.com/p/flies/issues/detail?id=197)
 * remove "Language Missing" button (http://code.google.com/p/flies/issues/detail?id=185)
 * show member number for the language groups (http://code.google.com/p/flies/issues/detail?id=186)
 * allow overriding POT directory in Maven client (http://code.google.com/p/flies/issues/detail?id=200)
 * support `[servers]` in flies.ini for Maven client (http://code.google.com/p/flies/issues/detail?id=193)
 * better info/error messages in Maven client

## flies-1.0.3
 * fix TM caching issue (http://code.google.com/p/flies/issues/detail?id=190)
 * add 'translator' role and security rules
 * configurable URLs

## flies-1.0.2
 * minor UI fixes (http://code.google.com/p/flies/issues/detail?id=173, http://code.google.com/p/flies/issues/detail?id=176)
 * ergonomics for Maven client
 * UI for assigning project maintainers (http://code.google.com/p/flies/issues/detail?id=180)
 * better error checking in REST API (http://code.google.com/p/flies/issues/detail?id=175)
 * security rule fix (http://code.google.com/p/flies/issues/detail?id=182)

## flies-1.0.1
 * database schema fixes
 * fixes for deployment issues

## flies-1.0
 * initial release
