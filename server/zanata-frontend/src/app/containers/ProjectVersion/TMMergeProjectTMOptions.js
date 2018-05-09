// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import {
  Col, Radio, OverlayTrigger, Tooltip
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

const MetaDataCheckOption = ({name, value, callback, hasReject, disabled}) => {
  const reject = hasReject && (
    <Radio checked={value === REJECT} validationState="error"
      onChange={callback(REJECT)} disabled={disabled}>
      <span>I don't want it</span><br />
      <CopyLabel type={REJECT} value={value} />
    </Radio>
  )
  return <Col>
    If the translation is from a different&nbsp;
    <span className="import-type">{name}</span>
    <Radio checked={value === IGNORE_CHECK} validationState='success'
      onChange={callback(IGNORE_CHECK)} disabled={disabled}>
      <span>I don't mind at all</span><br />
      <CopyLabel type={IGNORE_CHECK} value={value} />
    </Radio>
    <Radio checked={value === FUZZY} onChange={callback(FUZZY)}
      validationState='warning' disabled={disabled}>
      <span>I will need to review it</span><br />
      <CopyLabel type={FUZZY} value={value} />
      <OverlayTrigger placement='right' overlay={copyAsFuzzyTooltip}>
        <a className="btn-link tooltip-btn iconInfoVersionMerge" role="button">
          <Icon name="info" className="s0" />
        </a>
      </OverlayTrigger>
    </Radio>
    {reject}
  </Col>
}
MetaDataCheckOption.propTypes = {
  name: PropTypes.string.isRequired,
  value: PropTypes.oneOf([IGNORE_CHECK, FUZZY, REJECT]).isRequired,
  callback: PropTypes.func.isRequired,
  hasReject: PropTypes.bool,
  disabled: PropTypes.bool.isRequired
}

class TMMergeProjectTMOptions extends Component {
  static propTypes = {
    ...TMMergeOptionsValuePropType,
    ...TMMergeOptionsCallbackPropType,
    disableDifferentProjectOption: PropTypes.bool.isRequired,
    disabled: PropTypes.bool.isRequired
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
      differentContext,
      disableDifferentProjectOption,
      disabled
    } = this.props
    const diffProjectOption = disableDifferentProjectOption
      ? (<Col>
        If the translation is from a different&nbsp;
        <span className="import-type">project</span>
        <Radio checked validationState="error" disabled>
          <span>I don't want it</span><br />
          <CopyLabel type={REJECT} value={REJECT} />
        </Radio>
      </Col>)
      : <MetaDataCheckOption name="project" disabled={disabled}
        value={differentProject} callback={this.onDifferentProjectChange} />
    return (
      <Col>
        {diffProjectOption}
        <MetaDataCheckOption name="document" value={differentDocId}
          disabled={disabled}
          callback={this.onDifferentDocIdChange} hasReject />
        <MetaDataCheckOption name="context" value={differentContext}
          disabled={disabled}
          callback={this.onDifferentContextChange} hasReject />
      </Col>
    )
  }
}
export default TMMergeProjectTMOptions
