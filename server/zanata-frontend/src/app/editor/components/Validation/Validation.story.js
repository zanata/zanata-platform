// @ts-nocheck
/* eslint-disable */
import React from 'react'
import { storiesOf } from '@storybook/react'
import Validation from './index.tsx'

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


const source = `with two\t\ttabs`
const target = `with one\ttab`
/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('Validation', module)
  .add('TabValidation', () => (
    <div>
      <h2>Validation Messages for TabValidation</h2>
      <p>source: <input value={source} /> </p>
      <p>target: <input value={target} /> </p>
      <Validation
        source={source}
        target={target}
        localeId={'en-US'}
        validationOptions={validations}  />
    </div>
  ))
