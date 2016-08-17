# Ensure all dependencies are present at appropriate versions.
setup:
	npm install --ignore-scripts

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
watch: processhtml
	npm run watch --ignore-scripts=false

# Run a local development server backed by a fake Zanata server
watch-fakeserver:
	${MAKE} -j2 watch fakeserver

# Copy index.html into /dist
processhtml:
	npm run processhtml --ignore-scripts=false

# Run react-storybook server for development and testing of React components.
storybook:
	npm run storybook --ignore-scripts=false

# Build a static version of the React component storybook
#  - outputs to /storybook-static
#  - will not display properly from file:// url since it uses an iframe
#  - includes everything from /app/build even though it does not need
#    it all (only needs icons.svg at this point). Not worth the extra complexity
#    to prevent that.
storybook-static:
	npm run build-storybook --ignore-scripts=false

# Build the css and javascript bundles using webpack.
# Files end up as app.css and bundle.js in /app/dist
build: processhtml
	npm run build --ignore-scripts=false

# Run the tests.
test:
	npm test --ignore-scripts=false

.PHONY: test build
