jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import ProgressBar from '../../app/editor/components/ProgressBar'

describe('ProgressBarTest', () => {
  it('ProgressBar markup', () => {
    const counts = {
      total: 100,
      approved: 10,
      translated: 20,
      needswork: 25,
      rejected: 5,
      untranslated: 40
    }

    const actual = ReactDOMServer.renderToStaticMarkup(
      <ProgressBar size="small" counts={counts}/>)

    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="Progressbar Progressbar--sm">
        <span
          className="Progressbar-item Progressbar-approved"
          style={{ marginLeft: '0%', width: '10%' }}/>
        <span
          className="Progressbar-item Progressbar-translated"
          style={{ marginLeft: '10%', width: '20%' }}/>
        <span
          className="Progressbar-item Progressbar-needswork"
          style={{ marginLeft: '30%', width: '25%' }}/>
        <span
          className="Progressbar-item Progressbar-rejected"
          style={{ marginLeft: '55%', width: '5%' }}/>
        <span
          className="Progressbar-item Progressbar-untranslated"
          style={{ marginLeft: '60%', width: '40%' }}/>
      </div>
    )
    expect(actual).toEqual(expected)
  })
})
