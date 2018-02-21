import * as React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import CriteriaDropdown from './CriteriaDropdown'
import { MINOR, MAJOR, CRITICAL } from '../../utils/reject-trans-util'
import { Icon } from '../../../components'
import Dropdown from '../../components/Dropdown'

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
