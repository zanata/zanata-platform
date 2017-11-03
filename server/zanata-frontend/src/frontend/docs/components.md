# Components

The [components](../src/components) folder is where we keep all **reusable** React
components. Unless you need local state, locale methods, or access to lifecycle
methods, components should be
[functional stateless components](https://facebook.github.io/react/docs/reusable-components.html#stateless-functions).

Each of these components should be listed in the [Styleguide](./styleguide.md)
for easy reference.

All custom components should be composed of the [Base](../src/components/Base.jsx)
component, which adds the props `theme` and `atomic`, which should be used for
styling instead of className. For more detals on styling components, see
[Styles](./styles.md).

Each component should have:

- A default export
- propTypes (with documentation for each one)
- defaultProps if needed
- An example readme.md (Used in the styleguide) Not needed if it is not meant to be used directly
- (Tests)[./testing.md]
- Be added to [components/index.js](../src/components/index.js) for easy importing in from other files
