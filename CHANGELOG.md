# Zanata change log summary

## zanata-3.0.0
 * File upload
  * Move raw document storage to file system
  * Implement virus scanning using ClamAV (clamdscan)
  * Add descriptions on project type selectors
  * Allow adapter parameters to be set on source document upload
 * Editor improvements
  * Add attention key shortcut: Alt+X
  * Add attention shortcut to copy from source: Alt+X,G
 * TMX import/export
  * Allow users to export translations to TMX (from Project/Version pages)
  * Allow admins to export **all** project translations to TMX (from Projects page)
  * Allow admins to import and export TMX translation memories (from Admin pages)
  * Imported TMX shown in translation memory search results
 * Translation review/approval
  * Coordinators can assign reviewers for their languages
  * Project maintainers can require review for translations in their projects
  * Reviewers can approve or reject translations
  * Translators and reviewers can add comments to translations
 * New visual style for Zanata
 * Add Zanata dashboard
  * Recent translation/review activity
  * List of maintained projects
 * Upgrade platform to JBoss EAP 6.1

## zanata-2.3.1
 * Bug fixes:
  * Prevent incorrect validation warnings with concurrent edits
  * Search result back to editor causes multiple code mirror focus
  * Support message bookmark


## TODO fill in releases between 2.0.3 and 2.3.1

## zanata-2.0.3
 * Bug fixes: https://bugzilla.redhat.com/buglist.cgi?j_top=OR&f1=cf_fixed_in&o1=substring&classification=Community&o2=substring&query_format=advanced&f2=cf_fixed_in&bug_status=VERIFIED&bug_status=RELEASE_PENDING&bug_status=POST&bug_status=CLOSED&v1=2.0.3&product=Zanata
 * Allow admin to add extra locales by typing in the BCP-47 locale code.
 * TM Merge reports what it did
 * Allow choice of editor page size
 * Support txt, dtd and open document format (REST & web interface)
 * Editor option to disable CodeMirror (to enable browser spell-check)
 * Detect loss of connection to server
 * Fix for problem creating users with Kerberos
 * Allow Project Maintainers to Delete a Source Document

## zanata-2.0.2
 * Bug fixes for document search/navigation

## zanata-2.0.1
 * Update jboss-el to avoid bad artifact in repository

## zanata-2.0.0
 * UI redesign
 * Bug fixes: https://bugzilla.redhat.com/buglist.cgi?j_top=OR&f1=cf_fixed_in&o1=substring&classification=Community&o2=substring&query_format=advanced&f2=cf_fixed_in&bug_status=CLOSED&v1=1.8.0&v2=2.0.0&product=Zanata
 * Performance: async push service to avoid timeouts when pushing source/target
 * Performance: improve performance when loading large documents
 * Allow user to save work when concurrent edit occurs
 * Include last translator information in TM info box
 * Web analytics (Piwik integration)
 * Navigation breadcrumbs
 * Bugzilla link in UI
 * Get Stats about Translation Documents via REST
 * Remove blinking notification in editor
 * Configurable page size
 * Advanced glossary features
 * Open ID Authentication
 * Admin role assignment configuration for authentication types
 * Highlight tags in editor fields (CodeMirror for editor)
 * Translation editor rewrite
  * Project-level default Copy Trans options
  * Red bars for translations with validation warnings should stay in red when moving to the next row
 * Option to show word or message based statistics
 * Visible whitespace in editor
 * View history of translations for a text flow

## zanata-1.7.3
 * Bug fixes: https://bugzilla.redhat.com/buglist.cgi?f1=cf_fixed_in&o1=substring&classification=Community&query_format=advanced&bug_status=CLOSED&v1=1.7.3&product=Zanata

## zanata-1.7.2
 * Bug fixes: https://bugzilla.redhat.com/buglist.cgi?classification=Community&target_release=1.7.2&query_format=advanced&bug_status=CLOSED&product=Zanata

## zanata-1.7.1
 * Bug fixes: https://bugzilla.redhat.com/buglist.cgi?classification=Community&target_release=1.7.1&query_format=advanced&bug_status=CLOSED&product=Zanata

## zanata-1.7.0
 * UI Improvements
 * Bug fixes: https://bugzilla.redhat.com/buglist.cgi?classification=Community&target_release=1.6.2&target_release=1.7&query_format=advanced&bug_status=CLOSED&product=Zanata
 * Don't enforce locales for source documents
 * On-Demand copy trans
 * Email log appender
 * Centralise management of key shortcuts in Zanata
 * Improvements to reindexing (processing in small batches, index classes separately)
 * Editor validation for XML entities
 * Undo button for saved translations
 * Translation Memory merge in editor
 * Add support for positional strings in printf validator
 * Translation Memory now uses word-based indexing


