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
          <ul>
            <li className="inline-search-list"
              title="exact Resource ID for a string">
              Resource ID:
              <div className="InputGroup--outlined InputGroup--wide
                InputGroup--rounded">
                <input type="text"
                  onChange={callback}
                  placeholder="exact Resource ID for a string"
                  className="InputGroup-input"
                  value="para-0001" />
              </div>
            </li>
            <li className="inline-search-list" title="username">
              Last modified by:
              <div className="InputGroup--outlined InputGroup--wide
                InputGroup--rounded">
                <input type="text"
                  onChange={callback}
                  placeholder="username"
                  className="InputGroup-input"
                  value="cdickens" />
              </div>
            </li>
            <li className="inline-search-list"
              title="date in format yyyy/mm/dd">
              Last modified before:
              <div className="InputGroup--outlined InputGroup--wide
                InputGroup--rounded">
                <input type="text"
                  onChange={callback}
                  placeholder="date in format yyyy/mm/dd"
                  className="InputGroup-input"
                  value="1859-12-31" />
              </div>
            </li>
            <li className="inline-search-list"
              title="date in format yyyy/mm/dd">
              Last modified after:
              <div className="InputGroup--outlined InputGroup--wide
                InputGroup--rounded">
                <input type="text"
                  onChange={callback}
                  placeholder="date in format yyyy/mm/dd"
                  className="InputGroup-input"
                  value="1859-01-01" />
              </div>
            </li>
            <li className="inline-search-list" title="source comment text">
              Source comment:
              <div className="InputGroup--outlined InputGroup--wide
                InputGroup--rounded">
                <input type="text"
                  onChange={callback}
                  placeholder="source comment text"
                  className="InputGroup-input"
                  value="England and France" />
              </div>
            </li>
            <li className="inline-search-list" title="translation comment text">
              Translation comment:
              <div className="InputGroup--outlined InputGroup--wide
                InputGroup--rounded">
                <input type="text"
                  onChange={callback}
                  placeholder="translation comment text"
                  className="InputGroup-input"
                  value="blurst of times?! You stupid monkey!" />
              </div>
            </li>
            <li className="inline-search-list"
              title="exact Message Context for a string">
              msgctxt (gettext):
              <div className="InputGroup--outlined InputGroup--wide
                InputGroup--rounded">
                <input type="text"
                  onChange={callback}
                  placeholder="exact Message Context for a string"
                  className="InputGroup-input"
                  value="chapter01.txt" />
              </div>
            </li>
          </ul>
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
