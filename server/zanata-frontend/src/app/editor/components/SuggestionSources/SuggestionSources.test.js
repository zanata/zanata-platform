
import * as React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import SuggestionSources from '../SuggestionSources'
import SuggestionContents from '../SuggestionContents'
import SuggestionDetailsSummary from '../SuggestionDetailsSummary'

describe('SuggestionSourcesTest', () => {
  it('SuggestionSources markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionSources
        showDiff={false}
        showDetail={() => {}}
        directionClass='testClass'
        suggestion={{
          matchDetails: [
            {
              type: 'IMPORTED_TM',
              transMemorySlug: 'patterson'
            }
          ],
          sourceContents: [
            'There was movement at the station',
            'for the word had passed around'
          ]
        }}/>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="testClass TransUnit-panel TransUnit-source">
        <SuggestionContents
          plural={true}
          contents={[
            'There was movement at the station',
            'for the word had passed around'
          ]}
          compareTo={undefined}/>
        <SuggestionDetailsSummary
          onClick={() => {}}
          suggestion={{
            matchDetails: [
              {
                type: 'IMPORTED_TM',
                transMemorySlug: 'patterson'
              }
            ],
            sourceContents: [
              'There was movement at the station',
              'for the word had passed around'
            ]
          }}/>
      </div>
    )
    expect(actual).toEqual(expected)
  })
})
