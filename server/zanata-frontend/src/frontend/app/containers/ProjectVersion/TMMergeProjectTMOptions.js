import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {
  Col, Panel, ListGroup, ToggleButtonGroup, ToggleButton
} from 'react-bootstrap'

import {TMMergeOptionsValuePropType,
  TMMergeOptionsCallbackPropType, CopyLabel} from './TMMergeOptionsCommon'
import {IGNORE_CHECK, FUZZY, REJECT} from '../../utils/EnumValueUtils'

const MetaDataCheckOption = ({name, value, callback, hasReject}) => {
  const reject = hasReject && (
    <ToggleButton value={REJECT}> I don't want it
      <CopyLabel type={REJECT} value={value} />
    </ToggleButton>
  )
  return <ListGroup fill>
    If the translation is from a different
    <span className="text-bold"> {name}</span>
    <ToggleButtonGroup
      type="radio" name="radio"
      value={value}
      onChange={callback}>
      <ToggleButton value={IGNORE_CHECK}> I don't mind at all
        <CopyLabel type={IGNORE_CHECK} value={value} />
      </ToggleButton>
      <ToggleButton value={FUZZY}> I will need to review it
        <CopyLabel type={FUZZY} value={value} />
      </ToggleButton>
      {reject}
    </ToggleButtonGroup>
  </ListGroup>
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
  onDifferentProjectChange = (value) => {
    this.props.onDifferentProjectChange(value)
  }
  onDifferentDocIdChange = (value) => {
    this.props.onDifferentDocIdChange(value)
  }
  onDifferentContextChange = (value) => {
    this.props.onDifferentContextChange(value)
  }
  render () {
    const {
      differentProject,
      differentDocId,
      differentContext
    } = this.props
    return (
      <Col xs={12}>
        <Panel className='tm-panel'>
          <MetaDataCheckOption name="project"
            value={differentProject} callback={this.onDifferentProjectChange} />
          <MetaDataCheckOption name="document" value={differentDocId}
            callback={this.onDifferentDocIdChange} hasReject />
          <MetaDataCheckOption name="context" value={differentContext}
            callback={this.onDifferentContextChange} hasReject />
        </Panel>
      </Col>
    )
  }
}
export default TMMergeProjectTMOptions
