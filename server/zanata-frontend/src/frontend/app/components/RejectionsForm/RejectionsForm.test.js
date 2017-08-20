/* global jest describe it expect */
jest.disableAutomock()

import React from 'react'
import PropTypes from 'react-prop-types'
import ReactDOM from 'react-dom'
import ReactDOMServer from 'react-dom/server'
import RejectionsForm from '.'

describe('RejectionsForm Test', () => {
  it('read-only', () => {
    <RejectionsForm
        editable={false}
        criteriaPlaceholder='Criteria here'
        priority='Minor' textState='text-info'/>
  })
  it('editable', () => {
    <RejectionsForm
        className='active'
        editable={true}
        criteriaPlaceholder='Criteria here'
        priority='Minor' textState='text-info'/>
  })
  it('major', () => {
    <RejectionsForm
        editable={false}
        criteriaPlaceholder='Criteria here'
        priority='Major' textState='text-warning'/>
  })
  it('critical', () => {
    <RejectionsForm
        editable={false}
        criteriaPlaceholder='Criteria here'
        priority='Critical' textState='text-danger'/>
  })
})
