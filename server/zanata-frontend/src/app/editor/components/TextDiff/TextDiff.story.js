import * as React from 'react'
import { storiesOf } from '@storybook/react'
const TextDiff = require('.')

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('TextDiff', module)
  .add('default', () => (
    <TextDiff
      text1="The original text, whatever it was."
      text2="The changed text, what it now is."/>
  ))
