import * as React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import PriorityDropdown from './PriorityDropdown'
import { Icon } from '../../../components'
import Dropdown from '../../components/Dropdown'
import {
  MINOR, MAJOR, CRITICAL, textState
} from '../../utils/reject-trans-util'

const defaultClick = () => {}
/* global describe it expect */
describe('PriorityDropdown', () => {
  it('renders minor priority selected markup', () => {
    const minorStyle = textState(MINOR)
    const actual = ReactDOMServer.renderToStaticMarkup(
      <PriorityDropdown
        textState={minorStyle}
        priority={MINOR}
        priorityChange={defaultClick}
      />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <span className='PriorityDropdown'>
        <Icon name='warning' className='s2'
          parentClassName='u-textWarning' />
        <span id='PriorityTitle'>Priority</span>
        <Dropdown enabled isOpen={false}
          onToggle={defaultClick}
          className='dropdown-menu priority'>
          <Dropdown.Button>
            <a className='EditorDropdown-item'>
              <span className={minorStyle}>{MINOR}</span>
              <span className='arrow'>
                <Icon className='n1' name='chevron-down' />
              </span>
            </a>
          </Dropdown.Button>
          <Dropdown.Content>
            <ul>
              <li className='EditorDropdown-item'
                onClick={defaultClick}>
                <span>Minor</span></li>
              <li className='EditorDropdown-item'
                onClick={defaultClick}>
                <span className='u-textWarning'>Major</span></li>
              <li className='EditorDropdown-item'
                onClick={defaultClick}>
                <span className='u-textDanger'>Critical</span></li>
            </ul>
          </Dropdown.Content>
        </Dropdown>
      </span>
    )
    expect(actual).toEqual(expected)
  })
  it('renders major priority selected markup', () => {
    const majorStyle = textState(MAJOR)
    const actual = ReactDOMServer.renderToStaticMarkup(
      <PriorityDropdown
        textState={majorStyle}
        priority={MAJOR}
        priorityChange={defaultClick}
      />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <span className='PriorityDropdown'>
        <Icon name='warning' className='s2'
          parentClassName='u-textWarning' />
        <span id='PriorityTitle'>Priority</span>
        <Dropdown enabled isOpen={false}
          onToggle={defaultClick}
          className='dropdown-menu priority'>
          <Dropdown.Button>
            <a className='EditorDropdown-item'>
              <span className={majorStyle}>{MAJOR}</span>
              <span className='arrow'>
                <Icon className='n1' name='chevron-down' />
              </span>
            </a>
          </Dropdown.Button>
          <Dropdown.Content>
            <ul>
              <li className='EditorDropdown-item'
                onClick={defaultClick}>
                <span>Minor</span></li>
              <li className='EditorDropdown-item'
                onClick={defaultClick}>
                <span className='u-textWarning'>Major</span></li>
              <li className='EditorDropdown-item'
                onClick={defaultClick}>
                <span className='u-textDanger'>Critical</span></li>
            </ul>
          </Dropdown.Content>
        </Dropdown>
      </span>
    )
    expect(actual).toEqual(expected)
  })
  it('renders critical priority selected markup', () => {
    const criticalStyle = textState(CRITICAL)
    const actual = ReactDOMServer.renderToStaticMarkup(
      <PriorityDropdown
        textState={criticalStyle}
        priority={CRITICAL}
        priorityChange={defaultClick}
      />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <span className='PriorityDropdown'>
        <Icon name='warning' className='s2'
          parentClassName='u-textWarning' />
        <span id='PriorityTitle'>Priority</span>
        <Dropdown enabled isOpen={false}
          onToggle={defaultClick}
          className='dropdown-menu priority'>
          <Dropdown.Button>
            <a className='EditorDropdown-item'>
              <span className={criticalStyle}>{CRITICAL}</span>
              <span className='arrow'>
                <Icon className='n1' name='chevron-down' />
              </span>
            </a>
          </Dropdown.Button>
          <Dropdown.Content>
            <ul>
              <li className='EditorDropdown-item'
                onClick={defaultClick}>
                <span>Minor</span></li>
              <li className='EditorDropdown-item'
                onClick={defaultClick}>
                <span className='u-textWarning'>Major</span></li>
              <li className='EditorDropdown-item'
                onClick={defaultClick}>
                <span className='u-textDanger'>Critical</span></li>
            </ul>
          </Dropdown.Content>
        </Dropdown>
      </span>
    )
    expect(actual).toEqual(expected)
  })
  // Dropdown events tested in Editor Dropdown.test.js
})
