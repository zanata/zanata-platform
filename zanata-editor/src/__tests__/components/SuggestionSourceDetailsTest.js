jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import Icon from '../../app/components/Icon'
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
            <Icon name="import" className="Icon--xsm"/> {'champagne'}
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
            <Icon name="project" className="Icon--xsm"/> {'Sausages'}
          </li>
          <li>
            <Icon name="version" className="Icon--xsm"/> {'the-wurst-version'}
          </li>
          <li title="what-a-brat/i-rote-this.txt">
            <Icon name="document" className="Icon--xsm"/> {'i-rote-this.txt'}
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
            <Icon name="import" className="Icon--xsm"/> {'champagne'}
          </li>
          <li>
            <Icon name="translate" class="Icon--xsm"
            /> {3}{' more occurrences'}
          </li>
        </ul>
      </div>
    )
    expect(actual).toEqual(expected)
  })
})
