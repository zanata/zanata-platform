# Make is used here so that we can suppress script execution in other packages
# with --ignore-scripts, but not have to manually add --ignore-scripts=false to
# each invocation of `npm run <command>`.
# Instead we can just invoke `make <command>`.
# We also get tab-completion of the make commands.

build:
	npm run build --ignore-scripts=false

draft:
	npm run draft --ignore-scripts=false

start:
	npm run start --ignore-scripts=false

test:
	npm run test --ignore-scripts=false


.PHONY: test build
