## With Homebrew

1. Install [Homebrew](http://brew.sh/): `ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"`
  - If you already have Homebrew installed, run `brew update` to pull in the latest formulae.
2. If you **don't** have Java 1.8+, you can install it using `brew install brew-cask && brew cask install java`.
3. Install the zanata-client: `brew install zanata-client`
4. Now you can run `zanata-cli --help` for more options.

### Upgrading

1. Run `brew update`
2. Run `brew upgrade zanata-client`
