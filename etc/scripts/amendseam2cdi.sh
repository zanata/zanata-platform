#!/bin/bash -e
shopt -s globstar

commitMsg="Migrate from Seam to CDI annotations (seam2cdi.sh)"

if (git log -1 --pretty=%B|grep -q "$commitMsg"); then
    # remove commit from previous run of the script:
    git reset --hard HEAD^

    # run the refactoring script:
    etc/scripts/seam2cdi.sh */src/**/*.java */src/**/*.groovy */src/**/*.xhtml

    # commit the results:
    git add                 */src/**/*.java */src/**/*.groovy */src/**/*.xhtml
    git commit -m "$commitMsg"
else
    echo "ERROR: can't find commit to amend. You should run this script during git rebase -i, when editing the commit: $commitMsg"
fi
