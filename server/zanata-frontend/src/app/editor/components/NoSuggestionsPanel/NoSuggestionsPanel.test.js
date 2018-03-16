import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import { Icon } from '../../../components'
import NoSuggestionsPanel from '.'
import LoaderText from '../../../components/LoaderText'

describe('NoSuggestionsPanelTest', () => {
  it('NoSuggestionsPanel markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <NoSuggestionsPanel
        message="You're on your own"
        icon="search"/>
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div
        className="u-posCenterCenter u-textEmpty u-textCenter">
        <div className="u-sMB-1-4">
          <Icon
            name="search"
            className="s5" />
        </div>
        <p>You&apos;re on your own</p>
      </div>
    )
    expect(actual).toEqual(expected)
  })
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
            <LoaderText
              // @ts-ignore
              loading loadingText='You&apos;re on your own' />
          </div>
        </div>
    )
    expect(actual).toEqual(expected)
  })
})
