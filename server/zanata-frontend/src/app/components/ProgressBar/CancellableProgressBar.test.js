/* global jest describe it expect */

import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import CancellableProgressBar from './CancellableProgressBar'
import Progress from 'antd/lib/progress'
import { isProcessEnded } from '../../utils/EnumValueUtils'
import { ProcessStatus } from '../../utils/prop-types-util';

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
        <Progress percent={0} showInfo />
        <button type='button' className='ant-btn btn-danger ant-btn-danger'>
          <span>Cancel TM Merge</span>
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
    // Test the CancellableProgressBar markup cancel button is disabled
    expect(ReactDOMServer.renderToStaticMarkup(
      <CancellableProgressBar onCancelOperation={callback}
        processStatus={cancelledStatus} buttonLabel='Cancel TM Merge'
        queryProgress={callback} />
    )).toEqual(ReactDOMServer.renderToStaticMarkup(
      <div>
        <Progress percent={0} showInfo />
        <button disabled type='button' className='ant-btn btn-danger ant-btn-danger'>
          <span>Cancel TM Merge</span>
        </button>
      </div>
    ))
  })

  it('detects loading process cancellation', () => {
    // Testing the isProcessEnded utils function
    /** @type {ProcessStatus} */
    const cancelledStatus1 = {
      url: '/rest/process/key/TMMergeForVerKey-1-ja',
      percentageComplete: 0,
      statusCode: 'Cancelled'
    }
    expect(isProcessEnded(cancelledStatus1)).toEqual(true)
    /** @type {ProcessStatus} */
    const cancelledStatus2 = {
      url: '/rest/process/key/TMMergeForVerKey-1-ja',
      percentageComplete: 66,
      statusCode: 'Cancelled'
    }
    expect(isProcessEnded(cancelledStatus2)).toEqual(true)
    /** @type {ProcessStatus} */
    const cancelledStatus3 = {
      url: '/rest/process/key/TMMergeForVerKey-1-ja',
      percentageComplete: 100,
      statusCode: 'Cancelled'
    }
    expect(isProcessEnded(cancelledStatus3)).toEqual(true)
    /** @type {ProcessStatus} */
    const notCancelled = {
      url: '/rest/process/key/TMMergeForVerKey-1-ja',
      // This should not affect the status code logic
      percentageComplete: 9999,
      statusCode: 'Running'
    }
    expect(isProcessEnded(notCancelled)).toEqual(false)
  })
})
