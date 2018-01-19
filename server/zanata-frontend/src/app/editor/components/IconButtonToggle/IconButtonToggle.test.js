
import React from 'react'
import ReactDOMServer from 'react-dom/server'
import IconButton from '../IconButton'
import IconButtonToggle from '.'

describe('IconButtonToggleTest', () => {
  it('IconButtonToggle markup', () => {
    let clickFun = function (e) {}
    const actual = ReactDOMServer.renderToStaticMarkup(<IconButtonToggle
      icon="classical"
      title="Mozart"
      onClick={clickFun}
      active={false}
      disabled={true} />)

    const expected = ReactDOMServer.renderToStaticMarkup(
      <IconButton
        icon="classical"
        title="Mozart"
        onClick={clickFun}
        disabled={true}
        className="EditorButton Button--snug u-roundish Button--invisible"/>
    )
    expect(actual).toEqual(expected)
  })

  it('IconButtonToggle adds active style', () => {
    var clickFun = function (e) {}

    const actual = ReactDOMServer.renderToStaticMarkup(<IconButtonToggle
      icon="classical"
      title="Mozart"
      onClick={clickFun}
      active={true}
      className="pop-icon"/>)

    const expected = ReactDOMServer.renderToStaticMarkup(
      <IconButton
        active={true}
        className="pop-icon EditorButton Button--snug u-roundish Button--invisible is-active"
        icon="classical"
        title="Mozart"
        onClick={clickFun} />
    )
    expect(actual).toEqual(expected)
  })
})
