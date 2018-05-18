// @ts-nocheck
/* eslint-disable */
import React from 'react'
import { storiesOf } from '@storybook/react'
import Validation from './index.tsx'


// TODO: Generate these with validation factory
const messages = [
  {
    id: 'html-xml-tags',
    label: 'HTML/XML tags',
    defaultMessage: 'Check that XML/HTML tags are consistent.'
  },
  {
    id: 'html-xml-tags',
    label: 'HTML/XML tags',
    defaultMessage: 'Check that XML entity are complete.'
  },
  {
    id: 'java-variables',
    label: 'Java Variables',
    defaultMessage: 'Number of apostrophes (\' \') in source does not match number in translation. This may lead to other warnings.',
    description: 'Lists variables that appear a different number of times between source and target strings'
  },
  {
    id: 'java-variables',
    label: 'Java Variables',
    defaultMessage: 'Inconsistent count for variables: x, y ,z'
  }
]

const validations =
  [
    {
      id: 'html-xml-tags',
      label: 'HTML/XML tags',
      active: true,
      disabled: true
    },
    {
      id: 'java-variables',
      label: 'Java variables',
      active: true,
      disabled: false
    },
    {
      id: 'leading-trailing-newline',
      label: 'Leading/trailing newline',
      active: true,
      disabled: false
    },
    {
      id: 'positional-printf',
      label: 'Positional printf (XSI extension)',
      active: false,
      disabled: false
    },
    {
      id: 'printf-variables',
      label: 'Printf variables',
      active: false,
      disabled: false
    },
    {
      id: 'tab-characters',
      label: 'Tab characters',
      active: true,
      disabled: false
    },
    {
      id: 'xml-entity-reference',
      label: 'XML entity reference',
      active: false,
      disabled: false
    }
  ]

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('Validation', module)
  .add('default', () => (
    <div>
      <h2>Validation Messages Default</h2>
      <Validation messages={messages.slice(0, 1)}
        validationOptions={validations} />
      <h2>Validation Messages with Description Tooltip</h2>
      <Validation messages={messages.slice(2, 3)}
        validationOptions={validations} />
      <h2>Validation Messages Warnings</h2>
      <Validation messages={messages.slice(2, 4)}
        validationOptions={validations} />
      <h2>Validation Messages Errors</h2>
      <Validation messages={messages.slice(0, 2)}
        validationOptions={validations} />
      <h2>Validation Messages Mixed</h2>
      <Validation messages={messages} validationOptions={validations} />
    </div>
  ))
