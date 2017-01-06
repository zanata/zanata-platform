# Make is used here so that we can suppress script execution in other packages
# with --ignore-scripts, but not have to manually add --ignore-scripts=false to
# each invocation of `yarn run <command>`.
# Instead we can just invoke `make <command>`.
# We also get tab-completion of the make commands.

# Save the current dependency versions, including transitive dependencies
# This is to ensure the build will use consistent dependency versions.
# The --dev flag is needed or anything in devDependencies will be ignored.
shrinkwrap:
	npm shrinkwrap --dev

install:
	yarn --ignore-scripts

build:
	yarn run build --ignore-scripts=false

draft:
	yarn run draft --ignore-scripts=false

start:
	yarn run start --ignore-scripts=false

styleguide-build:
	yarn run styleguide-build --ignore-scripts=false

styleguide-server:
	yarn run styleguide-server --ignore-scripts=false

test:
	yarn run test --ignore-scripts=false


.PHONY: test build
