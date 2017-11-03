# Styles

## Design Concepts

There are some core concepts that Zanata and each component are designed around
to keep a consistent look and feel.

### Color Palette

To avoid using too many colors, stick to the colors used in
[constants/styles.js](../src/constants/styles.js). For different shades of white
or black, atomic.css has shortcut variables (i.e #fff.5, is 50% white).
Most of the time `opacity` or `filter: brightness()` can be used to change the
shade of a variable based color. This avoids many color declarations and
provides a single source of truth for colors.

### Rhythm

All measurements/spacing should be based on vertical rhythm. The rhythm is based
on the line-height, which is set at 1.5, which at the default browser font-size,
is 24px. There are rhythm variables available in [constants/styles.js](../src/constants/styles.js).

```
M(r1) = margin: 1.5rem;
Px(rq) = padding-left: .375rem; padding-right: .375rem;
Pend(e1h) = padding-right: 2.25em;
```

Most of the time, `rem` is the unit of measurement that should be used,
but for specific situations (e.g. sizing a component by just changing it's font
size), using ems is more appropriate. If you are not sure whether to use rems or
ems, see [REM vs EM – The Great Debate](http://zellwk.com/blog/rem-vs-em/).

If percentages are needed for width or height, atomic.css allows the
use of % or fractions directly within the () of a class. e.g. `W(1/4)`.

The only time to use pxs is for measurements less than 3px. e.g. borders.

### Modular Scale

Type and Icon size is based on a [modular scale](http://www.modularscale.com/?1&rem&1.2&web&text)
of 1.2 (minor third). This allows a wide range of font sizes, but without any
that are too close to each other.

### Named z-indexes

To ensure the z-space of the application is easy to understand in one place,
name each component that specifies a specific z-index. This ensures components
appear in the order you expect.

```
tooltipArrow: 100,
select: 300,
backDrop: 400,
tooltip: 400,
modal: 500
```

### Box shadows

To keep consistent z-space levels/depth for each component, there are 5
box-shadow levels available. `sh1-sh5`, `sh1` being the lowest and `sh5` being the
highest. A button default may only use `sh1`, where a modal may use `sh5`.

### Other variables

To keep things consistent, define a variable for every style that may be reused
to avoid repetition or inconsistencies. e.g. transitions, borders, etc.

## CSS Files

### Base

These are the default html styles. Styles here should be kept very minimal and
should be the most common styles used in the application. E.g. don't set custom
sizes for headings (h1, h2, etc) as the default size for a h2 is likely to
change a lot throughout the application, so is better to set explicitly in
context. Favour "reseting" default elements as much as possible.

This allows developers to use the correct html tags in each use-case without
the fear of having to "reset defaults" or just use a div.

### Atomic CSS

Most of Zanata's CSS should be in this file. **It is a generated file and
should not be edited**. This file is added to by declaring classes in Javascript
matching a [specific syntax](acss.io/guides/syntax.html).

Each class is usually single-purpose (i.e. Maps to a single CSS property).
The configuration and variables available to Atomic CSS are declared in
[atomicCssConfig.js](../atomicCssConfig.js). For more information on how to
use atomic CSS classes (including a reference of
[every class available](http://acss.io/reference)), see
[acss.io](http://acss.io/).

#### Styling Components

All custom components should be composed of the [Base](../src/components/Base.jsx)
component, which adds `theme` and `atomic` props.  `theme` should be used when
creating new components, as it allows other components to extend all the classes
of the component it is composing. `atomic` is used mostly for specific in-app
overrides, as it takes precedence over any theme classes.

Each component should declare a single `classes` variable, directly after it's
imports. This classes variable can then be merged with it's own theme prop (to
allow for external theming) and then it can be merged again for any state-based
classes.

The `classes` variable can contain classes for more than 1 element, if more than
one element is declared, the root element should be called `root`. See the
[LoaderText](../src/components/LoaderText.jsx) component to see how this might work.

Each `theme` or `atomic` object should include a keys for each property. This
allows the object to be merged and only assign a single set of classes for each
property. Alphabetical order is also preferred, to avoid duplicate keys.

For example:

```js
const classes = {
  root: {
    base: {
      c: 'C(pri)', // Color - Primary
      fz: 'Fz(ms1)', // Font size - modular scale 1
      p: 'P(r1)' // Padding - 1 rhythm
    },
    danger: {
      c: 'C(danger)' // If danger, override color
    }
  },
  container: {
    ai: 'Ai(c)' // Align items - center
    d: 'D(f)' // Display - flex
  }
}
```

### Animations

Animation definitions are something that can't be declared in atomic.css, but
they can be referenced. When you need to declare a new animation, do it in this
file, then reference it from atomic.css.

### Extra

**This file should be a last resort**. If you find something is not possible in
atomic.css, you can add it here. Where possible, try and match the syntax of
atomic.css (see if there is an emmet version available) e.g. `appearance: none;`
is not available in atomic.css so add `Ap(n)` to extra.css.

## Icons

Icons are handled by the [Icons](../src/components/Icons) and
[Icon](../src/components/Icon) components. The source for all icons is the
[svgs folder](../src/components/Icons/svgs) in the Icons component.

On build, there are scripts that will generate an Icon sprite and list all the
icons to be displayed in the styleguide. To understand how to use the Icon
component, see the [Styleguide](./styleguide.md).

## Styleguide

This CSS is specific to the [Styleguide](./styleguide.md) and is not used in
production. It is used for styleguide-specific overrides of its included
classes.
