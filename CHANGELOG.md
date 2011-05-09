# Zanata change log summary

## 1.3-alpha-3
 * finalise rebrand from flies->zanata: XML namespaces, media types, etc
 * more logging for authentication errors
 * bug fix for Kerberos authentication

## 1.3-alpha-2
 * switch source control to git on github
 * rebrand from flies->zanata (maven artifacts, java packages, mailing lists)
 * Fedora authentication rhbz#692011
 * generate zanata.xml config file (http://code.google.com/p/flies/issues/detail?id=282)
 * merge translations on import (http://code.google.com/p/flies/issues/detail?id=28)
 * preserve and generate PO header comments for translator credits (http://code.google.com/p/flies/issues/detail?id=269)
 * bug fixes

## 1.3-alpha-1
 * rebrand from flies->zanata (except URIs, maven artifacts and java packages)
 * specify locales per project/version (http://code.google.com/p/flies/issues/detail?id=261)
 * added tab for home page, removed project list, contents editable by admin (http://code.google.com/p/flies/issues/detail?id=279)
 * added help page/tab, contents editable by admin (http://code.google.com/p/flies/issues/detail?id=280)
 * removed name and description from project version (http://code.google.com/p/flies/issues/detail?id=281)
 * stats for all languages (http://code.google.com/p/flies/issues/detail?id=275)
 * workaround for form/login issue on Firefox 4.0 rhbz#691963
 * bug fixes

## 1.2
 * disabled bad key bindings (http://code.google.com/p/flies/issues/detail?id=262)
 * fixed python client issue with PotEntryHeader.extractedComment (http://code.google.com/p/flies/issues/detail?id=256)
 * web template redesign (new logo, CSS) (http://code.google.com/p/flies/issues/detail?id=238)
 * fixed Seam integration tests (http://code.google.com/p/flies/issues/detail?id=231)

## 1.2-alpha-3
 * improve notifications in editor (http://code.google.com/p/flies/issues/detail?id=191)
 * highlight search terms in editor (http://code.google.com/p/flies/issues/detail?id=227)

## 1.2-alpha-2
 * better messages
 * bug fixes

## 1.2-alpha-1
 * development change: re-arranged Maven modules into common, client and server

## 1.1.1
 * use word counts in translation statistics (http://code.google.com/p/flies/issues/detail?id=203)
 * bug fixes

## 1.1
 * Kerberos/JAAS fixes
 * require name & email address on first login for JAAS/Kerberos
 * validate changes to email address
 * use correct BCP-47 language tags (zh-CN-Hans is now zh-Hans-CN)

## 1.1-alpha-1
 * JAAS authentication
 * Kerberos authentication
 * remove communities tab and my communities UI (http://code.google.com/p/flies/issues/detail?id=197)
 * remove "Language Missing" button (http://code.google.com/p/flies/issues/detail?id=185)
 * show member number for the language groups (http://code.google.com/p/flies/issues/detail?id=186)
 * allow overriding POT directory in Maven client (http://code.google.com/p/flies/issues/detail?id=200)
 * support `[servers]` in flies.ini for Maven client (http://code.google.com/p/flies/issues/detail?id=193)
 * better info/error messages in Maven client

## 1.0.3
 * fix TM caching issue (http://code.google.com/p/flies/issues/detail?id=190)
 * add 'translator' role and security rules
 * configurable URLs

## 1.0.2
 * minor UI fixes (http://code.google.com/p/flies/issues/detail?id=173, http://code.google.com/p/flies/issues/detail?id=176)
 * ergonomics for Maven client
 * UI for assigning project maintainers (http://code.google.com/p/flies/issues/detail?id=180)
 * better error checking in REST API (http://code.google.com/p/flies/issues/detail?id=175)
 * security rule fix (http://code.google.com/p/flies/issues/detail?id=182)

## 1.0.1
 * database schema fixes
 * fixes for deployment issues

## 1.0
 * initial release
