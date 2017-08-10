/* global jest describe it expect */
jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import TestUtils from 'react-dom/test-utils'
import { EditorSearchInput } from '.'
import { Icon } from '../../../components'
import IconButton from '../IconButton'
import { Panel, Button } from 'react-bootstrap'

const callback = () => {}

describe('EditorSearchInputTest', () => {
  it('renders input markup with show advanced but not focused', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <EditorSearchInput
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
      <div>
        <div className="InputGroup InputGroup--outlined InputGroup--rounded">
          <span className="InputGroup-addon">
            <Icon name="search" title="Search" className="n1" />
          </span>
          <input type="search"
            placeholder="Search source and target text"
            maxLength="1000"
            value="it was the worst of"
            onChange={callback}
            className="InputGroup-input u-sizeLineHeight-1_1-4" />
          <span className="InputGroup-addon">
            <IconButton icon="cross"
              title="Clear search"
              iconSize="n1"
              onClick={callback} />
          </span>
          <span className="InputGroup-addon btn-xs advsearch btn-link"
            >Hide advanced</span>
        </div>
        <Panel collapsible expanded>
          <table>
            <tbody>
              <tr className="u-sMH-3-4 u-sMV-1-8 .u-sizeFull"
                title="exact Resource ID for a string">
                <td className="u-sPR-1-4">Resource ID:</td>
                <td className="u-sizeWidthFull">
                  <input type="text"
                    onChange={callback}
                    placeholder="exact Resource ID for a string"
                    className="u-bgHighest u-inputFlat u-sP-1-2 u-sMV-1-4
                      u-sizeFull"
                    value="para-0001" />
                </td>
              </tr>
              <tr className="u-sMH-3-4 u-sMV-1-8 .u-sizeFull"
                title="username">
                <td className="u-sPR-1-4">Last modified by:</td>
                <td className="u-sizeWidthFull">
                  <input type="text"
                    onChange={callback}
                    placeholder="username"
                    className="u-bgHighest u-inputFlat u-sP-1-2 u-sMV-1-4
                      u-sizeFull"
                    value="cdickens" />
                </td>
              </tr>
              <tr className="u-sMH-3-4 u-sMV-1-8 .u-sizeFull"
                title="date in format yyyy/mm/dd">
                <td className="u-sPR-1-4">Last modified before:</td>
                <td className="u-sizeWidthFull">
                  <input type="text"
                    onChange={callback}
                    placeholder="date in format yyyy/mm/dd"
                    className="u-bgHighest u-inputFlat u-sP-1-2 u-sMV-1-4
                      u-sizeFull"
                    value="1859-12-31" />
                </td>
              </tr>
              <tr className="u-sMH-3-4 u-sMV-1-8 .u-sizeFull"
                title="date in format yyyy/mm/dd">
                <td className="u-sPR-1-4">Last modified after:</td>
                <td className="u-sizeWidthFull">
                  <input type="text"
                    onChange={callback}
                    placeholder="date in format yyyy/mm/dd"
                    className="u-bgHighest u-inputFlat u-sP-1-2 u-sMV-1-4
                      u-sizeFull"
                    value="1859-01-01" />
                </td>
              </tr>
              <tr className="u-sMH-3-4 u-sMV-1-8 .u-sizeFull"
                title="source comment text">
                <td className="u-sPR-1-4">Source comment:</td>
                <td className="u-sizeWidthFull">
                  <input type="text"
                    onChange={callback}
                    placeholder="source comment text"
                    className="u-bgHighest u-inputFlat u-sP-1-2 u-sMV-1-4
                      u-sizeFull"
                    value="England and France" />
                </td>
              </tr>
              <tr className="u-sMH-3-4 u-sMV-1-8 .u-sizeFull"
                title="translation comment text">
                <td className="u-sPR-1-4">Translation comment:</td>
                <td className="u-sizeWidthFull">
                  <input type="text"
                    onChange={callback}
                    placeholder="translation comment text"
                    className="u-bgHighest u-inputFlat u-sP-1-2 u-sMV-1-4
                      u-sizeFull"
                    value="blurst of times?! You stupid monkey!" />
                </td>
              </tr>
              <tr className="u-sMH-3-4 u-sMV-1-8 .u-sizeFull"
                title="exact Message Context for a string">
                <td className="u-sPR-1-4">msgctxt (gettext):</td>
                <td className="u-sizeWidthFull">
                  <input type="text"
                    onChange={callback}
                    placeholder="exact Message Context for a string"
                    className="u-bgHighest u-inputFlat u-sP-1-2 u-sMV-1-4
                      u-sizeFull"
                    value="chapter01.txt" />
                </td>
              </tr>
            </tbody>
          </table>
          <Button bsStyle="link" bsSize="xsmall" className="clearadvsearch"
            onClick={callback}>
            Clear all
          </Button>
        </Panel>
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('Clears search text when X is clicked', () => {
    let updateSearchPayload
    const updateSearch = (payload) => {
      updateSearchPayload = payload
    }

    const inputWithText = TestUtils.renderIntoDocument(
      <EditorSearchInput
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
    const [ closeButton, clearAdvancedButton ] =
        TestUtils.scryRenderedDOMComponentsWithTag(inputWithText, 'button')
    const [ advancedSearchToggle ] =
        TestUtils.scryRenderedDOMComponentsWithClass(inputWithText,
            'InputGroup-addon btn-xs advsearch btn-link')

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

    TestUtils.Simulate.click(closeButton)
    expect(updateSearchPayload).toEqual(
      { searchString: '' },
      'Close button click should clear text')

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
