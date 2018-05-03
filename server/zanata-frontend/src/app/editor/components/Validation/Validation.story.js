// @ts-nocheck
/* eslint-disable */
import React from 'react'
import { storiesOf } from '@storybook/react'
import Validation from './index.tsx'

const messages = [
  {
    id: 'html-xml-tags',
    label: 'HTML/XML tags',
    defaultMessage: 'Check that XML/HTML tags are consistent.'
  },
  {
    id: 'java-variables',
    label: 'Java Variables',
    defaultMessage: 'Number of apostrophes (\' \') in source does not match number in translation. This may lead to other warnings.'
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
  .add('default (no-test)', () => (
    <div>
      <Validation messages={messages} validationOptions={validations} />
    </div>
  ))
