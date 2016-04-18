## 3.8.4

##### Bug fixes
 * [ZNTA-1011](https://zanata.atlassian.net/browse/ZNTA-1011) - Zanata client 3.8.3 can't push file to server running on WildFly
 * [ZNTA-125](https://zanata.atlassian.net/browse/ZNTA-125) - zanata-cli on Fedora for xliff project has NullPointerException during push when --validate is not specified

-----------------------

## 3.8.2

##### Bug fixes
 * [ZNTA-519](https://zanata.atlassian.net/browse/ZNTA-519) - Unable to push the POT file beginning from the underscore (_) in the filename
 * [ZNTA-843](https://zanata.atlassian.net/browse/ZNTA-843) - Improve/fix 0install bug
 * [ZNTA-901](https://zanata.atlassian.net/browse/ZNTA-901) - txw2 should be included in zanata-cli 0install
 * [ZNTA-930](https://zanata.atlassian.net/browse/ZNTA-930) - Zanata init command fails when specifying &quot;file&quot; project type
 * [ZNTA-937](https://zanata.atlassian.net/browse/ZNTA-937) - zanata-client: need better error message for unexpected html response

-----------------------

## 3.8.1

##### Changes
* Add a new parameter includeAutomatedEntry to API getContributionStatics 
* Downgrade dependency to keep enforcer happy
* 0install feed for Zanata CLI has been migrated to https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml. Please see [installation](/#installation) for updated command.

## 3.8.0

##### Highlight
* Copy Trans will nolonger run by default. Option `--copy-trans` is now required to invoke copy trans when pushing

##### Changes
* [ZNTA-354](https://zanata.atlassian.net/browse/ZNTA-354) - Improve Zanata client installation documentation and workflow
* [ZNTA-664](https://zanata.atlassian.net/browse/ZNTA-664) - Change client push command not to run copy trans by default

-----------------------

##### Bug fixes
*

-----------------------

##### New Features
*

-----------------------

