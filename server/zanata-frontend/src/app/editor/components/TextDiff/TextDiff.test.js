import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import TextDiff from '.'

describe('TextDiffTest', () => {
  it('TextDiff markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TextDiff
        text1="It was the best of times"
        text2="It was the worst of times" />
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="Difference">
        <span>It was the </span>
        <del>best</del>
        <ins>worst</ins>
        <span> of times</span>
      </div>
    )
    expect(actual).toEqual(expected)
  })
})
