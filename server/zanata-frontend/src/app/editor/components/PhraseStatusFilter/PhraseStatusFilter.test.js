/* global jest describe it expect */
jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import TestUtils from 'react-dom/test-utils'
import { PhraseStatusFilter } from '.'
import FilterToggle from '../FilterToggle'
import { Icon } from '../../../components'
import { Row } from 'react-bootstrap'
import mockGettextCatalog from '../../../../__mocks__/mockAngularGettext'

const callback = () => {}

describe('PhraseStatusFilterTest', () => {
  it('FilterToggle markup', () => {
    const doStuff = () => {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <FilterToggle id="government-issued"
        className="soClassy"
        isChecked
        onChange={doStuff}
        title="titalic"
        count="12"
        withDot />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="Toggle u-round soClassy">
        <input className="Toggle-checkbox"
          type="checkbox"
          id="government-issued"
          checked
          onChange={doStuff} />
        <span className="Toggle-fakeCheckbox" />
        <label className="Toggle-label"
          htmlFor="government-issued"
          title="titalic">
          <Row>
            <Icon name="dot" className="n1" />
            12
            <span className="u-hiddenVisually">titalic</span>
          </Row>
        </label>
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('FilterToggle markup (unchecked)', () => {
    const doStuff = () => {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <FilterToggle id="government-issued"
        className="soClassy"
        isChecked={false}
        onChange={doStuff}
        title="titalic"
        count="17"
        withDot={false} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="Toggle u-round soClassy">
        <input className="Toggle-checkbox"
          type="checkbox"
          id="government-issued"
          checked={false}
          onChange={doStuff} />
        <span className="Toggle-fakeCheckbox" />
        <label className="Toggle-label"
          htmlFor="government-issued"
          title="titalic">
          <Row>
            17
            <span className="u-hiddenVisually">titalic</span>
          </Row>
        </label>
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('PhraseStatusFilter markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <PhraseStatusFilter
        resetFilter={callback}
        onFilterChange={callback}
        filter={{
          all: true,
          approved: false,
          translated: true,
          needswork: false,
          rejected: true,
          untranslated: false
        }}
        counts={{
          total: 1,
          approved: 2,
          translated: 3,
          needswork: 4,
          rejected: 5,
          untranslated: 6
        }}
        gettextCatalog={mockGettextCatalog} />
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <ul className="u-listHorizontal u-sizeHeight-1">
        <li className="u-sm-hidden u-sMV-1-4">
          <FilterToggle
            id="filter-phrases-total"
            className="u-textSecondary"
            isChecked
            title="Total Phrases"
            count={1}
            onChange={callback}
            withDot={false} />
        </li>
        <li className="u-ltemd-hidden u-sMV-1-4">
          <FilterToggle
            id="filter-phrases-approved"
            className="u-textHighlight"
            isChecked={false}
            title="Approved"
            count={2}
            onChange={callback} />
        </li>
        <li className="u-ltemd-hidden u-sMV-1-4">
          <FilterToggle
            id="filter-phrases-translated"
            className="u-textSuccess"
            isChecked
            title="Translated"
            count={3}
            onChange={callback} />
        </li>
        <li className="u-ltemd-hidden u-sMV-1-4">
          <FilterToggle
            id="filter-phrases-needs-work"
            className="u-textUnsure"
            isChecked={false}
            title="Needs Work"
            count={4}
            onChange={callback} />
        </li>
        <li className="u-ltemd-hidden u-sMV-1-4">
          <FilterToggle
            id="filter-phrases-rejected"
            className="u-textWarning"
            isChecked
            title="Rejected"
            count={5}
            onChange={callback} />
        </li>
        <li className="u-ltemd-hidden u-sMV-1-4">
          <FilterToggle
            id="filter-phrases-untranslated"
            className="u-textNeutral"
            isChecked={false}
            title="Untranslated"
            count={6}
            onChange={callback} />
        </li>
      </ul>
    )
    expect(actual).toEqual(expected)
  })

  it('PhraseStatusFilter events', () => {
    let filterReset = false
    const resetFilter = () => {
      filterReset = true
    }

    let filterChangeType = 'none'
    const onFilterChange = statusType => {
      filterChangeType = statusType
    }

    const filterComponent = TestUtils.renderIntoDocument(
      <PhraseStatusFilter
        resetFilter={resetFilter}
        onFilterChange={onFilterChange}
        filter={{
          all: true,
          approved: false,
          translated: true,
          needswork: false,
          rejected: false,
          untranslated: true
        }}
        counts={{
          total: 1,
          approved: 2,
          translated: 3,
          needswork: 4,
          rejected: 5,
          untranslated: 6
        }}
        gettextCatalog={mockGettextCatalog} />
    )
    const [all, approved, translated, needsWork, untranslated] =
      TestUtils.scryRenderedDOMComponentsWithTag(filterComponent, 'input')

    TestUtils.Simulate.change(needsWork, {'target': {'checked': true}})

    expect(filterChangeType).toEqual('needswork',
      'should call filter toggle action with correct type when specific ' +
      'status is changed')

    TestUtils.Simulate.change(all, {'target': {'checked': true}})
    expect(filterReset).toEqual(true,
      'should call given reset function when total/all is changed')
  })
})
