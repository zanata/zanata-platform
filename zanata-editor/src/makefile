# Ensure all dependencies are present at appropriate versions.
setup:
	npm install

# Save the current dependency versions, including transitive dependencies
# This is to ensure the build will use consistent dependency versions.
# The --dev flag is needed or anything in devDependencies will be ignored.
shrinkwrap:
	npm shrinkwrap --dev

# Run a server that implements some of the Zanata API with some dummy data.
# The server runs on localhost:7878 and has 0.5 to 5s of random latency.
fakeserver:
	cd node_modules/fake-zanata-server && npm start

# Run the app on a local development server, automatically rebuild and refresh
# when the code changes (sprites are only built at the beginning).
watch: spritesheet
	npm run watch

# Run a local development server backed by a fake Zanata server
watch-fakeserver:
	${MAKE} -j2 watch fakeserver

# Build and inject an icon spritesheet into index.html, which is placed in the
# build folder. The spritesheet combines all the individual sprites in
# app/components/icons/images
spritesheet:
	npm run spritesheet

# Like spritesheet, but makes a static file that can be used in the storybook
# rather than injecting the spritesheet into the index file.
storybook-spritesheet:
	npm run storybook-spritesheet

# Run react-storybook server for development and testing of React components.
storybook: storybook-spritesheet
	npm run storybook

# Build a static version of the React component storybook
#  - outputs to /storybook-static
#  - will not display properly from file:// url since it uses an iframe
#  - includes everything from /app/build even though it does not need
#    it all (only needs icons.svg at this point). Not worth the extra complexity
#    to prevent that.
storybook-static: storybook-spritesheet
	npm run build-storybook

# Build the css and javascript bundles using webpack.
# Files end up as app.css and bundle.js in /app/build
build: spritesheet
	npm run build

# Run the tests.
test:
	npm test

.PHONY: test build
