jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import Icon from '../../app/components/Icon'
import NoSuggestionsPanel from '../../app/components/NoSuggestionsPanel'

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
        <Icon
          name="loader"
          className="Icon--lg Icon--circle u-sMB-1-4"/>
        <p>You&apos;re on your own</p>
      </div>
    )
    expect(actual).toEqual(expected)
  })
})
