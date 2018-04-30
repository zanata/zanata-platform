import React from 'react'
import { Component } from 'react'
import { Icon } from '../../../components'
import * as PropTypes from 'prop-types'
import { MINOR, MAJOR, CRITICAL } from '../../utils/reject-trans-util'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/index.less'
import 'antd/lib/icon/style/'

/**
 * A Local Editor Dropdown coponent that selects the Priority of a
 * translation rejection message
 */
class PriorityDropdown extends Component {
  static propTypes = {
    textState: PropTypes.string.isRequired,
    priority: PropTypes.oneOf(
      [
        MINOR,
        MAJOR,
        CRITICAL
      ]
    ).isRequired,
    priorityChange: PropTypes.func.isRequired
  }
  render () {
    const { textState, priority, priorityChange } = this.props
    console.log(priority)
    const options = [
      <Select.Option key={0} value={MINOR}>
        {MINOR}
      </Select.Option>,
      <Select.Option key={1} value={MAJOR} className='u-textWarning'>
        {MAJOR}
      </Select.Option>,
      <Select.Option key={2} value={CRITICAL} className='u-textDanger'>
        {CRITICAL}
      </Select.Option>
    ]
    return (
      <span className='PriorityDropdown'>
        <Icon name='warning' className='s2'
          parentClassName='u-textWarning' />
        <span id='PriorityTitle'>Priority</span>
        <Select
          className={textState}
          defaultValue={priority}
          style={{ width: '100%' }}
          onChange={priorityChange}>
          {options}
        </Select>
      </span>
    )
  }
}
export default PriorityDropdown
