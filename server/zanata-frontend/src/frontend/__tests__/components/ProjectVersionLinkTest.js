jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import ProjectVersionLink from '../../app/editor/components/ProjectVersionLink'

describe('ProjectVersionLinkTest', () => {
  it('ProjectVersionLink markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <ProjectVersionLink project={{name: 'Weight Gain'}}
        version="4000"
        url="https://en.wikipedia.org/wiki/Weight_Gain_4000"/>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <a href="https://en.wikipedia.org/wiki/Weight_Gain_4000"
         className="Link--invert Header-item u-inlineBlock">
        <span className="u-sPH-1-4 u-sizeWidth1 u-gtemd-hidden">
          <i className="i i--arrow-left"></i>
        </span>
        <span className="Editor-currentProject u-sm-hidden u-sML-1-2">
          <span>Weight Gain</span> <span
            className="u-textMuted">4000</span>
        </span>
      </a>
    )
    expect(actual).toEqual(expected)
  })
})
