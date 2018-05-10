import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import {Radio, Tooltip} from 'antd'
import {IGNORE_CHECK, FUZZY, REJECT} from '../../utils/EnumValueUtils'
import {CopyLabel} from './TMMergeOptionsCommon'
import Icon from '../../components/Icon'
import Toggle from 'react-toggle'

const tooltip = (
  <span id='copy-as-translated-TM'>
    Less than 100% match still copies as fuzzy.
  </span>)

class TMMergeImportedTM extends Component {
  static propTypes = {
    fromImportedTM: PropTypes.oneOf([IGNORE_CHECK, FUZZY, REJECT]).isRequired,
    onImportedTMChange: PropTypes.func.isRequired
  }
  defaultState = {
    enabled: true
  }
  constructor (props) {
    super(props)
    this.state = this.defaultState
  }
  toggleChange = (e) => {
    const checked = e.target.checked
    this.setState(() => ({
      enabled: checked
    }))
  }
  render () {
    const {fromImportedTM, onImportedTMChange} = this.props
    const disabled = !this.state.enabled
    return (
      <span>
        <div>
          <span>
            <Toggle icons={false} defaultChecked
              onChange={this.toggleChange} />
          </span>
          <span>From </span>
          <span className="panel-name">TM Source</span>
        </div>
        <div>
          No project, document or context for TMX<br />
          <Radio checked={fromImportedTM === IGNORE_CHECK} disabled={disabled}
            onChange={onImportedTMChange(IGNORE_CHECK)}>
            <span>I don't mind at all</span><br />
            <CopyLabel type={IGNORE_CHECK} value={fromImportedTM} />
            <Tooltip placement='right' overlay={tooltip}>
              <a className="btn-link tooltip-btn" role="button">
                <Icon name="info" className="s0"
                  parentClassName="iconInfoVersionMerge" />
              </a>
            </Tooltip>
          </Radio>
          <Radio checked={fromImportedTM === FUZZY} disabled={disabled}
            onChange={onImportedTMChange(FUZZY)}>
            <span>I will need to review it</span><br />
            <CopyLabel type={FUZZY} value={fromImportedTM} />
          </Radio>
        </div>
      </span>
    )
  }
}

export default TMMergeImportedTM
