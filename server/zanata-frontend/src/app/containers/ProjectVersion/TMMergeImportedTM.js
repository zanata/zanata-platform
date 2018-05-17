import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import Radio from 'antd/lib/radio'
import 'antd/lib/radio/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
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
          <Row>
            <Col span={12}>
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
            </Col>
            <Col span={12}>
              <Radio checked={fromImportedTM === FUZZY} disabled={disabled}
                onChange={onImportedTMChange(FUZZY)}>
                <span>I will need to review it</span><br />
                <CopyLabel type={FUZZY} value={fromImportedTM} />
              </Radio>
            </Col>
          </Row>
        </div>
      </span>
    )
  }
}

export default TMMergeImportedTM
