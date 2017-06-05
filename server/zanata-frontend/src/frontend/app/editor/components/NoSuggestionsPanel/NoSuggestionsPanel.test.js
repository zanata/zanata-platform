jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import { Icon } from '../../../components'
import NoSuggestionsPanel from '.'

describe('NoSuggestionsPanelTest', () => {
  it('NoSuggestionsPanel markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <NoSuggestionsPanel
        message="You are going to have to wait"
        icon="search"/>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <div
        className="u-posCenterCenter u-textEmpty u-textCenter">
        <div className="u-sMB-1-4">
          <Icon name="search" className="s5" />
        </div>
        <p>You are going to have to wait</p>
      </div>
    )
    expect(actual).toEqual(expected)
  })
})
