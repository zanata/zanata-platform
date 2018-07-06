/* global describe it expect */
import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import { Icon } from '../../../components'

import SuggestionDetailsSummary from '.'

describe('SuggestionDetailsSummaryTest', () => {
  it('SuggestionDetailsSummary markup (imported)', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionDetailsSummary
        suggestion={{
          matchDetails: [
            {
              type: 'IMPORTED_TM',
              transMemorySlug: 'champagne'
            }
          ]
        }} />
    )
    // Note: slug wrapped as a string here to work around assertion
    //       library, which considers ' champagne' different from
    //       ' ' + 'champagne'
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="TransUnit-details">
        <ul className="u-textMeta u-listInline u-sizeLineHeight-1">
          <li>
            <Icon name="import" className="n1" /> {'champagne'}
          </li>
        </ul>
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('SuggestionDetailsSummary markup (local project)', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionDetailsSummary
        suggestion={{
          matchDetails: [
            {
              type: 'LOCAL_PROJECT',
              projectId: 'sausages',
              projectName: 'Sausages',
              version: 'the-wurst-version',
              documentPath: 'what-a-brat',
              documentName: 'i-rote-this.txt'
            }
          ]
        }} />
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="TransUnit-details">
        <ul className="u-textMeta u-listInline u-sizeLineHeight-1">
          <li title="sausages">
            <Icon name="project" className="n1" /> {'Sausages'}
          </li>
          <li>
            <Icon name="version" className="n1" /> {'the-wurst-version'}
          </li>
          <li className="DocName" title="what-a-brat/i-rote-this.txt">
            <Icon name="document" className="n1" />
            <span className="ellipsis">{'i-rote-this.txt'}</span>
          </li>
        </ul>
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('SuggestionDetailsSummary markup (imported)', () => {
    // different type from top match so it is obvious
    // if display is based on the wrong one
    const remainingMatch = {
      type: 'LOCAL_PROJECT',
      projectId: 'sausages',
      projectName: 'Sausages',
      version: 'the-wurst-version',
      documentPath: 'what-a-brat',
      documentName: 'i-rote-this.txt'
    }

    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionDetailsSummary
        suggestion={{
          matchDetails: [
            {
              type: 'IMPORTED_TM',
              transMemorySlug: 'champagne'
            },
            remainingMatch,
            remainingMatch,
            remainingMatch
          ]
        }} />
    )

    // Note: some of the expected text output must be wrapped in
    //       {} to work around a weakness in the assertion
    //       library, which considers ' champagne' different from
    //       ' ' + 'champagne'
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="TransUnit-details">
        <ul className="u-textMeta u-listInline u-sizeLineHeight-1">
          <li>
            <Icon name="import" className="n1" /> {'champagne'}
          </li>
          <li>
            <Icon name="translate" className="n1"
            /> {3}{' more occurrences'}
          </li>
        </ul>
      </div>
    )
    expect(actual).toEqual(expected)
  })
})
