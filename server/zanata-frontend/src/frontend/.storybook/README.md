# Component Stories

There is a "Storybook" that shows our React components with a representative set
of states for each. This is useful during development and manual testing of
components. Components should be developed in the storybook first, then used in
the app.

Made with [react-storybook](https://github.com/kadirahq/react-storybook).

## Usage

Local storybook on [localhost:9001](http://localhost:9001/) with hot reload:

```
make storybook
```

Build a static deployable storybook in `storybook-static`. This could be put in
gh-pages branch, for example.

```
make storybook-static
```

## Code

 - `story.js` is next to a component's index file. It describes the states for a
   component.
 - `stories.js` is in the component directory, and is just to combine all the
   `story.js` files. It only exists so that the main config does not have lots
   of long import paths in it.
 - `/.storybook/config.js` sets up the environment for components and imports
   all the stories.
