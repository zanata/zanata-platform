/* global jest describe it expect */
jest.disableAutomock()

import React from 'react'
import ReactDOM from 'react-dom'
import ReactDOMServer from 'react-dom/server'
import RejectTranslationModal from '.'

describe('RejectTranslationModal Test', () => {
  it('can render', () => {
    <RejectTranslationModal show isOpen={true}
      criteria="test"
      priority="Critical"  textState="u-textDanger" />
  })
})
