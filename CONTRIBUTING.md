# Contributing to Zanata

We would love for you to contribute to our source code and to make Zanata
even better than it is today! To make sure we see your feedback contributions
straight away, please follow these guidelines:

## <a name="Feedback_or_Issues">Feedback or Issues</a>
If you find a bug, want a feature or improvement, have an idea for how to make
Zanata better, or just want to tell us what you think of something in Zanata,
please let us know using our [issue system](https://zanata.atlassian.net/)

## <a name="Pull_Requests">Pull Requests</a>
Pull requests welcome!

### <a name="Setup">Setup</a>
The
[Developer Setup Guide](https://github.com/zanata/zanata-server/wiki/Developer-Guide)
shows the dependencies and how to setup Java, Maven, MySQL, JBoss, and IDE.

### Branches
The branches that should be targeted in pull requests
 - **master**: New features, bug fixes and enhancements should target this branch.
   If you are unsure which branch to target pull request, use this branch.
 - **release**: Only urgent bug fixes and documentation should target this
   branch.

### Test against Wildfly
To run all tests against WildFly (takes about 1 hour):

```
mvn clean verify -Dappserver=wildfly8 -DstaticAnalysis -Dchromefirefox
```

### <a name="Commit_Message_Format"> Commit Message Format</a>
We follow the [angular commit message format]
(https://github.com/angular/angular.js/blob/master/CONTRIBUTING.md#commit),
that is:

Each commit message consists of a **header**, a **body** and a **footer**.  The header has a special
format that includes a **type**, a **scope** and a **subject**:

```
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```
The **header** is mandatory and the **scope** of the header is optional.

Any line of the commit message cannot be longer 100 characters! This allows the message to be easier
to read on GitHub as well as in various git tools.

#### Type
Must be one of the following:

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation only changes
- **style**: Changes that do not affect the meaning of the code (white-space, formatting, missing
             semi-colons, etc)
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **perf**: A code change that improves performance
- **test**: Adding missing tests
- **chore**: Changes to the build process or auxiliary tools and libraries such as documentation
generation
- **revert**: reverts a previous commit. The subject should be the header of the reverted commit. In the body it should say: `This reverts commit <hash>.`, where the hash is the SHA of the commit being reverted.

#### Scope
The scope could be anything specifying place of the commit change.
If the pull requrest is targeting an issue, use issue ID like `ZNAT-1234`.
Otherwise you can use the component or element name like `translation memory`
`statistics` and `glossary`, as well as the purposes of pull requests,
like `dependency`, `cleanup`.

#### Subject
The subject contains a succinct description of the change:

* use the imperative, present tense: "change" not "changed" nor "changes"
* do not capitalize first letter
* no dot (.) at the end

#### Body
Just as in the **subject**, use the imperative, present tense: "change" not "changed" nor "changes".
The body should include the motivation for the change and contrast this with previous behavior.

#### Footer
The footer should contain any information about **Breaking Changes** and is also the place to
reference GitHub issues that this commit **Closes**.

**Breaking Changes** should start with the word `BREAKING CHANGE:` with a space or two newlines. The rest of the commit message is then used for this.

#### Live Examples
* [fix(project):shows lock icon when project is readonly](https://github.com/zanata/zanata-server/commit/414c3e3d8038dd10143a30b62226ebd1267709ec)
* [chore: use deltaspike for data access](https://github.com/zanata/zanata-server/commit/5c785b9eb15ccb2ac87cdfb9e0740ee8444f9d1c)

### Submit a Pull Request

1. Make your changes in a new git branch.
2. Create your patch, **including appropriate test cases**.
3. Commit your changes using a descriptive commit message that follows our
    [commit message format](#Commit_Message_Format).
4. Push your branch to GitHub.
5. In GitHub, target the pull request to `zanata:master`.
6. If we suggest changes then:
   a. Make the required updates.
   b. Commit your changes to your branch (e.g. `my-pr-branch`).
    * Push the changes to your GitHub repository (this will update your Pull Request).

For pull request detail, read
[GitHub using pull request](https://help.github.com/articles/using-pull-requests/).

That is it! Thank you for your contribution!

### After your pull request is merged

After your pull request is merged, you may safely delete your branch and
pull the changes from the **master** (or **release** branch if you were
working with it) branch of main (upstream) repository.

