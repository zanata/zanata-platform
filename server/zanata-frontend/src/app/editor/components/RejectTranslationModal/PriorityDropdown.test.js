import * as React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import PriorityDropdown from './PriorityDropdown'
import { Icon } from '../../../components'
import {
  MINOR, MAJOR, CRITICAL, textState
} from '../../utils/reject-trans-util'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'

const defaultClick = () => {}
const options = [
  <Select.Option key={1}>
    <span>Minor</span>
  </Select.Option>,
  <Select.Option key={2}>
    <span className='txt-warn'>Major</span>
  </Select.Option>,
  <Select.Option key={3}>
    <span className='txt-error'>Critical</span>
  </Select.Option>
]
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
        <Select
          defaultValue={MINOR}
          style={{ width: 'auto' }}
          onChange={defaultClick}>
          {options}
        </Select>
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
          parentClassName='txt-warn' />
        <span id='PriorityTitle'>Priority</span>
        <Select
          className={majorStyle}
          defaultValue={MAJOR}
          style={{ width: 'auto' }}
          onChange={defaultClick}>
          {options}
        </Select>
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
        <Select
          className={criticalStyle}
          defaultValue={CRITICAL}
          style={{ width: 'auto' }}
          onChange={defaultClick}>
          {options}
        </Select>
      </span>
    )
    expect(actual).toEqual(expected)
  })
  // Dropdown events tested in Editor Dropdown.test.js
})
