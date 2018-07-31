// @ts-nocheck
/* global jest describe it expect */

import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import * as TestUtils from 'react-dom/test-utils'
import {
  IntlProvider
} from 'react-intl'
import { EditorSearchInput } from '.'
import Collapse from 'antd/lib/collapse'
import 'antd/lib/collapse/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
const Panel = Collapse.Panel

const callback = () => {}

/* Use to simulate intl prop from react-intl injectIntl,
 * which wraps the EditorSearchInput component */
const intlProvider = new IntlProvider({ locale: 'en' }, {})
const { intl } = intlProvider.getChildContext()

describe('EditorSearchInputTest', () => {
  it('renders input markup with show advanced but not focused', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <EditorSearchInput
        intl={intl}
        showAdvanced
        search={{
          searchString: 'it was the worst of',
          resId: 'para-0001',
          lastModifiedByUser: 'cdickens',
          changedBefore: '1859-12-31',
          changedAfter: '1859-01-01',
          sourceComment: 'England and France',
          transComment: 'blurst of times?! You stupid monkey!',
          msgContext: 'chapter01.txt'
        }}
        updateSearch={callback}
        toggleAdvanced={callback}
      />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      /* eslint-disable max-len */
      <div>
        <div className="EditorInputGroup EditorInputGroup--outlined EditorInputGroup--rounded">
          <input type="search"
            placeholder="Search source and target text"
            maxLength="1000"
            value="it was the worst of"
            onChange={callback}
            className="EditorInputGroup-input u-sizeLineHeight-1_1-4" />
          <span className="EditorInputGroup-addon btn-xs btn-link n1"
            >Hide advanced</span>
        </div>
        <Collapse activeKey={'1'} onChange={callback}
          style={{
            zIndex: '1000',
            position: 'absolute',
            marginBottom: '0.5rem',
            width: '100%'
          }}>
          <Panel key='1' header={undefined} showArrow={false}>
            <span>
              <div title="exact Resource ID for a string"
                className="u-sPB-1-2">
                <label className="u-textSecondary u-sPB-1-4">Resource ID</label>
                <input type="text"
                  onChange={callback}
                  placeholder="exact Resource ID for a string"
                  className="u-bgHighest u-sizeFull u-inputFlat u-sP-1-2 u-rounded u-sMH-1-4 u-sMV-1-8"
                  value="para-0001" />
              </div>
              <div title="username"
                className="u-sPB-1-2">
                <label className="u-textSecondary u-sPB-1-4">
                  Last modified by
                </label>
                <input type="text"
                  onChange={callback}
                  placeholder="username"
                  className="u-bgHighest u-sizeFull u-inputFlat u-sP-1-2 u-rounded u-sMH-1-4 u-sMV-1-8"
                  value="cdickens" />
              </div>
              <div title="date in format yyyy/mm/dd"
                className="u-sPB-1-2">
                <label className="u-textSecondary u-sPB-1-4">
                  Last modified before
                </label>
                <input type="text"
                  onChange={callback}
                  placeholder="date in format yyyy/mm/dd"
                  className="u-bgHighest u-sizeFull u-inputFlat u-sP-1-2 u-rounded u-sMH-1-4 u-sMV-1-8"
                  value="1859-12-31" />
              </div>
              <div title="date in format yyyy/mm/dd"
                className="u-sPB-1-2">
                <label className="u-textSecondary u-sPB-1-4">
                  Last modified after
                </label>
                <input type="text"
                  onChange={callback}
                  placeholder="date in format yyyy/mm/dd"
                  className="u-bgHighest u-sizeFull u-inputFlat u-sP-1-2 u-rounded u-sMH-1-4 u-sMV-1-8"
                  value="1859-01-01" />
              </div>
              <div title="source comment text"
                className="u-sPB-1-2">
                <label className="u-textSecondary u-sPB-1-4">Source comment</label>
                <input type="text"
                  onChange={callback}
                  placeholder="source comment text"
                  className="u-bgHighest u-sizeFull u-inputFlat u-sP-1-2 u-rounded u-sMH-1-4 u-sMV-1-8"
                  value="England and France" />
              </div>
              <div title="exact Message Context for a string"
                className="u-sPB-1-2">
                <label className="u-textSecondary u-sPB-1-4">
                  msgctxt (gettext)
                </label>
                <input type="text"
                  onChange={callback}
                  placeholder="exact Message Context for a string"
                  className="u-bgHighest u-sizeFull u-inputFlat u-sP-1-2 u-rounded u-sMH-1-4 u-sMV-1-8"
                  value="chapter01.txt" />
              </div>
              <Button size={'small'} aria-label='button' className="AdvSearch-clear"
                onClick={callback}>
                Clear all
              </Button>
            </span>
          </Panel>
        </Collapse>
      </div>
      /* eslint-enable max-len */
    )
// When we uncomment transComment in EditorSearchInput/index.js, this
// should go after the diff for "source comment text" above:
/*
              <div title="translation comment text"
                className="u-sPB-1-2">
                <label className="u-textSecondary u-sPB-1-4">
                  Translation comment
                </label>
                <input type="text"
                  onChange={callback}
                  placeholder="translation comment text"
                  className="u-bgHighest u-sizeFull u-inputFlat u-sP-1-2 u-rounded u-sMH-1-4 u-sMV-1-8"
                  value="blurst of times?! You stupid monkey!" />
              </div>
*/

    expect(actual).toEqual(expected)
  })

  it('Clears search text when X is clicked', () => {
    let updateSearchPayload
    const updateSearch = (payload) => {
      updateSearchPayload = payload
    }

    const inputWithText = TestUtils.renderIntoDocument(
      <EditorSearchInput
        intl={intl}
        showAdvanced
        search={{
          searchString: 'it was the worst of',
          resId: 'para-0001',
          lastModifiedByUser: 'cdickens',
          changedBefore: '1859-12-31',
          changedAfter: '1859-01-01',
          sourceComment: 'England and France',
          transComment: 'blurst of times?! You stupid monkey!',
          msgContext: 'chapter01.txt'
        }}
        updateSearch={updateSearch}
        toggleAdvanced={callback}
      />
    )

    const [ textInput, resourceIdInput, lastModifiedByInput ] =
        TestUtils.scryRenderedDOMComponentsWithTag(inputWithText, 'input')
    const [ clearAdvancedButton ] =
        TestUtils.scryRenderedDOMComponentsWithTag(inputWithText, 'button')
    const [ advancedSearchToggle ] =
        TestUtils.scryRenderedDOMComponentsWithClass(inputWithText,
            'EditorInputGroup-addon btn-xs btn-link')

    TestUtils.Simulate.focus(textInput)
    textInput.value = textInput.value + ' times'
    TestUtils.Simulate.change(textInput)
    expect(updateSearchPayload).toEqual(
      { searchString: 'it was the worst of times' },
        'Changing the main text input should call the search update event')
    // Note: cannot simulate event.currentTarget properly so this will always
    //       miss one line in coverage.
    TestUtils.Simulate.blur(textInput)

    TestUtils.Simulate.click(advancedSearchToggle)

    resourceIdInput.value = resourceIdInput.value + '-1'
    TestUtils.Simulate.change(resourceIdInput)
    expect(updateSearchPayload).toEqual({ resId: 'para-0001-1' },
        'Changing advanced search fields should update the appropriate field')

    lastModifiedByInput.value = 'damason'
    TestUtils.Simulate.change(lastModifiedByInput)
    expect(updateSearchPayload).toEqual({ lastModifiedByUser: 'damason' },
        'Changing advanced search fields should update the appropriate field')

    TestUtils.Simulate.click(clearAdvancedButton)
    expect(updateSearchPayload).toEqual({
      resId: '',
      lastModifiedByUser: '',
      changedBefore: '',
      changedAfter: '',
      sourceComment: '',
      transComment: '',
      msgContext: ''
    }, 'Clear all should clear all fields except text')
  })
})
