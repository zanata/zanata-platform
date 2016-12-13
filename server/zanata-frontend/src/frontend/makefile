# Make is used here so that we can suppress script execution in other packages
# with --ignore-scripts, but not have to manually add --ignore-scripts=false to
# each invocation of `npm run <command>`.
# Instead we can just invoke `make <command>`.
# We also get tab-completion of the make commands.

# Save the current dependency versions, including transitive dependencies
# This is to ensure the build will use consistent dependency versions.
# The --dev flag is needed or anything in devDependencies will be ignored.
shrinkwrap:
	npm shrinkwrap --dev

install:
	npm install --ignore-scripts

build:
	npm run build --ignore-scripts=false

draft:
	npm run draft --ignore-scripts=false

start:
	npm run start --ignore-scripts=false

styleguide-build:
	npm run styleguide-build --ignore-scripts=false

styleguide-server:
	npm run styleguide-server --ignore-scripts=false

test:
	npm run test --ignore-scripts=false


.PHONY: test build
