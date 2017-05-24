import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import ValidationOptions from '.'

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('ValidationOptions', module)
  .add('default', () => (
     <ValidationOptions />
  ))
