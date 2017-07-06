jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import CancellableProgressBar from './CancellableProgressBar'
import { ProgressBar } from 'react-bootstrap'
import { isProcessEnded } from '../../utils/EnumValueUtils'

describe('CancellableProgressBarTest', () => {
  it('can render CancellableProgressBar markup', () => {
    const clickFun = () => {}
    const processShape = {
      url: '/rest/process/key/TMMergeForVerKey-1-ja',
      percentageComplete: 0,
      statusCode: 'Running'
    }
    const actual = ReactDOMServer.renderToStaticMarkup(
      <CancellableProgressBar onCancelOperation={clickFun}
        processStatus={processShape} buttonLabel='Cancel TM Merge'
        queryProgress={clickFun} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>
        <ProgressBar now={0}label={' 0%'} />
        <button type='button' className='btn-danger btn btn-primary'>
          Cancel TM Merge
        </button>
      </div>
    )
    expect(actual).toEqual(expected)
  })
  it('detects loading process completion', () => {
    const cancelledShape = {
      url: '/rest/process/key/TMMergeForVerKey-1-ja',
      percentageComplete: 0,
      statusCode: 'Cancelled'
    }
    const clickFun = () => {}
    // Test the isProcessEnded utils function
    const cancelled = isProcessEnded(cancelledShape)
    const cancelledExpected = true
    expect(cancelled).toEqual(cancelledExpected)
    // Test the CancellableProgressBar markup cancel button is disabled
    const actual = ReactDOMServer.renderToStaticMarkup(
      <CancellableProgressBar onCancelOperation={clickFun}
        processStatus={cancelledShape} buttonLabel='Cancel TM Merge'
        queryProgress={clickFun} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>
        <ProgressBar now={0}label={' 0%'} />
        <button disabled type='button' className='btn-danger btn btn-primary'>
          Cancel TM Merge
        </button>
      </div>
    )
    expect(actual).toEqual(expected)
  })
})
