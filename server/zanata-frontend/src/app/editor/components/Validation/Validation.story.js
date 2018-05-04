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
    defaultMessage: 'Number of apostrophes (\' \') in source does not match number in translation. This may lead to other warnings.'
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
  .add('default (no test)', () => (
    <div>
      <p>Validation Messages Default</p>
      <Validation messages={messages.slice(0, 1)}
        validationOptions={validations} />
      <p>Validation Messages Warnings</p>
      <Validation messages={messages.slice(2, 4)}
        validationOptions={validations} />
      <p>Validation Messages Errors</p>
      <Validation messages={messages.slice(0, 2)}
        validationOptions={validations} />
      <p>Validation Messages Mixed</p>
      <Validation messages={messages} validationOptions={validations} />
    </div>
  ))
