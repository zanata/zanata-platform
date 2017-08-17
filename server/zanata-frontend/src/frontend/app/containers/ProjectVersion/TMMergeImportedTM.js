import React from 'react'
import PropTypes from 'prop-types'
import {Panel,
  ToggleButtonGroup, ToggleButton, Col} from 'react-bootstrap'
import {IGNORE_CHECK, FUZZY, REJECT} from '../../utils/EnumValueUtils'
import {CopyLabel} from './TMMergeOptionsCommon'

const TMMergeImportedTM = ({fromImportedTM, onImportedTMChange}) => {
  return (
    <Col xs={12}>
      <Panel>
        <div className="vmerge-title">
          From<span className="text-info"> Imported TM</span>
        </div>
        No project, document or context for TMX
        <ToggleButtonGroup
          type="radio" name="radio"
          value={fromImportedTM}
          onChange={onImportedTMChange}>
          <ToggleButton value={IGNORE_CHECK}> I don't mind at all
            <CopyLabel type={IGNORE_CHECK} value={fromImportedTM} />
          </ToggleButton>
          <ToggleButton value={FUZZY}> I will need to review it
            <CopyLabel type={FUZZY} value={fromImportedTM} />
          </ToggleButton>
        </ToggleButtonGroup>
      </Panel>
    </Col>
  )
}

TMMergeImportedTM.propTypes = {
  fromImportedTM: PropTypes.oneOf([IGNORE_CHECK, FUZZY, REJECT]).isRequired,
  onImportedTMChange: PropTypes.func.isRequired
}

export default TMMergeImportedTM
