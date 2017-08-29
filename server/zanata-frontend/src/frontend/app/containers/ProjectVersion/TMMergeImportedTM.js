import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {Panel, Radio, OverlayTrigger, Button,
  Tooltip, Col} from 'react-bootstrap'
import {IGNORE_CHECK, FUZZY, REJECT} from '../../utils/EnumValueUtils'
import {CopyLabel} from './TMMergeOptionsCommon'
import Icon from '../../components/Icon'
import Toggle from 'react-toggle'

const tooltip = (
  <Tooltip id='copy-as-translated-TM' title='Copy as translated - TM'>
    Less than 100% match still copies as fuzzy.
  </Tooltip>)

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
      <Col xs={12}>
        <Panel>
          <Col xs={12}>
            <div className="vmerge-title vmerge-title-flex">
              <span>
                <Toggle icons={false} defaultChecked
                  onChange={this.toggleChange} />
              </span>
              <span>From </span>
              <span className="panel-name">TM Source</span>
            </div>
          </Col>
          <Col xs={12} md={8}>
            No project, document or context for TMX
            <Radio checked={fromImportedTM === IGNORE_CHECK} disabled={disabled}
              onChange={onImportedTMChange(IGNORE_CHECK)}> I don't mind at all
              <CopyLabel type={IGNORE_CHECK} value={fromImportedTM} />
              <OverlayTrigger placement='right' overlay={tooltip}>
                <Button bsStyle="link" className="tooltip-btn">
                  <Icon name="info" className="s0 info-icon" />
                </Button>
              </OverlayTrigger>
            </Radio>
            <Radio checked={fromImportedTM === FUZZY} disabled={disabled}
              onChange={onImportedTMChange(FUZZY)}> I will need to review it
              <CopyLabel type={FUZZY} value={fromImportedTM} />
            </Radio>
          </Col>
        </Panel>
      </Col>
    )
  }
}

export default TMMergeImportedTM
