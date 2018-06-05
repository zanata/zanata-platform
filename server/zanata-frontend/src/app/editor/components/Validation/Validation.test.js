/* global describe expect it */
import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import Validation from '.'
import { IntlProvider } from 'react-intl'

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

// TabValidation
const tabSource = `with two\t\ttabs`
const tabTarget = `with one\ttab`

// Tab and HtmlXml
const bothSource = `with two\t\ttabs and <user>1</user>`
const bothTarget = `with one\ttab and <user>1</user><foo>`

describe('Validation', () => {
  it('markup with errors', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <IntlProvider locale={'en'}>
        <Validation
          source={HXTSource}
          target={HXTTarget}
          validationOptions={validations}
        />
      </IntlProvider>)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className='TextflowValidation'>
        <div className='ant-collapse'>
          <div className='ant-collapse-item' role='tablist'>
            <div className='ant-collapse-header' role='tab' aria-expanded='false'>
              <i className='arrow'></i>
              <span> <option>Errors: 1</option>
              </span>
            </div>
          </div>
        </div>
      </div>
    )
    expect(actual).toEqual(expected)
  })
  it('markup with warnings', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <IntlProvider locale={'en'}>
        <Validation
          source={tabSource}
          target={tabTarget}
          validationOptions={validations}
        />
      </IntlProvider>)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className='TextflowValidation'>
        <div className='ant-collapse'>
          <div className='ant-collapse-item' role='tablist'>
            <div className='ant-collapse-header' role='tab' aria-expanded='false'>
              <i className='arrow'></i>
              <span><option>Warnings: 1</option> </span>
            </div>
          </div>
        </div>
      </div>
    )
    expect(actual).toEqual(expected)
  })
  it('markup with warnings and errors', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <IntlProvider locale={'en'}>
        <Validation
          source={bothSource}
          target={bothTarget}
          validationOptions={validations}
        />
      </IntlProvider>)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className='TextflowValidation'>
        <div className='ant-collapse'>
          <div className='ant-collapse-item' role='tablist'>
            <div className='ant-collapse-header' role='tab' aria-expanded='false'>
              <i className='arrow'></i>
              <span><option>Warnings: 1</option> <option>Errors: 1</option>
              </span>
            </div>
          </div>
        </div>
      </div>
    )
    expect(actual).toEqual(expected)
  })
  it('markup no warnings or errors', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <IntlProvider locale={'en'}>
        <Validation
          source={''}
          target={''}
          validationOptions={validations}
        />
      </IntlProvider>)
    const expected = ''
    expect(actual).toEqual(expected)
  })
})
