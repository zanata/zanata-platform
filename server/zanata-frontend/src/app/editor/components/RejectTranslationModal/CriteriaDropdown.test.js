import * as React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import CriteriaDropdown from './CriteriaDropdown'
import { MINOR, MAJOR, CRITICAL } from '../../utils/reject-trans-util'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/index.less'

const defaultClick = () => {}
const criteriaList = [{
  commentRequired: false,
  description: 'One',
  priority: MINOR
}, {
  commentRequired: false,
  description: 'Two',
  priority: MAJOR
}, {
  commentRequired: false,
  description: 'Three',
  priority: CRITICAL
}]
const options = criteriaList.map((value, index) => {
  return (
    <li key={index}
      className='EditorDropdown-item'
      onClick={defaultClick}>
      {value.description}
    </li>
  )
})
/* global describe it expect */
describe('CriteriaDropdown', () => {
  it('renders default markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <CriteriaDropdown
        criteriaList={criteriaList}
        onCriteriaChange={defaultClick}
        onUnspecifiedCriteria={defaultClick}
        criteriaDescription='One'
      />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <Select
        defaultValue={'One'}
        style={{ width: '95%' }}
        onChange={defaultClick}>
        {options}
      </Select>
    )
    expect(actual).toEqual(expected)
  })
  // Dropdown events tested in Editor Dropdown.test.js
})
