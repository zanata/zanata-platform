jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import { TranslationItem } from './TransUnitTranslationPanel'
import Textarea from 'react-textarea-autosize'
import SyntaxHighlighter, { registerLanguage }
  from 'react-syntax-highlighter/light'
import xml from 'react-syntax-highlighter/languages/hljs/xml'
import { atelierLakesideLight } from 'react-syntax-highlighter/styles/hljs'

registerLanguage('xml', xml)

/* eslint-disable max-len */
describe('TransUnitTranslationPanelTest', () => {
  it('TranslationItem Syntax Highlighting on markup', () => {
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
  it('TranslationItem Syntax Highlighting off markup', () => {
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
        syntaxOn={false} />
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
})
