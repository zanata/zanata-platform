import React from 'react'
import PropTypes from 'prop-types'
import {
  Col, Panel, ListGroup, ListGroupItem, Label
} from 'react-bootstrap'
import {TriCheckbox} from '../../components'

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
    onImportedCheckboxChange
  } = props
  return (
    <Col xs={12}>
      <Panel className='tm-panel'>
        <ListGroup fill>
          <ListGroupItem>
            <div className='checkbox'>
              <label>
                <TriCheckbox onChange={onDocIdCheckboxChange}
                  checked={differentDocId} /> Different DocID
                <small> Document name and path</small>
                <CopyLabel copy={differentDocId} />
              </label>
            </div>
          </ListGroupItem>
          <ListGroupItem>
            <div className='checkbox'>
              <label>
                <TriCheckbox onChange={onContextCheckboxChange}
                  checked={differentContext} /> Different Context
                <small> resId, msgctxt</small>
                <CopyLabel copy={differentContext} />
              </label>
            </div>
          </ListGroupItem>
        </ListGroup>
        <span />
        <ListGroup fill>
          <ListGroupItem >
            <div className='checkbox'>
              <label>
                <TriCheckbox onChange={onImportedCheckboxChange}
                  checked={fromImportedTM} /> Match from Imported TM
                <CopyLabel copy={fromImportedTM} />
              </label>
            </div>
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
  onImportedCheckboxChange: PropTypes.func.isRequired
}
