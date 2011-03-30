Guide to Translation Build Scripts
==================================


Translation Workflow
--------------------

1. Author modifies documentation, checks in DocBook XML source.
2. At some point in the lifecycle, a documentation freeze is announced.
3. Import job is run (eg from Hudson/Jenkins).  (See script 2 below: `zanata_import_source`.)
4. Translators can begin translating at <https://translate.jboss.org/>.
5. Draft builds are run nightly or more often (Jenkins?).  (See script 3 below: `zanata_draft_build`)
6. If author changes any XML, go back to step 3.
7. Translations declared "final"
8. Documentation release build is run.  (See script 4 below: `zanata_export_translations`)



Configuration for build machine
-------------------------------
Create the file `~/.config/zanata.ini` like this:

<pre>
[servers]
jboss.url = https://translate.jboss.org/
jboss.username = your_jboss_username
jboss.key = your_API_key_from_Flies_Profile_page
</pre>

NB: Your key can be obtained by logging in to [Zanata](https://translate.jboss.org/), 
visiting the [Profile page](https://translate.jboss.org/profile/view) and 
clicking "generate API key" at the bottom.

The Scripts
-----------

1. Initial Import (run once only, when first integrating with Zanata):  
 This script will update the POT (source) and PO (translation) files 
 under `.` from the DocBook XML, and push the content 
 to Zanata for translation.  
 `etc/scripts/zanata_import_all`  
 `svn add .; svn ci`  

2. Import job (run after documentation freeze):  
 This script will update the POT files in `pot` and
 push them to Zanata for translation.  
 `etc/scripts/zanata_import_source`

3. Build docs with latest translations (probably run nightly):  
 This script will fetch the latest translations to temporary files 
 in `target/draft` and build the documentation for review purposes.  
 `etc/scripts/zanata_draft_build`

4. Documentation release build:  
 This script will fetch the latest translations so that the release 
 build can be run.  
 `etc/scripts/zanata_export_translations`  
 `svn add .; svn ci`  
 _run existing ant build_


