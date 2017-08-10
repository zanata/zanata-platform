import React from 'react'
import PropTypes from 'prop-types'
import {
  Col, Panel, Checkbox, ListGroup, ListGroupItem, Label
} from 'react-bootstrap'

import {TMMergeOptionsValuePropType, TMMergeOptionsCallbackPropType}
  from './TMMergeOptionsCommon'

const CopyLabel = (props) => {
  return props.copy
      ? (<Label bsStyle='warning'>
        Copy as Fuzzy
      </Label>)
      : (<Label bsStyle='danger'>
        Don't Copy
      </Label>)
}
CopyLabel.propTypes = {
  copy: PropTypes.bool.isRequired
}

export const ProjectVersionOptions = (props) => {
  const {
    differentDocId,
    differentContext,
    fromImportedTM,
    onDocIdCheckboxChange,
    onContextCheckboxChange,
    onImportedCheckboxChange,
    ignoreDifferentContext,
    ignoreDifferentDocId,
    importedTMCopyAsTranslated,
    onIgnoreDifferentDocIdChange,
    onIgnoreDifferentContextChange,
    onImportedTMCopyRuleChange
  } = props
  return (
    <Col xs={12}>
      <Panel className='tm-panel'>
        <ListGroup fill>
          <ListGroupItem>
            <Checkbox checked={ignoreDifferentDocId}
              onChange={onIgnoreDifferentDocIdChange}>
              Ignore this check
            </Checkbox>
            <Checkbox onChange={onDocIdCheckboxChange}
              checked={differentDocId}>
              Different DocID
              <small> Document name and path</small>
              <CopyLabel copy={differentDocId} />
            </Checkbox>
          </ListGroupItem>
          <ListGroupItem>
            <Checkbox checked={ignoreDifferentContext}
              onChange={onIgnoreDifferentContextChange}>
              Ignore this check
            </Checkbox>
            <Checkbox onChange={onContextCheckboxChange}
              checked={differentContext}>
              Different Context
              <small> resId, msgctxt</small>
              <CopyLabel copy={differentContext} />
            </Checkbox>
          </ListGroupItem>
        </ListGroup>
        <span />
        <ListGroup fill>
          <ListGroupItem >
            <Checkbox checked={importedTMCopyAsTranslated}
              onChange={onImportedTMCopyRuleChange}>
              Ignore this check
            </Checkbox>
            <Checkbox onChange={onImportedCheckboxChange}
              checked={fromImportedTM}>
              Match from Imported TM
              <CopyLabel copy={fromImportedTM} />
            </Checkbox>
          </ListGroupItem>
        </ListGroup>
      </Panel>
    </Col>
  )
}
ProjectVersionOptions.propTypes = {
  ...TMMergeOptionsValuePropType,
  ...TMMergeOptionsCallbackPropType
}
