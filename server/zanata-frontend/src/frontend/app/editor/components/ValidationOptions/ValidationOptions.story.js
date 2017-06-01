import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import ValidationOptions from '.'

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('ValidationOptions', module)
  .add('default', () => (
    <ValidationOptions updateValidationOption={action('updateValidationOption')}
      states={{
        'HTML/XML tags': false,
        'Java variables': false,
        'Leading/trailing newline (n)': false,
        'Positional printf (XSI extension)': false,
        'Printf variables': false,
        'Tab characters (t)': false,
        'XML entity reference': false
      }} />
  ))

  .add('half checked', () => (
    <ValidationOptions states={{
      'HTML/XML tags': true,
      'Java variables': true,
      'Leading/trailing newline (n)': false,
      'Positional printf (XSI extension)': false,
      'Printf variables': false,
      'Tab characters (t)': true,
      'XML entity reference': true
    }} />
  ))

  .add('all checked', () => (
    <ValidationOptions states={{
      'HTML/XML tags': true,
      'Java variables': true,
      'Leading/trailing newline (n)': true,
      'Positional printf (XSI extension)': true,
      'Printf variables': true,
      'Tab characters (t)': true,
      'XML entity reference': true
    }} />
  ))
