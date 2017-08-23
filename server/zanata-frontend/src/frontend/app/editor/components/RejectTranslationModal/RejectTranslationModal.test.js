/* global jest describe it expect */
jest.disableAutomock()

import React from 'react'
import ReactDOM from 'react-dom'
import ReactDOMServer from 'react-dom/server'
import RejectTranslationModal from '.'

describe('RejectTranslationModal Test', () => {
  it('critical priority', () => {
    <RejectTranslationModal show isOpen={true}
      criteria="test criteria"
      priority="Critical"  textState="u-textDanger" />
  })
  it('major priority', () => {
    <RejectTranslationModal show isOpen={true}
      criteria="test criteria"
      priority="Major"  textState="u-textWarning" />
  })
  it('minor priority', () => {
    <RejectTranslationModal show isOpen={true}
      criteria="test criteria"
      priority="Minor" />
  })
})
