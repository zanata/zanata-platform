JIRA issue URL: *[paste URL here]*

*[Add a short description of what is changed here.]*


## Reviewer tasks

The following is based on the
[Zanata Development Guidelines](https://github.com/zanata/zanata-server/wiki/Development-Guidelines).
As a reviewer, you should check the guidelines regularly for updates, and just
to refresh your memory.


The reviewer can use this list for reference to make sure
they have considered all the important points.

Use the checkboxes or not, whatever works best for you.

 - Code quality
   - [ ] Code passes style check (checkstyle/eslint)
   - [ ] Code is clear and concise
   - [ ] UI code is internationalised
   - [ ] Code has adequate comments where appropriate
   - Code is in an appropriate place
     - [ ] Classes are in appropriate packages
     - [ ] Code fits with class's responsibilities (SRP)
   - [ ] Configuration files are documented enough
   - If code is removed
     - [ ] No remaining code references the removed code
       - [ ] No orphaned rewrite rules
       - [ ] No orphaned config settings
     - [ ] No remaining comments refer to the removed code
     - [ ] Unused imports and libraries are removed
 - Testing
   - [ ] New code has tests
   - [ ] Changed code has tests
   - [ ] All tests pass
   - [ ] Test coverage stable or increased
 - Documentation
   - [ ] New features are documented
   - [ ] Docs removed for deleted features
   - [ ] Docs updated for all changed features
   - [ ] Developer/sysadmin docs updated if appropriate
 - If there are new dependencies
   - [ ] Does each new dependency pass all the
         [Considerations for new dependencies](https://github.com/zanata/zanata-server/wiki/Development-Guidelines#new-technologies-and-dependencies)?


----
*This template can be updated in .github/PULL_REQUEST_TEMPLATE.md*
