# Styleguide

This is where all reusable component documentation should be easily viewable
along with an example. It is generated using [react-styleguidist](https://github.com/sapegin/react-styleguidist). There are
some cases where a component may seem broken in the styleguide, but it works
perfectly well in the application. This is usually due to limitations in
react-styleguidist.

Each example is generated from a readme.md file in each component's folder.
See (react-styleguidist)[https://github.com/sapegin/react-styleguidist] for more
documentation.

To run the Styleguide, use `npm run styleguide-server`. Then go to [http://localhost:3000](http://localhost:3000).

Each component should have:

- PropTypes
- A Description
- A live-editable example(s)

An example can be excluded if the component should never be used directly. If
an there are props that change how a component functions, examples should for
each configuration should be provided.

The configuration for the styleguide is in [styleguide.config.js](../styleguide.config.js). The base template is in [styleguide.html](../styleguide.html).
