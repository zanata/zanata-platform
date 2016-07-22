jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import { Icon, Row } from 'zanata-ui'
import SuggestionSourceDetails from '../../app/components/SuggestionSourceDetails'

describe('SuggestionSourceDetailsTest', () => {
  it('SuggestionSourceDetails markup (imported)', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionSourceDetails
        suggestion={{
          matchDetails: [
            {
              type: 'IMPORTED_TM',
              transMemorySlug: 'champagne'
            }
          ]
        }}/>
    )
    // Note: slug wrapped as a string here to work around assertion
    //       library, which considers ' champagne' different from
    //       ' ' + 'champagne'
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="TransUnit-details">
        <ul className="u-textMeta u-listInline u-sizeLineHeight-1">
          <li>
            <Row>
              <Icon name="import" size="n1"/> {'champagne'}
            </Row>
          </li>
        </ul>
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('SuggestionSourceDetails markup (local project)', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionSourceDetails
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
        }}/>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="TransUnit-details">
        <ul className="u-textMeta u-listInline u-sizeLineHeight-1">
          <li title="sausages">
            <Row>
              <Icon name="project" size="n1"/> {'Sausages'}
            </Row>
          </li>
          <li>
            <Row>
              <Icon name="version" size="n1"/> {'the-wurst-version'}
            </Row>
          </li>
          <li title="what-a-brat/i-rote-this.txt">
            <Row>
              <Icon name="document" size="n1"/> {'i-rote-this.txt'}
            </Row>
          </li>
        </ul>
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('SuggestionSourceDetails markup (imported)', () => {
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
      <SuggestionSourceDetails
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
        }}/>
    )

    // Note: some of the expected text output must be wrapped in
    //       {} to work around a weakness in the assertion
    //       library, which considers ' champagne' different from
    //       ' ' + 'champagne'
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="TransUnit-details">
        <ul className="u-textMeta u-listInline u-sizeLineHeight-1">
          <li>
            <Row>
              <Icon name="import" size="n1"/> {'champagne'}
            </Row>
          </li>
          <li>
            <Row>
              <Icon name="translate" size="n1"
              /> {3}{' more occurrences'}
            </Row>
          </li>
        </ul>
      </div>
    )
    expect(actual).toEqual(expected)
  })
})
