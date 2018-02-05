import React from 'react'
import ReactDOMServer from 'react-dom/server'
import { TranslationItem } from './TransUnitTranslationPanel'
import Textarea from 'react-textarea-autosize'
import SyntaxHighlighter, { registerLanguage }
  from 'react-syntax-highlighter/light'
import xml from 'react-syntax-highlighter/languages/hljs/xml'
import { atelierLakesideLight } from 'react-syntax-highlighter/styles/hljs'

registerLanguage('xml', xml)

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
        phrase={{id: '1'}}
        selected
        selectedPluralIndex={1}
        selectPhrasePluralIndex={defaultFunc}
        setTextArea={defaultFunc}
        textChanged={defaultFunc}
        translation={'Je ne parle pas français'}
        directionClass={''}
        syntaxOn />
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
        phrase={{id: '1'}}
        selected={false}
        selectedPluralIndex={1}
        selectPhrasePluralIndex={defaultFunc}
        setTextArea={defaultFunc}
        textChanged={defaultFunc}
        translation={'Je ne parle pas français'}
        directionClass={''}
        syntaxOn />
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
        phrase={{id: '1'}}
        selected={false}
        selectedPluralIndex={1}
        selectPhrasePluralIndex={defaultFunc}
        setTextArea={defaultFunc}
        textChanged={defaultFunc}
        translation={'Je ne parle pas français'}
        directionClass={''}
        syntaxOn />
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
      </div>
    )
    expect(actual).toEqual(expected)
  })
  /* eslint-enable max-len */
})