## zanata-1.6.1
 * Bug fixes: https://bugzilla.redhat.com/buglist.cgi?classification=Community&target_release=1.6.1&query_format=advanced&bug_status=CLOSED&product=Zanata
 * Allow Zanata to add locales for which plural form is not known

## zanata-1.6.0
 * UI Improvements
 * Bug fixes: https://bugzilla.redhat.com/buglist.cgi?classification=Community&target_release=1.6&target_release=1.6-alpha-1&target_release=1.6-beta-1&query_format=advanced&bug_status=CLOSED&product=Zanata
 * Allow Project Maintainers to edit all language files
 * Glossary suggestions
 * Add the ability to specify custom locales that are not enabled by default
 * Upgrade Liquibase to version 2.0
 * Project grouping
 * Support plural forms
 * Offline translation feature via web UI
 * Allow translators to push translations using Maven client
 * Indicators for simultaneous edits
 * Java style variable validations in translation editor
 * "Create Project" for non-administrator users
 * Display page context in window title
 * Ability to monitor Zanata server statistics (JavaMelody)
 * Overview for available keyboard shortcuts in web editor

## zanata-1.5.0
 * Allow bookmarking of selected document, document list filter and current view: https://bugzilla.redhat.com/show_bug.cgi?id=757621
 * Add workspace query string parameters for generating a custom doclist with a custom title: https://bugzilla.redhat.com/show_bug.cgi?id=758587
  * e.g. &title=Custom%20title&doc=full/path/of/first/doc&doc=full/path/of/second/doc
 * Redesign of color scheme translation editor workspace layout
 * Project/project iteration status changes: ACTIVE, READONLY, and OBSOLETE
 * Allow readonly access to retired project/project iteration: https://bugzilla.redhat.com/show_bug.cgi?id=755759
 * Implement filter messages in the editor by translation status: https://bugzilla.redhat.com/show_bug.cgi?id=773459
 * Implement validation in editor:
  * Newline validation on leading and trailing string: https://bugzilla.redhat.com/show_bug.cgi?id=768802
  * Variables to be checked for consistency: https://bugzilla.redhat.com/show_bug.cgi?id=769471
  * XML and HTML tags to be checked for completeness: https://bugzilla.redhat.com/show_bug.cgi?id=756235
 * Project list filtering based on status
 * Overall statistics page for Admin
 * Add file download page with the option to download a single PO file, or a zip with a project iteration's files for one locale
 * Translation memory results now have highlighted differences
 * Activate entity caching
 * Maven client option to create 'skeleton' PO files when no translations are present
 * Maven client option to log detailed client-server message information
 * Generate Zanata Rest API documentation
 * Add automated compatibility tests with previous versions of the Zanata java client classes
 * Redirect to previous page after sign in
 * Several UI updates and changes
 * Bug fixes:
  * Rapid key navigation causes backlog of TM queries: https://bugzilla.redhat.com/show_bug.cgi?id=785034
  * Long strings slow down the operation: https://bugzilla.redhat.com/show_bug.cgi?id=750956
  * "Participants" information is incorrect.: https://bugzilla.redhat.com/show_bug.cgi?id=756292
  * Translation editor: Long word in source cell invades the editor cell: https://bugzilla.redhat.com/show_bug.cgi?id=759337
  * On push operations, copyTrans runs too slowly: https://bugzilla.redhat.com/show_bug.cgi?id=746899
  * Edit profile: "duplicate email" is shown even if user press save without changing email: https://bugzilla.redhat.com/show_bug.cgi?id=719176
  * Translation editor table shows changes which failed to save: https://bugzilla.redhat.com/show_bug.cgi?id=690669

## zanata-1.4.5.2
 * Fix handling of fuzzy entries when saving Properties files

## zanata-1.4.5.1
 * Fix regression with Unicode encoding for ordinary (Latin-1) .properties files: https://bugzilla.redhat.com/show_bug.cgi?id=795597

## zanata-1.4.5
 * Add support for Maven modules: https://bugzilla.redhat.com/show_bug.cgi?id=742872
 * Fix bug: Moving to a new page does not refresh the translation textboxes (ghost translations): https://bugzilla.redhat.com/show_bug.cgi?id=760431


## zanata-1.4.4
 * Ensure final reindex batch is properly flushed: https://bugzilla.redhat.com/show_bug.cgi?id=747836
 * Support UTF-8 Properties files, handle empty properties: https://bugzilla.redhat.com/show_bug.cgi?id=760390
 * Fix bug: Editor table stops working after 'Source and Target' search returns no results: https://bugzilla.redhat.com/show_bug.cgi?id=759994
 * Add dryRun option for Maven goals 'push' and 'pull'

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
