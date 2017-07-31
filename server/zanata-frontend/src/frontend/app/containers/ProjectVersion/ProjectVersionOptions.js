import React from 'react'
import PropTypes from 'prop-types'
import {
  Col, Panel, Checkbox, ListGroup, ListGroupItem, Label
} from 'react-bootstrap'

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
    below100match
  } = props
  return (
    <Col xs={12}>
      <Panel className='tm-panel'>
        <ListGroup fill>
          <ListGroupItem>
            <Checkbox onChange={onDocIdCheckboxChange}
              checked={differentDocId}>
              Different DocID
              <small> Document name and path</small>
              <CopyLabel copy={differentDocId} />
            </Checkbox>
          </ListGroupItem>
          <ListGroupItem>
            <Checkbox onChange={onContextCheckboxChange}
              checked={differentContext} disabled={below100match}>
              Different Context
              <small> resId, msgctxt</small>
              <CopyLabel copy={differentContext} />
            </Checkbox>
          </ListGroupItem>
        </ListGroup>
        <span />
        <ListGroup fill>
          <ListGroupItem >
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
  differentDocId: PropTypes.bool.isRequired,
  differentContext: PropTypes.bool.isRequired,
  fromImportedTM: PropTypes.bool.isRequired,
  onDocIdCheckboxChange: PropTypes.func.isRequired,
  onContextCheckboxChange: PropTypes.func.isRequired,
  onImportedCheckboxChange: PropTypes.func.isRequired,
  below100match: PropTypes.bool.isRequired
}
