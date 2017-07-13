/* global jest describe it expect */
jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import CancellableProgressBar from './CancellableProgressBar'
import { ProgressBar } from 'react-bootstrap'
import { isProcessEnded } from '../../utils/EnumValueUtils'

const callback = () => {}

describe('CancellableProgressBar', () => {
  it('can render CancellableProgressBar markup', () => {
    const processStatus = {
      url: '/rest/process/key/TMMergeForVerKey-1-ja',
      percentageComplete: 0,
      statusCode: 'Running'
    }
    const actual = ReactDOMServer.renderToStaticMarkup(
      <CancellableProgressBar onCancelOperation={callback}
        processStatus={processStatus} buttonLabel='Cancel TM Merge'
        queryProgress={callback} />
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
    const cancelledStatus = {
      url: '/rest/process/key/TMMergeForVerKey-1-ja',
      percentageComplete: 0,
      statusCode: 'Cancelled'
    }
    // Test the isProcessEnded utils function
    const cancelled = isProcessEnded(cancelledStatus)
    const cancelledExpected = true
    expect(cancelled).toEqual(cancelledExpected)
    // Test the CancellableProgressBar markup cancel button is disabled
    expect(ReactDOMServer.renderToStaticMarkup(
      <CancellableProgressBar onCancelOperation={callback}
        processStatus={cancelledStatus} buttonLabel='Cancel TM Merge'
        queryProgress={callback} />
    )).toEqual(ReactDOMServer.renderToStaticMarkup(
      <div>
        <ProgressBar now={0}label={' 0%'} />
        <button disabled type='button' className='btn-danger btn btn-primary'>
          Cancel TM Merge
        </button>
      </div>
    ))
  })

  it('detects loading process cancellation', () => {
    const cancelledStatus1 = {
      url: '/rest/process/key/TMMergeForVerKey-1-ja',
      percentageComplete: 0,
      statusCode: 'Cancelled'
    }
    // Test the isProcessEnded utils function
    expect(isProcessEnded(cancelledStatus1)).toEqual(true)
    const cancelledStatus2 = {
      url: '/rest/process/key/TMMergeForVerKey-1-ja',
      percentageComplete: 66,
      statusCode: 'Cancelled'
    }
    // Test the isProcessEnded utils function
    expect(isProcessEnded(cancelledStatus2)).toEqual(true)
    const cancelledStatus3 = {
      url: '/rest/process/key/TMMergeForVerKey-1-ja',
      percentageComplete: 100,
      statusCode: 'Cancelled'
    }
    // Test the isProcessEnded utils function
    expect(isProcessEnded(cancelledStatus3)).toEqual(true)
  })
})
