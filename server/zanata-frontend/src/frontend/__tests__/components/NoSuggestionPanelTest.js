jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import { Icon }  from 'zanata-ui'
import NoSuggestionsPanel from '../../app/editor/components/NoSuggestionsPanel'

describe('NoSuggestionsPanelTest', () => {
  it('NoSuggestionsPanel markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <NoSuggestionsPanel
        message="You're on your own"
        icon="loader"/>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <div
        className="u-posCenterCenter u-textEmpty u-textCenter">
        <div className="u-sMB-1-4">
          <Icon name="loader" size="5" />
        </div>
        <p>You&apos;re on your own</p>
      </div>
    )
    expect(actual).toEqual(expected)
  })
})
