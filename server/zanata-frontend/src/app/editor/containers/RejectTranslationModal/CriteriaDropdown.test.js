import * as React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import CriteriaDropdown from './CriteriaDropdown'
import { MINOR, MAJOR, CRITICAL } from '../../utils/reject-trans-util'
import { Icon } from '../../../components'
import Dropdown from '../../components/Dropdown'
import { UNSPECIFIED } from './index'

const defaultClick = () => {}
const criteriaList = [{
  editable: false,
  description: 'One',
  priority: MINOR
}, {
  editable: false,
  description: 'Two',
  priority: MAJOR
}, {
  editable: false,
  description: 'Three',
  priority: CRITICAL
}]
const options = criteriaList.map((value, index) => {
  return (
    <li key={index + 1}
      className='EditorDropdown-item'
      onClick={defaultClick}>
      {value.description}
    </li>
  )
})

// FIXME: should not be modifying the options array
options.unshift(
  <li key={0}
    className='EditorDropdown-item'
    onClick={defaultClick}>
    {UNSPECIFIED}
  </li>
)
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
      <Dropdown enabled isOpen={false}
        onToggle={defaultClick}
        className='dropdown-menu Criteria'>
        <Dropdown.Button>
          <a className='EditorDropdown-item'>
            {'One'}
            <Icon className='n1 u-pullRight' name='chevron-down' />
          </a>
        </Dropdown.Button>
        <Dropdown.Content>
          <ul>
            {options}
          </ul>
        </Dropdown.Content>
      </Dropdown>
    )
    expect(actual).toEqual(expected)
  })
  // Dropdown events tested in Editor Dropdown.test.js
})
