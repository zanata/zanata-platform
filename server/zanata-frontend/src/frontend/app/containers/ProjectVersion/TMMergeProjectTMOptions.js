import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {
  Col, Radio, OverlayTrigger, Button, Tooltip
} from 'react-bootstrap'
import Icon from '../../components/Icon'
import {TMMergeOptionsValuePropType,
  TMMergeOptionsCallbackPropType, CopyLabel} from './TMMergeOptionsCommon'
import {IGNORE_CHECK, FUZZY, REJECT} from '../../utils/EnumValueUtils'

const copyAsFuzzyTooltip = (
  <Tooltip id='copy-as-fuzzy-project' title='Copy as fuzzy - Project'>
    Can only copy as translated if the context is the same. Otherwise it will
    always use fuzzy.
  </Tooltip>)

const MetaDataCheckOption = ({name, value, callback, hasReject}) => {
  const reject = hasReject && (
    <Radio checked={value === REJECT} validationState="error"
      onChange={callback(REJECT)}> I don't want it
      <CopyLabel type={REJECT} value={value} />
    </Radio>
  )
  return <Col xs={12} md={4}>
    If the translation is from a different <span>{name}</span>
    <Radio checked={value === IGNORE_CHECK} validationState='success'
      onChange={callback(IGNORE_CHECK)}> I don't mind at all
      <CopyLabel type={IGNORE_CHECK} value={value} />
    </Radio>
    <Radio checked={value === FUZZY} onChange={callback(FUZZY)}
      validationState='warning'> I will need to review it
      <CopyLabel type={FUZZY} value={value} />
      <OverlayTrigger placement='right' overlay={copyAsFuzzyTooltip}>
        <Button bsStyle="link" className="tooltip-btn">
          <Icon name="info" className="s0 info-icon" />
        </Button>
      </OverlayTrigger>
    </Radio>
    {reject}
  </Col>
}
MetaDataCheckOption.propTypes = {
  name: PropTypes.string.isRequired,
  value: PropTypes.oneOf([IGNORE_CHECK, FUZZY, REJECT]).isRequired,
  callback: PropTypes.func.isRequired,
  hasReject: PropTypes.bool
}

class TMMergeProjectTMOptions extends Component {
  static propTypes = {
    ...TMMergeOptionsValuePropType,
    ...TMMergeOptionsCallbackPropType
  }
  onDifferentProjectChange = (value) => () => {
    this.props.onDifferentProjectChange(value)
  }
  onDifferentDocIdChange = (value) => () => {
    this.props.onDifferentDocIdChange(value)
  }
  onDifferentContextChange = (value) => () => {
    this.props.onDifferentContextChange(value)
  }
  render () {
    const {
      differentProject,
      differentDocId,
      differentContext
    } = this.props
    return (
      <Col xs={12} className="validations">
        <MetaDataCheckOption name="project"
          value={differentProject} callback={this.onDifferentProjectChange} />
        <MetaDataCheckOption name="document" value={differentDocId}
          callback={this.onDifferentDocIdChange} hasReject />
        <MetaDataCheckOption name="context" value={differentContext}
          callback={this.onDifferentContextChange} hasReject />
      </Col>
    )
  }
}
export default TMMergeProjectTMOptions
