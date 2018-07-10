// @ts-nocheck
/* eslint-disable */
import React from 'react'
import { storiesOf } from '@storybook/react'
import Validation from './index.tsx'

const validations =
  [
    {
      id: 'HTML_XML',
      label: 'HTML/XML tags',
      active: true,
      disabled: true
    },
    {
      id: 'JAVA_VARIABLES',
      label: 'Java variables',
      active: true,
      disabled: false
    },
    {
      id: 'NEW_LINE',
      label: 'Leading/trailing newline',
      active: true,
      disabled: false
    },
    {
      id: 'PRINTF_XSI_EXTENSION',
      label: 'Positional printf (XSI extension)',
      active: true,
      disabled: false
    },
    {
      id: 'PRINTF_VARIABLES',
      label: 'Printf variables',
      active: true,
      disabled: false
    },
    {
      id: 'TAB',
      label: 'Tab characters',
      active: true,
      disabled: false
    },
    {
      id: 'XML_ENTITY',
      label: 'XML entity reference',
      active: true,
      disabled: false
    }
  ]

// HtmlXmlTag Validator
const HXTSource = '<group><users><user>1</user></users></group>'
const HXTTarget = '<group><users><user>1</user></users><foo></group>'

// JavaVariablesValidation
const JavaSource = "Testing string with variable {0}"
const JavaTarget = "Testing string with no variables"

// NewLineLeadTrailValidation
const NLSource = '\nTesting string with leading new line'
const NLTarget = 'Different string with the newline removed'

// PrintfVariablesValidation
const PFsource = 'Testing string with variable %1v'
const PFtarget = 'Testing string with no variables'

// PrintXSIExtensionValidation
const PXIsource = '%s: Read error at byte %s, while reading %lu byte'
const PXItarget = '%1$s：Read error while reading %3$lu bytes，at %2$s'

// TabValidation
const tabSource = `with two\t\ttabs`
const tabTarget = `with one\ttab`

// XMLEnitityValidation
const XMLsource = 'Source string'
const XMLtarget = 'Target string: bla bla &test'

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('Validation', module)
  .add('All Validators', () => (
    <>
      <h2>Validation Messages for HtmlXmlTagValidation</h2>
      <p>source: <input value={HXTSource} /> </p>
      <p>target: <input value={HXTTarget} /> </p>
      <Validation
        source={HXTSource}
        target={HXTTarget}
        validationOptions={validations} />
      <h2>Validation Messages for JavaVariablesValidation</h2>
      <p>source: <input value={JavaSource} /> </p>
      <p>target: <input value={JavaTarget} /> </p>
      <Validation
        source={JavaSource}
        target={JavaTarget}
        validationOptions={validations} />
      <h2>Validation Messages for NewLineLeadTrailValidation</h2>
      <p>source: <input value={NLSource} /> </p>
      <p>target: <input value={NLTarget} /> </p>
      <Validation
        source={NLSource}
        target={NLTarget}
        validationOptions={validations} />
      <h2>Validation Messages for PrintfVariablesValidation</h2>
      <p>source: <input value={PFsource} /> </p>
      <p>target: <input value={PFtarget} /> </p>
      <Validation
        source={PFsource}
        target={PFtarget}
        validationOptions={validations} />
      <h2>Validation Messages for PrintXSIExtensionValidation</h2>
      <p>source: <input value={PXIsource} /> </p>
      <p>target: <input value={PXItarget} /> </p>
      <Validation
        source={PXIsource}
        target={PXItarget}
        validationOptions={validations} />
      <h2>Validation Messages for TabValidation</h2>
      <p>source: <input value={tabSource} /> </p>
      <p>target: <input value={tabTarget} /> </p>
      <Validation
        source={tabSource}
        target={tabTarget}
        validationOptions={validations} />
      <h2>Validation Messages for XMLEnitityValidation</h2>
      <p>source: <input value={XMLsource} /> </p>
      <p>target: <input value={XMLtarget} /> </p>
      <Validation
        source={XMLsource}
        target={XMLtarget}
        validationOptions={validations} />
    </>
  ))
