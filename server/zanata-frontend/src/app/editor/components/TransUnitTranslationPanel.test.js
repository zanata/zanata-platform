/* global jest */
import React from 'react'
import ReactDOMServer from 'react-dom/server'
import { TranslationItem } from './TransUnitTranslationPanel'
import Textarea from 'react-textarea-autosize'
import SyntaxHighlighter, { registerLanguage }
  from 'react-syntax-highlighter/light'
import Validation from './Validation'
import xml from 'react-syntax-highlighter/languages/hljs/xml'
import { atelierLakesideLight } from 'react-syntax-highlighter/styles/hljs'

jest.mock('./Validation')

registerLanguage('xml', xml)

const permissions = {
  reviewer: true,
  translator: true
}

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

const phrase = {
  id: '1',
  sources: ['source text']
}

/* global describe expect it */
describe('TransUnitTranslationPanel', () => {
  it('renders Syntax Highlighting markup on selected && syntax enabled', () => {
    const defaultFunc = () => {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TranslationItem
        dropdownIsOpen={false}
        index={1}
        isPlural={false}
        onSelectionChange={defaultFunc}
        phrase={phrase}
        selected
        selectedPluralIndex={1}
        selectPhrasePluralIndex={defaultFunc}
        setTextArea={defaultFunc}
        textChanged={defaultFunc}
        translation={'Je ne parle pas français'}
        validationOptions={validations}
        directionClass={''}
        syntaxOn
        permissions={permissions} />
    )
    const lineStyle = {
      padding: '0.5rem',
      width: '90%',
      whiteSpace: 'pre-wrap',
      wordWrap: 'break-word'
    }
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="TransUnit-item">
        <Textarea
          ref={defaultFunc}
          className={' TransUnit-text'}
          disabled={false}
          rows={1}
          value={'Je ne parle pas français'}
          placeholder="Enter a translation…"
          onFocus={defaultFunc}
          onChange={defaultFunc}
          onSelect={defaultFunc} />
        <SyntaxHighlighter
          language='html'
          style={atelierLakesideLight}
          wrapLines
          lineStyle={lineStyle}>
          {'Je ne parle pas français'}
        </SyntaxHighlighter>
        <Validation
          source={phrase.sources[0]}
          target={'Je ne parle pas français'}
          validationOptions={validations} />
      </div>
    )
    expect(actual).toEqual(expected)
  })
  /* eslint-disable max-len */
  it('doesnt render Syntax Highlighting markup on !selected && syntax enabled', () => {
    const defaultFunc = () => {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TranslationItem
        dropdownIsOpen={false}
        index={1}
        isPlural={false}
        onSelectionChange={defaultFunc}
        phrase={phrase}
        selected={false}
        selectedPluralIndex={1}
        selectPhrasePluralIndex={defaultFunc}
        setTextArea={defaultFunc}
        textChanged={defaultFunc}
        translation={'Je ne parle pas français'}
        validationOptions={validations}
        directionClass={''}
        syntaxOn
        permissions={permissions} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="TransUnit-item">
        <Textarea
          ref={defaultFunc}
          className={' TransUnit-text'}
          disabled={false}
          rows={1}
          value={'Je ne parle pas français'}
          placeholder="Enter a translation…"
          onFocus={defaultFunc}
          onChange={defaultFunc}
          onSelect={defaultFunc} />
        <Validation
          source={phrase.sources[0]}
          target={'Je ne parle pas français'}
          validationOptions={validations} />
      </div>
    )
    expect(actual).toEqual(expected)
  })
  it('doesnt render Syntax Highlighting markup on selected && !syntax enabled', () => {
    const defaultFunc = () => {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TranslationItem
        dropdownIsOpen={false}
        index={1}
        isPlural={false}
        onSelectionChange={defaultFunc}
        phrase={phrase}
        selected={false}
        selectedPluralIndex={1}
        selectPhrasePluralIndex={defaultFunc}
        setTextArea={defaultFunc}
        textChanged={defaultFunc}
        translation={'Je ne parle pas français'}
        validationOptions={validations}
        directionClass={''}
        syntaxOn
        permissions={permissions} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="TransUnit-item">
        <Textarea
          ref={defaultFunc}
          className={' TransUnit-text'}
          disabled={false}
          rows={1}
          value={'Je ne parle pas français'}
          placeholder="Enter a translation…"
          onFocus={defaultFunc}
          onChange={defaultFunc}
          onSelect={defaultFunc} />
        <Validation
          source={phrase.sources[0]}
          target={'Je ne parle pas français'}
          validationOptions={validations} />
      </div>
    )
    expect(actual).toEqual(expected)
  })
  /* eslint-enable max-len */
})
